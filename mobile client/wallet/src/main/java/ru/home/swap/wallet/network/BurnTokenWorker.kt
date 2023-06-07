package ru.home.swap.wallet.network

import android.app.Application
import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.core.extensions.registerIdlingResource
import ru.home.swap.core.extensions.unregisterIdlingResource
import ru.home.swap.core.model.RequestType
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.fromJson
import ru.home.swap.core.network.Response
import ru.home.swap.wallet.model.BurnTransaction
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.TransactionReceiptDomain
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.model.ServerTransaction
import javax.inject.Inject
import javax.inject.Named

class BurnTokenWorker
@Inject constructor(
    context: Context,
    params: WorkerParameters,
    repository: IWalletRepository,
    cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): BaseChainWorker(context, params, repository, cacheRepository, backgroundDispatcher) {

    companion object {
        private const val KEY_OWNER = "KEY_OWNER"
        private const val KEY_TOKEN_ID = "KEY_TOKEN_ID"
        private const val KEY_SERVICE_AS_JSON = "KEY_SERVICE_AS_JSON"
    }

    object Builder {
        fun build(owner: String, tokenId: Int, service: String): Data {
            return workDataOf(
                Pair(KEY_OWNER, owner),
                Pair(KEY_TOKEN_ID, tokenId),
                Pair(KEY_SERVICE_AS_JSON, service)
            )
        }
    }

    override suspend fun doWork(): Result {
        logger.d("[RemoveServiceWorker] start work on burn a token")
        val application = (applicationContext as Application)
        application.registerIdlingResource()

        var result = Result.failure()
        setForeground(foregroundIndo)

        try {
            val (tx, serverTx) = prepareBurnTransaction()
            cacheRepository.createChainTxAndServerTx(tx as ITransaction, serverTx as ServerTransaction)

            val responseFromChain = repository.burn((tx as BurnTransaction).owner, (tx as BurnTransaction).tokenId)
            preProcessResponse(responseFromChain, tx, serverTx)
            result = processResponse(responseFromChain)
        } catch (ex: Exception) {
            logger.e("Failed to remove a token", ex)
        } finally {
            application.unregisterIdlingResource()
        }
        logger.d("[RemoveServiceWorker] stop work on burn a token")
        return result
    }

    private fun prepareBurnTransaction(): List<Any> {
        val inputData: Data = inputData
        val owner = inputData.getString(KEY_OWNER)!!
        val tokenId = inputData.getLong(KEY_TOKEN_ID, 0L)
        val serviceAsJson = inputData.getString(KEY_SERVICE_AS_JSON)!!

        val tx = BurnTransaction(owner = owner, tokenId = tokenId)
        val serverTx = ServerTransaction(
            requestType = RequestType.TX_BURN,
            payload = Service().fromJson(serviceAsJson))

        return listOf(tx, serverTx)
    }

    private fun processResponse(it: Response<TransactionReceiptDomain>): Result {
        return when(it) {
            is Response.Data -> { if (it.data.isStatusOK()) Result.success(workDataOf(Pair(Consts.KEY_ERROR, false))) else Result.success(workDataOf(Pair(Consts.KEY_ERROR, true)))}
            is Response.Error.Message -> { Result.failure(workDataOf(Pair(Consts.KEY_ERROR, true), Pair(Consts.KEY_ERROR_MSG, it.msg))) }
            is Response.Error.Exception -> { Result.failure(workDataOf(Pair(Consts.KEY_ERROR, true), Pair(Consts.KEY_ERROR_MSG, it.error.message))) }
        }
    }
}