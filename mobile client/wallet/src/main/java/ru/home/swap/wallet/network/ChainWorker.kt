package ru.home.swap.wallet.network

import android.content.Context
import android.util.Log
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.home.swap.core.App
import ru.home.swap.core.di.NetworkModule
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
        // output keys
        public const val KEY_ERROR = "KEY_ERROR"
        public const val KEY_ERROR_MSG = "KEY_ERROR_MSG"
    }

    object Builder {
        fun build(to: String, valueAsJson: String, uri: String): Data {
            return workDataOf(
                Pair(KEY_TO, to),
                Pair(KEY_VALUE_JSON, valueAsJson),
                Pair(KEY_URI, uri)
            )
        }
    }

    override suspend fun doWork(): Result {
        Log.d(TAG_CHAIN, "Start work on minting a token")
        var result = Result.failure()
        setForeground(foregroundIndo)

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

        lateinit var newTx: ITransaction

        cacheRepository.createChainTxAsFlow(tx)
            .map {
                newTx = it
                repository.mintToken(to, value, uri)
            }
            .onEach { preProcessResponse(it, newTx) }
            .flowOn(backgroundDispatcher)
            .collect {
                Log.d(TAG_CHAIN, "Get result on mint() call ${it}")
                result = processResponse(it)
            }
        Log.d(App.TAG, "[ChainWorker] before Result.success()")
        return result
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

    private suspend fun preProcessResponse(it: Response<TransactionReceiptDomain>, newTx: ITransaction) {
        when(it) {
            is Response.Data -> {
                if (it.data.isStatusOK()) {
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
}