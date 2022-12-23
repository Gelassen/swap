package ru.home.swap.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.model.Token
import ru.home.swap.wallet.model.Transaction
import ru.home.swap.wallet.model.Wallet
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import java.math.BigInteger
import javax.inject.Inject

data class Model(
    var privateKey: String = "", // TODO refactor this dev mode solution
    val wallet: Wallet = Wallet(),
    val pendingTx: List<Transaction> = mutableListOf(),
    val status: Status = Status.NONE,
    val errors: List<String> = mutableListOf()
)
enum class Status {
    NONE, BALANCE, MINT_TOKEN, MY_TOKENS
}
class WalletViewModel
    @Inject constructor(
        val repository: IWalletRepository,
        val cacheRepository: StorageRepository
    ): ViewModel() {

    private val logger: Logger = Logger.getInstance()

    val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun balanceOf(owner: String) {
        logger.d("[start] balanceOf()")
        viewModelScope.launch {
            repository.balanceOf(owner)
                .flowOn(Dispatchers.IO)
                .collect { value ->
                    logger.d("collect get balance result")
                    processBalanceOfResponse(value)
                }
        }
        logger.d("[end] balanceOf()")
    }

    private fun processBalanceOfResponse(value: Response<BigInteger>) {
        when (value) {
            is Response.Data -> {
                state.update {
                    it.wallet.setBalance(value.data)
                    it.copy(wallet = it.wallet, status = Status.BALANCE)
                }
            }
            is Response.Error.Message -> {
                state.update {
                    it.copy(errors = it.errors + value.msg, status = Status.NONE)
                }
            }
            is Response.Error.Exception -> {
                logger.e("Get an exception due balanceOf() call", value.error)
                val error = "Something went wrong with exception: ${value.error}"
                state.update {
                    it.copy(errors = it.errors + error, status = Status.NONE)
                }
            }
        }
    }

    fun mintToken(to: String, value: Value, uri: String) {
        logger.d("[start] mintToken()")
        lateinit var newTx: Transaction
        viewModelScope.launch {
            cacheRepository.createChainTx(Transaction(0, to, value, uri, "pending"))
                .map {
                    newTx = it
                    repository.mintToken(to, value, uri)
                }
                .onEach {
                    when(it) {
                        // TODO how to cover reverted, rejected and declined status of tx ?
                        is Response.Data -> {
                            if (it.data.isStatusOK) {
                                newTx.status = "mined"
                                cacheRepository.createChainTx(newTx)
                            } else {
                                newTx.status = "reverted"
                                cacheRepository.createChainTx(newTx)
                                updateStateWithError("Reverted cause: ${it.data.revertReason}")
                            }
                        }
                        is Response.Error.Message -> {
                            newTx.status = "exception"
                            cacheRepository.createChainTx(newTx)
                            updateStateWithError("Exception: ${it.msg}")
                        }
                        is Response.Error.Exception -> {
                            newTx.status = "exception"
                            cacheRepository.createChainTx(newTx)
                            updateStateWithError("Exception: ${it.error.message}")
                        }
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    state.update { state ->
                        state.copy(
                            status = Status.MINT_TOKEN,
                            pendingTx = state.pendingTx + newTx
                        )
                    }
                }
        }
        logger.d("[end] mintToken()")
    }

    @Deprecated(message = "Get token ids over call, not by filter events as it is very time consuming")
    fun getTokensThatBelongsToMeNotConsumedNotExpired(account: String) {
        logger.d("[start] getTokens()")
        viewModelScope.launch {
            repository.getTransferEvents()
                .flowOn(Dispatchers.IO) // explicitly choose network thread
                .filter { it ->
                    it.to?.lowercase().equals(account.lowercase())
                }
                .map { it ->
                    // TODO consider to add async {} here to request offers asynchronously
                    val valueResponse = repository.getOffer(it.tokenId.toString())
                    if (valueResponse is Response.Data<*>) {
                        Token(it.tokenId!!.toLong(), valueResponse.data as Value)
                    } else {
                        Token(it.tokenId!!.toLong(), Value())
                    }
                }
                .filter { it ->
                    !it.value.isConsumed
                            && it.value.availabilityEnd.toLong() > System.currentTimeMillis()
                }
                .flowOn(Dispatchers.IO)
                .collect { token ->
                    logger.d("Collect result value for tokens owned by swap address $token")
                    state.update {
                        it.wallet.addToken(token)
                        logger.d("${account} tokens ${it.wallet.getTokens()}")
                        it.copy(
                            status = Status.MY_TOKENS,
                            wallet = it.wallet
                        )

                    }
                }
        }
        logger.d("[end] getTokens()")
    }

    fun registerUserOnSwapMarket(userWalletAddress: String) {
        viewModelScope.launch {
            repository.registerUserOnSwapMarket(userWalletAddress)
                .catch { ex ->
                    logger.e("Get an exception due registerUser() call", ex)
                    state.update { it ->
                        val error = "Can not register a new user: ${ex.message} ${ex.cause}"
                        it.copy(errors = it.errors + error, status = Status.NONE )
                    }
                }
                .flowOn(Dispatchers.IO)
/*                .map {
                    // TODO implement caching the result
                }*/
                .collect { it ->
                    processRegisterUserResponse(it)
                }
        }
    }

    private fun processRegisterUserResponse(it: Response<TransactionReceipt>) {
        when (it) {
            is Response.Data -> {
                logger.d("Collect response from registerUser() request ${it}")
            }
            is Response.Error.Message -> {
                logger.d("Collect response from registerUser(). Get an error ${it.msg}")
            }
            is Response.Error.Exception -> {
                logger.e("Collect response from registerUser(). Get an exception ", it.error)
            }
        }
    }

    fun approveTokenManager(swapChainAddress: String) {
        viewModelScope.launch {
            repository.approveTokenManager(swapChainAddress, true)
                .flowOn(Dispatchers.IO)
                .collect {
                    processApproveTokenManagerResponse(it)
                }
        }
    }

    private fun processApproveTokenManagerResponse(response: Response<TransactionReceipt>) {
        when(response) {
            is Response.Data -> {
                logger.d("Collect response from the approveTokenManager() call with the status "
                        + "${response.data.status} and tx receipt ${response}")
            }
            is Response.Error.Message -> {
                logger.d("Get an error on approveTokenManager() call ${response.msg}")
            }
            is Response.Error.Exception -> {
                logger.e("Get an error on approveTokenManager() call", response.error)
            }
        }
    }

    fun approveSwap(matchSubj: Match) {
        viewModelScope.launch {
            repository.approveSwap(matchSubj)
                .flowOn(Dispatchers.IO)
                .collect {
                    processApproveSwapResponse(it)
                }
        }
    }

    private fun processApproveSwapResponse(response: Response<TransactionReceipt>) {
        when (response) {
            is Response.Data -> {
                logger.d("Collect approve swap response ")
            }
            is Response.Error.Message -> {
                logger.d("Get an error on approve swap call ${response.msg}")
            }
            is Response.Error.Exception -> {
                logger.e("Get an error on approve swap call", response.error)
            }
        }
    }

    fun registerDemand(userAddress: String, demand: String) {
        viewModelScope.launch {
            repository.registerDemand(userAddress, demand)
                .flowOn(Dispatchers.IO)
                .collect {
                    processRegisterDemandResponse(it)
                }
        }
    }

    private fun processRegisterDemandResponse(response: Response<TransactionReceipt>) {
        when (response) {
            is Response.Data -> {
                logger.d("Register demand response with status ${response.data.status} and tx receipt ${response.data}")
            }
            is Response.Error.Message -> {
                logger.d("Get an error on register demand call ${response.msg}")
            }
            is Response.Error.Exception -> {
                logger.e("Get an error on register demand call", response.error)
            }
        }
    }

    fun getTokenIdsForUser(userAddress: String) {
        viewModelScope.launch {
            repository.getTokenIdsWithValues(userAddress, false)
                .flowOn(Dispatchers.IO)
                .collect {
                    processTokenIdsResponse(userAddress, it)
                }
        }
    }

    private fun processTokenIdsResponse(userAddress: String, response: Response<List<*>>) {
        when (response) {
            is Response.Data -> {
                logger.d("Get token ids for ${userAddress}: ${response.data}")
            }
            is Response.Error.Message -> {
                logger.d("Failed to request token ids: ${response.msg}")
            }
            is Response.Error.Exception -> {
                logger.e("Failed to obtain token ids", response.error)
            }
        }
    }

    fun swap(subj: Match) {
        viewModelScope.launch {
            repository.swap(subj)
                .flowOn(Dispatchers.IO)
                .collect {
                    processSwapResponse(it)
                }
        }
    }

    private fun processSwapResponse(response: Response<TransactionReceipt>) {
        when (response) {
            is Response.Data -> {
                logger.d("Get response for swap call: ${response.data}")
            }
            is Response.Error.Message -> {
                logger.d("Failed to execute swap call: ${response.msg}")
            }
            is Response.Error.Exception -> {
                logger.e("Failed to execute swap call:", response.error)
            }
        }
    }

    private fun updateStateWithError(error: String) {
        state.update { state ->
            state.copy(
                errors = state.errors + error
            )
        }
    }

}