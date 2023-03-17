package ru.home.swap.wallet.storage

import kotlinx.coroutines.*
import ru.home.swap.core.di.NetworkModule
import ru.home.swap.wallet.di.WalletModule
import ru.home.swap.wallet.repository.IStorageRepository
import javax.inject.Inject
import javax.inject.Named

class CacheUtils
@Inject constructor(
    val repository: IStorageRepository,
    @Named(WalletModule.CACHE_SCOPE) val scope: CoroutineScope, // supposed to be CoroutineScope(SupervisorJob() + Dispatchers.IO)
    @Named(NetworkModule.DISPATCHER_IO) val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    object Const {
        const val MAX_LIMIT = 60
        const val DEFAULT_RECORD_TO_CLEAN_UP = MAX_LIMIT - 10 // it is safer to keep last several records
    }

    fun cleanCacheIfRequired() {
        scope.launch {
            val totalRecordsCount = repository.getStorageRecordsCount()
            if (totalRecordsCount < Const.MAX_LIMIT) return@launch

            repository.removeCachedData(Const.DEFAULT_RECORD_TO_CLEAN_UP)
        }
    }

    fun cancel() {
        scope.cancel("Manually cancel coroutines within CacheUtils.")
    }

}