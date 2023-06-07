package ru.home.swap.wallet

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.asFlow
import androidx.lifecycle.viewModelScope
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.repository.IWalletRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.*
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.contract.convertToJson
import ru.home.swap.wallet.contract.toMatchSubject
import ru.home.swap.wallet.model.*
import ru.home.swap.wallet.network.BaseChainWorker
import ru.home.swap.wallet.network.MintTokenWorker
import ru.home.swap.wallet.network.getWorkRequest
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.storage.model.ServerTransaction
import ru.home.swap.wallet.storage.model.TxStatus
import java.math.BigInteger
import javax.inject.Inject
import javax.inject.Named

data class Model(
    var balance: BigInteger = BigInteger.valueOf(0),
    val tokens: MutableList<Token> = mutableListOf(),
    val pendingTx: List<ITransaction> = mutableListOf(),
    val status: Status = Status.NONE,
    val isLoading: Boolean = false,
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
        private val workManager: WorkManager,
        @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
    ): AndroidViewModel(app) {

    private val logger: Logger = Logger.getInstance()

    val state: MutableStateFlow<Model> = MutableStateFlow(Model())
    val uiState: StateFlow<Model> = state
        .asStateFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, state.value)

    fun balanceOf(owner: String) {
        logger.d("[start] balanceOf()")
        viewModelScope.launch {
            repository.balanceOf(owner)
                .flowOn(backgroundDispatcher)
                .collect { value ->
                    logger.d("collect get balance result")
                    processBalanceOfResponse(value)
                    logger.d("[end] balanceOf()")
                }
        }
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
            val work = workManager.getWorkRequest<MintTokenWorker>(MintTokenWorker.Builder.build(to, value.convertToJson(), uri, "stub"))
            workManager.enqueue(work)
            workManager
                .getWorkInfoByIdLiveData(work.id)
                .asFlow()
                .collect { it ->
                    when(it.state) {
                        WorkInfo.State.SUCCEEDED -> { logger.d("[mint token] succeeded status with result: ${it.toString()}")}
                        WorkInfo.State.FAILED -> {
                            logger.d("[mint token] failed status with result: ${it.toString()}")
                            val error = it.outputData.keyValueMap.get(BaseChainWorker.Consts.KEY_ERROR_MSG) as String
                            state.update { state -> state.copy(isLoading = false, errors = state.errors.plus(error)) }
                        }
                        else -> { logger.d("[mint token] unexpected state with result: ${it.toString()}") }
                    }
                    logger.d("[end] mintToken()")
                }
        }
    }

    fun registerUserOnSwapMarket(userWalletAddress: String, personProfile: PersonProfile) {
        logger.d("[start] registerUserOnSwapMarket()")
        viewModelScope.launch {
            val tx = RegisterUserTransaction(userWalletAddress = userWalletAddress)
            val serverTx = ServerTransaction(
                requestType = RequestType.TX_REGISTER_USER,
                payload = personProfile
            )
            val cachedRecordsIds = cacheRepository.createChainTxAndServerTx(tx, serverTx)
            val responseFromChain = repository.registerUserOnSwapMarket(userWalletAddress)

            tx.uid = cachedRecordsIds.first
            serverTx.uid = cachedRecordsIds.second
            preProcessResponse(responseFromChain, tx, serverTx)
            processResponse(responseFromChain)
            logger.d("[end] registerUserOnSwapMarket()")
        }
    }

    fun approveTokenManager(swapChainAddress: String) {
        logger.d("[start] approveTokenManager()")
        viewModelScope.launch {
            val tx = ApproveTokenManagerTransaction(swapMarketContractAddress = swapChainAddress, isApproved = true)
            val cachedRecordId = cacheRepository.createChainTx(tx)
            val responseFromChain = repository.approveTokenManager(swapChainAddress, true)

            tx.uid = cachedRecordId
            preProcessResponse(responseFromChain, tx)
            processResponse(responseFromChain)
            logger.d("[end] approveTokenManager()")
        }
    }

    fun approveSwap(matchSubj: Match) {
        logger.d("[start] approveSwap()")
        viewModelScope.launch {
            val tx = ApproveSwapTransaction(match = matchSubj)
            val serverTx = ServerTransaction(
                requestType = RequestType.TX_APPROVE_SWAP,
                payload = matchSubj.toMatchSubject()
            )
            val cachedRecordsIds = cacheRepository.createChainTxAndServerTx(tx, serverTx)
            val responseFromChain = repository.approveSwap(matchSubj)

            tx.uid = cachedRecordsIds.first
            serverTx.uid = cachedRecordsIds.second
            preProcessResponse(responseFromChain, tx, serverTx)
            processResponse(responseFromChain)
            logger.d("[end] approveSwap()")
        }
    }

    fun getTokenIdsForUser(userAddress: String) {
        logger.d("[start] getTokenIdsForUser()")
        viewModelScope.launch {
            repository.getTokenIdsWithValues(userAddress, false)
                .flowOn(backgroundDispatcher)
                .collect {
                    processTokenIdsResponse(userAddress, it)
                    logger.d("[end] getTokenIdsForUser()")
                }
        }
    }

    fun getMatchesForProfile(firstUserAddress: String, secondUserAddress: String) {
        logger.d("[start] getMatchesForProfile()")
        viewModelScope.launch {
            repository.getMatchesAsFlow(firstUserAddress, secondUserAddress)
                .flowOn(backgroundDispatcher)
                .collect { it ->
                    logger.d("Get matches result ${it.toString()}")
                    logger.d("[end] getMatchesForProfile()")
                }
        }
    }

    fun swap(matchSubj: Match) {
        logger.d("[start] swap()")
        viewModelScope.launch {
            val tx = SwapTransaction(match = matchSubj)
            val serverTx = ServerTransaction(
                requestType = RequestType.TX_SWAP,
                payload = matchSubj.toMatchSubject()
            )
            val cachedRecordsIds = cacheRepository.createChainTxAndServerTx(tx, serverTx)
            val responseFromChain = repository.swap(matchSubj)

            tx.uid = cachedRecordsIds.first
            serverTx.uid = cachedRecordsIds.second
            preProcessResponse(responseFromChain, tx, serverTx)
            processResponse(responseFromChain)
            logger.d("[end] swap()")
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

    private fun getWorkRequest(inputData: Data): OneTimeWorkRequest {
        return OneTimeWorkRequestBuilder<MintTokenWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(inputData) /* input data for worker */
            .build()
    }

    private suspend fun preProcessResponse(it: Response<TransactionReceiptDomain>, newTx: ITransaction, serverTxPayload: ServerTransaction): Pair<Long, Long> {
        when(it) {
            is Response.Data -> { newTx.status = if (it.data.isStatusOK()) TxStatus.TX_MINED else TxStatus.TX_REVERTED }
            is Response.Error.Message -> { newTx.status = TxStatus.TX_EXCEPTION }
            is Response.Error.Exception -> { newTx.status = TxStatus.TX_EXCEPTION }
        }
        return cacheRepository.createChainTxAndServerTx(newTx, serverTxPayload)
    }

    private suspend fun preProcessResponse(it: Response<TransactionReceiptDomain>, newTx: ITransaction): Long {
        when(it) {
            is Response.Data -> { newTx.status = if (it.data.isStatusOK()) TxStatus.TX_MINED else TxStatus.TX_REVERTED }
            is Response.Error.Message -> { newTx.status = TxStatus.TX_EXCEPTION }
            is Response.Error.Exception -> { newTx.status = TxStatus.TX_EXCEPTION }
        }
        return cacheRepository.createChainTx(newTx)
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