package ru.home.swap.wallet.network

import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.work.*
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.contract.fromJson
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.storage.TxStatus
import javax.inject.Inject
import javax.inject.Named


class ChainWorker
@Inject constructor(
    context: Context,
    params: WorkerParameters,
    val repository: IWalletRepository,
    val cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
): CoroutineWorker(context, params) {

    companion object {
        private val TAG_CHAIN = "CHAIN"

        private const val KEY_TO = "KEY_TO"
        private const val KEY_VALUE_JSON = "KEY_VALUE_JSON"
        private const val KEY_URI = "KEY_URI"
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

    private val notificationId = 1001

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager


    override suspend fun doWork(): Result {
        Log.d(TAG_CHAIN, "Start work on minting a token")
        val progress = "Start execute tx on the chain"
        setForeground(createForegroundInfo(progress))

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
        cacheRepository.createChainTxAsFlow(tx)
            .map {
                /*newTx = it*/
                repository.mintToken(to, value, uri)
            }
            .onEach { /*preProcessResponse(it, newTx)*/ }
            .flowOn(backgroundDispatcher)
            .collect {/* processResponse(it)*/
                Log.d(TAG_CHAIN, "Get result on mint() call ${it}")
            }
        return Result.success()
    }

    private fun createForegroundInfo(progress: String): ForegroundInfo {
        val id = "10001"//applicationContext.getString(R.string.notification_channel_id)
        val title = "title"//applicationContext.getString(R.string.notification_title)
        val cancel = "cancel"//applicationContext.getString(R.string.cancel_download)
        // This PendingIntent can be used to cancel the worker
        val intent = WorkManager.getInstance(applicationContext)
            .createCancelPendingIntent(getId())

        // Create a Notification channel if necessary
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel()
        }

        val notification = NotificationCompat.Builder(applicationContext, id)
            .setContentTitle(title)
            .setTicker(title)
            .setContentText(progress)
            .setSmallIcon(dagger.android.support.R.drawable.abc_ic_arrow_drop_right_black_24dp)
            .setOngoing(true)
            // Add the cancel action to the notification which can
            // be used to cancel the worker
            .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()

        return ForegroundInfo(notificationId, notification)  // TODO shall we add FOREGROUND_TYPE  field here?

    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        // Create a Notification channel
    }

}