package ru.home.swap.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.repository.IWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.web3j.protocol.core.methods.response.TransactionReceipt
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.contract.convertToJson
import ru.home.swap.wallet.model.*
import ru.home.swap.wallet.network.ChainWorker
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.storage.TxStatus
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Named

data class Model(
    var balance: BigInteger = BigInteger.valueOf(0),
    val tokens: MutableList<Token> = mutableListOf(),
    val pendingTx: List<ITransaction> = mutableListOf(),
    val status: Status = Status.NONE,
    val errors: List<String> = mutableListOf()
)
enum class Status {
    NONE, BALANCE, MINT_TOKEN, MY_TOKENS
}
class WalletViewModel
    @Inject constructor(
        private val app: Application,
        private val repository: IWalletRepository,
        private val cacheRepository: IStorageRepository,
        @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): AndroidViewModel(app) {

    private val logger: Logger = Logger.getInstance()

    val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

/*    private val work = OneTimeWorkRequestBuilder<ChainWorker>()
        .setConstraints(
            Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        )
        .setInputData(*//* input data *//*)
        .build()*/
/*    private val workInfo = WorkManager.getInstance(app)
        .getWorkInfoByIdLiveData(work.id)
        .asFlow()*/

    init {
        getTxFromCache()
    }

    private fun getWorkRequest(inputData: Data): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<ChainWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(inputData) /* input data for worker */
            .build()
    }

    private fun getTxFromCache() {
        logger.d("[start] getTxFromCache()")
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
        logger.d("[end] getTxFromCache()")
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
                    it.copy(balance = value.data, status = Status.BALANCE)
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
            val work = getWorkRequest(ChainWorker.Builder.build(to, value.convertToJson(), uri))
            WorkManager.getInstance(app).enqueue(work)
            WorkManager.getInstance(app)
                .getWorkInfoByIdLiveData(work.id)
                .asFlow()
                .collect { it ->
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> { logger.d("[mint token] succeeded status with result: ${it.toString()}")}
                        WorkInfo.State.FAILED -> { logger.d("[mint token] failed status with result: ${it.toString()}") }
                        else -> { logger.d("[mint token] unexpected state with result: ${it.toString()}") }
                    }
            }
        }
/*        lateinit var newTx: ITransaction
        viewModelScope.launch {
            val tx = MintTransaction(
                uid = 0,
                status = TxStatus.TX_PENDING,
                to = to,
                value = value,
                uri = uri
            )
            cacheRepository.createChainTxAsFlow(tx)
                .map {
                    newTx = it
                    repository.mintToken(to, value, uri)
                }
                .onEach { preProcessResponse(it, newTx) }
                .flowOn(backgroundDispatcher)
                .collect { processResponse(it) }
        }*/
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
                        it.tokens.add(token)
                        logger.d("${account} tokens ${it.tokens}")
                        it.copy(
                            status = Status.MY_TOKENS,
                            tokens = it.tokens
                        )

                    }
                }
        }
        logger.d("[end] getTokens()")
    }

    fun registerUserOnSwapMarket(userWalletAddress: String) {
        logger.d("[start] registerUserOnSwapMarket()")
        viewModelScope.launch {
            lateinit var newTx: ITransaction
            cacheRepository.createChainTxAsFlow(RegisterUserTransaction(userWalletAddress = userWalletAddress))
                .map { it ->
                    newTx= it
                    repository.registerUserOnSwapMarket(userWalletAddress)
                }
                .onEach { it -> preProcessResponse(it, newTx) }
                .flowOn(backgroundDispatcher)
                .collect { it -> processResponse(it) }
        }
        logger.d("[end] registerUserOnSwapMarket()")
    }

    fun approveTokenManager(swapChainAddress: String) {
        viewModelScope.launch {
            val tx = ApproveTokenManagerTransaction(swapMarketContractAddress = swapChainAddress, isApproved = true)
            lateinit var newTx: ITransaction
            cacheRepository.createChainTxAsFlow(tx)
                .map {
                    newTx = it
                    repository.approveTokenManager(swapChainAddress, true)
                }
                .onEach { preProcessResponse(it, newTx) }
                .flowOn(backgroundDispatcher)
                .collect {
                    processResponse(it)
                }
        }
    }

    fun approveSwap(matchSubj: Match) {
        viewModelScope.launch {
            lateinit var newTx: ITransaction
            cacheRepository.createChainTxAsFlow(ApproveSwapTransaction(match = matchSubj))
                .map {
                    newTx = it
                    repository.approveSwap(matchSubj)
                }
                .onEach { preProcessResponse(it, newTx) }
                .flowOn(backgroundDispatcher)
                .collect { processResponse(it) }
        }
    }

    @Deprecated("Since SwapChainV2.sol registerDemand() is not supported")
    fun registerDemand(userAddress: String, demand: String) {
        viewModelScope.launch {
            val tx = RegisterDemandTransaction(userAddress = userAddress, demand = demand)
            lateinit var newTx: ITransaction
            cacheRepository.createChainTxAsFlow(tx)
                .map {
                    newTx = it
                    repository.registerDemand(userAddress, demand)
                }
                .onEach { preProcessResponse(it, newTx) }
                .flowOn(backgroundDispatcher)
                .collect { processResponse(it) }
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

    fun getMatchesForProfile(firstUserAddress: String, secondUserAddress: String) {
        viewModelScope.launch {
            repository.getMatchesAsFlow(firstUserAddress, secondUserAddress)
                .flowOn(backgroundDispatcher)
                .collect { it ->
                    logger.d("Get matches result ${it.toString()}")
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
            lateinit var newTx: ITransaction
            cacheRepository.createChainTxAsFlow(SwapTransaction(match = subj))
                .map {
                    newTx = it
                    repository.swap(subj)
                }
                .onEach { preProcessResponse(it, newTx) }
                .flowOn(backgroundDispatcher)
                .collect { processResponse(it) }
        }
    }

    private fun preProcessResponse(it: Response<TransactionReceiptDomain>, newTx: ITransaction) {
        when(it) {
            is Response.Data -> {
                if (it.data.isStatusOK()) {
                    newTx.status = TxStatus.TX_MINED
                    cacheRepository.createChainTxAsFlow(newTx)
                } else {
                    newTx.status = TxStatus.TX_REVERTED
                    cacheRepository.createChainTxAsFlow(newTx)
                }
            }
            is Response.Error.Message -> {
                newTx.status = TxStatus.TX_EXCEPTION
                cacheRepository.createChainTxAsFlow(newTx)
            }
            is Response.Error.Exception -> {
                newTx.status = TxStatus.TX_EXCEPTION
                cacheRepository.createChainTxAsFlow(newTx)
            }
        }
    }

    private fun processResponse(it: Response<TransactionReceiptDomain>) {
        when(it) {
            is Response.Data -> { if (!it.data.isStatusOK()) updateStateWithError("Reverted cause: ${it.data.getRevertReason()}") }
            is Response.Error.Message -> { updateStateWithError("Exception: ${it.msg}") }
            is Response.Error.Exception -> { updateStateWithError("Exception: ${it.error.message}") }
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