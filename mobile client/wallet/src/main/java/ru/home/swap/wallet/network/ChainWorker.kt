package ru.home.swap.wallet.network

import android.app.Application
import android.content.Context
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.generated.Uint256
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.extensions.attachIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.fromJson
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.contract.fromJson
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.model.TransactionReceiptDomain
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.TxStatus
import javax.inject.Inject
import javax.inject.Named

class ChainWorker
@Inject constructor(
    context: Context,
    params: WorkerParameters,
    repository: IWalletRepository,
    cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): BaseChainWorker(context, params, repository, cacheRepository, backgroundDispatcher) {

    companion object {
        private val TAG_CHAIN = "CHAIN"

        private const val KEY_TO = "KEY_TO"
        private const val KEY_VALUE_JSON = "KEY_VALUE_JSON"
        private const val KEY_URI = "KEY_URI"
        private const val KEY_METADATA_JSON = "KEY_METADATA"
        // output keys
        public const val KEY_ERROR = "KEY_ERROR"
        public const val KEY_ERROR_MSG = "KEY_ERROR_MSG"
    }

    object Builder {
        fun build(to: String, valueAsJson: String, uri: String, serverMetadataAsJson: String): Data {
            return workDataOf(
                Pair(KEY_TO, to),
                Pair(KEY_VALUE_JSON, valueAsJson),
                Pair(KEY_URI, uri),
                Pair(KEY_METADATA_JSON, serverMetadataAsJson)
            )
        }
    }

    private val logger = Logger.getInstance()

    override suspend fun doWork(): Result {
        logger.d("[ChainWorker] Start work on minting a token")
        var result = Result.failure()
        setForeground(foregroundIndo)

        val (tx, to, value, uri, service) = prepareMintTransaction()
        lateinit var newTx: MintTransaction

        cacheRepository.createChainTxAsFlow(tx as ITransaction)
            .map {
                (service as Service).chainService.id = it.uid
                cacheRepository.createServerTx(service, it.uid, false)
//                flow { emit(it) }
                return@map it
            }
            .map {
                newTx = it as MintTransaction
                repository.mintToken(to as String, value as Value, uri as String)
            }
            .onEach {
                if (it is Response.Data) {
                    val tokenId = FunctionReturnDecoder.decodeIndexedValue(
                        it.data.topics.get(it.data.topics.size - 1),
                        object: TypeReference<Uint256>()  {}
                    ) as Uint256
                    newTx.tokenId = tokenId.value.toInt()
                }
                preProcessResponse(it, newTx)
            }
            .attachIdlingResource()
            .catch { logger.e("Failed ChainWorker", it) }
            .flowOn(backgroundDispatcher)
            .collect { result = processResponse(it) }
        logger.d("[ChainWorker] end work on minting a token")
        return result
    }

    private fun prepareMintTransaction(): List<Any> {
        val inputData: Data = inputData
        val to = inputData.getString(KEY_TO)!!
        val value = Value().fromJson(inputData.getString(KEY_VALUE_JSON)!!)
        val uri = inputData.getString(KEY_URI)!!
        val tx = MintTransaction(
            uid = 0,
            status = TxStatus.TX_PENDING,
            to = to,
            value = value,
            uri = uri
        )
        val service = Service().fromJson(inputData.getString(KEY_METADATA_JSON)!!)
        return listOf(tx, to, value, uri, service)
    }

    private fun processResponse(it: Response<TransactionReceiptDomain>): Result {
        var result = Result.failure()
        when(it) {
            is Response.Data -> {
                if (!it.data.isStatusOK()) {
                    result = Result.failure(workDataOf(Pair(KEY_ERROR, true), Pair(KEY_ERROR_MSG, it.data.getRevertReason())))
                } else {
                    result = Result.success(workDataOf(Pair(KEY_ERROR, false)))
                }
            }
            is Response.Error.Message -> { result = Result.failure(workDataOf(Pair(KEY_ERROR, true), Pair(KEY_ERROR_MSG, it.msg))) }
            is Response.Error.Exception -> { result = Result.failure(workDataOf(Pair(KEY_ERROR, true), Pair(KEY_ERROR_MSG, it.error.message))) }
        }
        return result
    }

}