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
        viewModelScope.launch {
            repository.mintToken(to, value, uri)
                .catch { it ->
                    if (it is TransactionException
                        && it.message!!.contains("Transaction receipt was not generated after 600 seconds for transaction")) {
                        cacheRepository.createChainTx(to, value, uri) // TODO consider always put in pending cache and remove after it confirms as succeeded
                        val txReceipt = TransactionReceipt()
                        txReceipt.transactionHash = ""
                        emit(Response.Data(txReceipt))
                    } else {
                        emit(Response.Error.Exception(it))
                    }
                }
                .flowOn(Dispatchers.IO)
                .collect {
                    logger.d(it.toString())
                    when (it) {
                        is Response.Data -> {
                            if (it.data.transactionHash.isEmpty()) {
                                state.update {
                                    it.copy(
                                        status = Status.MINT_TOKEN,
                                        pendingTx = it.pendingTx + Transaction(to, value, uri)
                                    )
                                }
                            }
                        }
                        is Response.Error.Message -> {
                            val errorMsg = "Something went wrong on mint a token with error ${it.msg}"
                            logger.d(errorMsg)
                            state.update {
                                val newErrors = it.errors + "Something went wrong on mint a token with error ${errorMsg}"
                                it.copy(status = Status.MINT_TOKEN, errors = newErrors)
                            }
                        }
                        is Response.Error.Exception -> {
                            logger.e("Something went wrong on mint a token ${to}, ${value}, ${uri}", it.error)
                            state.update {
                                val newErrors = it.errors + "Something went wrong on mint a token ${to}, ${value}, ${uri}"
                                it.copy(status = Status.MINT_TOKEN, errors = newErrors)
                            }
                        }
                    }
                }
        }
        logger.d("[end] mintToken()")
    }

    fun getTokensThatBelongsToMeNotConsumedNotExpired(account: String) {
        logger.d("start getTokens()")
        viewModelScope.launch {
            repository.getTransferEvents()
                .flowOn(Dispatchers.IO) // explicitly choose network thread
                .filter { it ->
                    it.to?.lowercase().equals(account.lowercase())
                }
                .map { it ->
                    // TODO consider to add async {} here to request offers asynchronously
                    val value = repository.getOffer(it.tokenId.toString())
                    Token(it.tokenId!!.toLong(), value)
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
                        it.copy(
                            status = Status.MY_TOKENS,
                            wallet = it.wallet
                        )
                    }
                }
        }
        logger.d("end getTokens()")
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
                logger.d("Get an error on approveTokenManager() call ${response.error}")
            }
        }
    }
}