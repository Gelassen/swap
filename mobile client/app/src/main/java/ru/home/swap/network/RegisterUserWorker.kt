package ru.home.swap.network

import android.app.Application
import android.content.Context
import androidx.work.Data
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.home.swap.core.extensions.registerIdlingResource
import ru.home.swap.core.extensions.unregisterIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.RequestType
import ru.home.swap.core.network.Response
import ru.home.swap.repository.IPersonRepository
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.RegisterUserTransaction
import ru.home.swap.wallet.model.TransactionReceiptDomain
import ru.home.swap.wallet.network.BaseChainWorker
import ru.home.swap.wallet.network.BaseChainWorker.Consts.KEY_ERROR
import ru.home.swap.wallet.network.BaseChainWorker.Consts.KEY_ERROR_MSG
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.model.ServerTransaction
import ru.home.swap.wallet.storage.model.TxStatus
import javax.inject.Inject

class RegisterUserWorker
    @Inject constructor(
        context: Context,
        params: WorkerParameters,
        repository: IWalletRepository,
        cacheRepository: IStorageRepository,
        backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO,
        val personRepository: IPersonRepository,
) : BaseChainWorker(context, params, repository, cacheRepository, backgroundDispatcher) {

    companion object {
        public const val KEY_PERSON_PROFILE = "KEY_PERSON_PROFILE"
    }

    object Builder {
        fun build(personProfile: PersonProfile) : Data {
            return workDataOf(Pair(KEY_PERSON_PROFILE, personProfile.toJson()))
        }
    }

    override suspend fun doWork(): Result {
        logger.d("[RegisterUserWorker] Start work on register user")
        val application = (applicationContext as Application)
        application.registerIdlingResource()

        var result = Result.failure()
        setForeground(foregroundIndo)

        try {
            val personProfile = PersonProfile().fromJson(inputData.getString(KEY_PERSON_PROFILE)!!)
            val tx = RegisterUserTransaction(userWalletAddress = personProfile.userWalletAddress)
            val serverTx = ServerTransaction(requestType = RequestType.TX_REGISTER_USER, payload = personProfile)
            personRepository.cacheAccount(personProfile)

            val cachedRecordsIds = cacheRepository.createChainTxAndServerTx(tx, serverTx)
            val responseFromChain = repository.registerUserOnSwapMarket(userWalletAddress = personProfile.userWalletAddress)

            val statusAndResult = defineCachedTxStatusAndResult(responseFromChain, personProfile)
            updateRecordInCache(tx, serverTx, cachedRecordsIds, statusAndResult)

            result = statusAndResult.second
        } catch(ex: Exception) {
            logger.e("Register user worker has been failed", ex)
        } finally {
            application.unregisterIdlingResource()
        }

        logger.d("[RegisterUserWorker] Stop work on register user")
        return result
    }

    private suspend fun updateRecordInCache(tx: ITransaction,
                                            serverTx: ServerTransaction,
                                            cachedRecordsIds: Pair<Long, Long>,
                                            statusAndResult: Pair<String, Result>) {

        tx.uid = cachedRecordsIds.first
        tx.status =statusAndResult.first
        serverTx.uid = cachedRecordsIds.second
        serverTx.txChainId = tx.uid
        cacheRepository.createChainTxAndServerTx(tx, serverTx)
    }

    private fun defineCachedTxStatusAndResult(
        responseFromChain: Response<TransactionReceiptDomain>,
        personProfile: PersonProfile
    ): Pair<String, Result> {
        val txStatus: String
        val result: Result
        when(responseFromChain) {
            is Response.Data -> {
                if (responseFromChain.data.isStatusOK()) {
                    txStatus = TxStatus.TX_MINED
                    result = Result.success(
                        workDataOf(
                            Pair(KEY_ERROR, false),
                            Pair(KEY_PERSON_PROFILE, personProfile.toJson())
                        )
                    )
                } else {
                    txStatus = TxStatus.TX_REVERTED
                    result = Result.failure(
                        workDataOf(Pair(KEY_ERROR, true), Pair(KEY_ERROR_MSG, responseFromChain.data.getRevertReason()))
                    )
                }
            }
            is Response.Error.Message -> {
                txStatus = TxStatus.TX_EXCEPTION
                result = Result.failure(
                    workDataOf(Pair(KEY_ERROR, true), Pair(KEY_ERROR_MSG, responseFromChain.msg))
                )

            }
            is Response.Error.Exception -> {
                // cover the case when user has been registered on both backend and chain, aka sign-in
                if (responseFromChain.error.message?.contains("User already registered") == true) {
                    // we consider previous successful tx as sharing its mined status with this tx
                    // to avoid confusion from user perspective who will observe UI
                    txStatus = TxStatus.TX_MINED
                    result = Result.success(
                        workDataOf(
                            Pair(KEY_ERROR, false),
                            Pair(KEY_PERSON_PROFILE, personProfile.toJson())
                        )
                    )
                } else {
                    txStatus = TxStatus.TX_EXCEPTION
                    result = Result.failure(
                        workDataOf(Pair(KEY_ERROR, true), Pair(KEY_ERROR_MSG, responseFromChain.error.message))
                    )
                }
            }
        }
        return Pair(txStatus, result)
    }
}