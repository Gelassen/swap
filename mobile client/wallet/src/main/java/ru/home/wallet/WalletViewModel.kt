package ru.home.wallet

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wallet.contract.Value
import com.example.wallet.model.Token
import com.example.wallet.model.Transaction
import com.example.wallet.model.Wallet
import com.example.wallet.repository.IWalletRepository
import com.example.wallet.repository.StorageRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.exceptions.TransactionException
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.network.Response
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

    @Deprecated("Repository is passed now via dagger DI")
    fun setRepository(context: Context) {
//        repository = WalletRepository(context)
    }

    fun balanceOf(owner: String) {
        logger.d("[start] balanceOf()")
        viewModelScope.launch {
            repository.balanceOf(owner)
                .flowOn(Dispatchers.IO)
                .collect { value ->
                    logger.d("collect get balance result")
                    state.update {
                        it.wallet.setBalance(value)
                        it.copy(wallet = it.wallet, status = Status.BALANCE)
                    }
                }
        }
        logger.d("[end] balanceOf()")
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
                    logger.d("Collect result value for tokens owned by wallet address $token")
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
//            repository.registerUser(userWalletAddress)
        }
    }
}