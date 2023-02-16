package ru.home.swap.network

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.di.AppMainScope
import ru.home.swap.wallet.network.ChainWorker
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.repository.IWalletRepository
import javax.inject.Inject
import javax.inject.Named

// TODO it is better to move it into the wallet module, but I haven't solved yet DI issue due necessity
//  to do injection into Application -> Application inheritance -> compiler time issue with
//  binding from different scope

@AppMainScope
class MyWorkerFactory @Inject constructor(
    val repository: IWalletRepository,
    val cacheRepository: IStorageRepository,
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) : WorkerFactory()/*DelegatingWorkerFactory()*/ {
    /*    init {
        addFactory(myWorkerFactory(service))
        // Add here other factories that you may need in your application
    }*/
    override fun createWorker(
        appContext: Context,
        workerClassName: String,
        workerParameters: WorkerParameters
    ): ListenableWorker? {
        return when(workerClassName) {
            ChainWorker::class.java.name ->
                ChainWorker(
                    context = appContext,
                    params = workerParameters,
                    repository = repository,
                    cacheRepository =  cacheRepository,
                    backgroundDispatcher = backgroundDispatcher
                )
            else -> null
        }
    }


}