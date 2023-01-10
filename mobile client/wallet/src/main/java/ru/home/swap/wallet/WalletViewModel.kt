package ru.home.swap.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.repository.IWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.web3j.protocol.core.methods.response.TransactionReceipt
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.model.*
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.storage.TxStatus
import java.math.BigInteger
import javax.inject.Inject

data class Model(
    var privateKey: String = "", // TODO refactor this dev mode solution
    val wallet: Wallet = Wallet(),
    val pendingTx: List<ITransaction> = mutableListOf(),
    val status: Status = Status.NONE,
    val errors: List<String> = mutableListOf()
)
enum class Status {
    NONE, BALANCE, MINT_TOKEN, MY_TOKENS
}
class WalletViewModel
    @Inject constructor(
        val repository: IWalletRepository,
        val cacheRepository: IStorageRepository,
        val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): ViewModel() {

    private val logger: Logger = Logger.getInstance()

    val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    init {
        getTxFromCache()
    }

    fun getTxFromCache() {
        viewModelScope.launch {
            cacheRepository.getAllChainTransactions()
                .flowOn(backgroundDispatcher)
                .collect {
                    logger.d("Collect result of getTxFromCache() call")
                    state.update { state ->
                        state.copy(
                            pendingTx = it
                        )
                    }
                }
        }
    }

    fun balanceOf(owner: String) {
        logger.d("[start] balanceOf()")
        viewModelScope.launch {
            repository.balanceOf(owner)
                .flowOn(backgroundDispatcher)
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
        lateinit var newTx: ITransaction
        viewModelScope.launch {
            val tx = MintTransaction(
                uid = 0,
                status = TxStatus.TX_PENDING,
                to = to,
                value = value,
                uri = uri
            )
            cacheRepository.createChainTx(tx)
                .map {
                    newTx = it
                    repository.mintToken(to, value, uri)
                }
                .onEach {
                    when(it) {
                        is Response.Data -> {
                            if (it.data.isStatusOK) {
                                newTx.status = TxStatus.TX_MINED
                                cacheRepository.createChainTx(newTx)
                            } else {
                                newTx.status = TxStatus.TX_REVERTED
                                cacheRepository.createChainTx(newTx)
                            }
                        }
                        is Response.Error.Message -> {
                            newTx.status = TxStatus.TX_EXCEPTION
                            cacheRepository.createChainTx(newTx)
                        }
                        is Response.Error.Exception -> {
                            newTx.status = TxStatus.TX_EXCEPTION
                            cacheRepository.createChainTx(newTx)
                        }
                    }
                }
                .flowOn(backgroundDispatcher)
                .collect {
                    when(it) {
                        is Response.Data -> { if (!it.data.isStatusOK) updateStateWithError("Reverted cause: ${it.data.revertReason}") }
                        is Response.Error.Message -> { updateStateWithError("Exception: ${it.msg}") }
                        is Response.Error.Exception -> { updateStateWithError("Exception: ${it.error.message}") }
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
                .flowOn(backgroundDispatcher) // explicitly choose network thread
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
                .flowOn(backgroundDispatcher)
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
        logger.d("[start] registerUserOnSwapMarket()")
        viewModelScope.launch {
            val newTx = RegisterUserTransaction(userWalletAddress = userWalletAddress)
            cacheRepository.createChainTx(newTx)
                .map { it ->
                    newTx.uid = it.uid
                    repository.registerUserOnSwapMarket(userWalletAddress)
                }
                .onEach { it ->
                    // TODO think about design decision: process result here or repeat all this branches in the collect{}
                    when(it) {
                        is Response.Data -> {
                            if (it.data.isStatusOK) {
                                newTx.status = TxStatus.TX_MINED
                                cacheRepository.createChainTx(newTx)
                            } else {
                                newTx.status = TxStatus.TX_REVERTED
                                cacheRepository.createChainTx(newTx)
                                updateStateWithError("Reverted cause: ${it.data.revertReason}")
                            }
                        }
                        is Response.Error.Message -> {
                            newTx.status = TxStatus.TX_EXCEPTION
                            cacheRepository.createChainTx(newTx)
                            updateStateWithError("Exception: ${it.msg}")
                        }
                        is Response.Error.Exception -> {
                            newTx.status = TxStatus.TX_EXCEPTION
                            cacheRepository.createChainTx(newTx)
                            updateStateWithError("Exception: ${it.error.message}")
                        }
                    }
                }
                .flowOn(backgroundDispatcher)
                .collect { it ->
//                    when(it) {}
                    // TODO process result in the correct new way
                    processRegisterUserResponse(it)
                }

/*            repository.registerUserOnSwapMarket(userWalletAddress)
                .catch { ex ->
                    logger.e("Get an exception due registerUser() call", ex)
                    state.update { it ->
                        val error = "Can not register a new user: ${ex.message} ${ex.cause}"
                        it.copy(errors = it.errors + error, status = Status.NONE )
                    }
                }
                .flowOn(backgroundDispatcher)
*//*                .map {
                    // TODO implement caching the result
                }*//*
                .collect { it ->
                    processRegisterUserResponse(it)
                }*/
        }
        logger.d("[end] registerUserOnSwapMarket()")
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
                .flowOn(backgroundDispatcher)
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
                .flowOn(backgroundDispatcher)
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
                .flowOn(backgroundDispatcher)
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
                .flowOn(backgroundDispatcher)
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
                .flowOn(backgroundDispatcher)
                .collect {
                    processSwapResponse(it)
                }
        }
    }

    fun addTestRow(tx: ITransaction) {
        viewModelScope.launch {
            cacheRepository.createChainTx(tx)
                .collect {
                    logger.d("New row is inserted $it")
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