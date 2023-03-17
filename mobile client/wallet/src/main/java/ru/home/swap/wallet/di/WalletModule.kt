package ru.home.swap.wallet.di

import android.app.Application
import com.example.wallet.R
import ru.home.swap.wallet.storage.AppDatabase
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.protocol.http.HttpService
import ru.home.swap.core.network.interceptors.DefaultInterceptor
import ru.home.swap.wallet.repository.*
import ru.home.swap.wallet.storage.CacheUtils
import ru.home.swap.wallet.storage.dao.ChainTransactionDao
import ru.home.swap.wallet.storage.model.Schema
import ru.home.swap.wallet.storage.dao.ServerTransactionDao
import javax.inject.Named

@Module
class WalletModule(val context: Application) {

    companion object {
        const val CACHE_SCOPE = "cache"
    }

    // network

    @WalletMainScope
    @Provides
    fun providesInterceptor(): Interceptor {
        return DefaultInterceptor(context)
    }

    @WalletMainScope
    @Provides
    fun providesWeb3jHttpService(interceptor: Interceptor): HttpService {
        val url: String = context.getString(R.string.ethereum_api_endpoint)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient
            .Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logging)
            .build()
        return HttpService(url, client)
    }

    // scopes
    @WalletMainScope
    @Provides
    @Named(CACHE_SCOPE)
    fun providesCacheUtilsScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.IO)
    }

    // repositories

    @WalletMainScope
    @Provides
    fun providesWalletRepository(httpService: HttpService): IWalletRepository {
        return WalletRepository(context, httpService)
    }

    @WalletMainScope
    @Provides
    fun providesStorageRepository(dao: ChainTransactionDao, serverDao: ServerTransactionDao, pagedDataSource: TxDataSource): IStorageRepository {
        return StorageRepository(dao, serverDao, pagedDataSource)
    }

    // cache

    @WalletMainScope
    @Provides
    fun providesDatabase(): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @WalletMainScope
    @Provides
    fun providesServerTransactionDao(database: AppDatabase): ServerTransactionDao {
        return database.serverTransactionDao()
    }

    @WalletMainScope
    @Provides
    fun providesChainTransactionDao(database: AppDatabase): ChainTransactionDao {
        return database.chainTransactionDao()
    }

    @WalletMainScope
    @Provides
    fun providesTxDataSource(dao: ChainTransactionDao): TxDataSource {
        return TxDataSource(context, dao, Schema.ChainTransaction.DEFAULT_PAGE_SIZE)
    }

    @WalletMainScope
    @Provides
    fun providesCacheUtils(repository: IStorageRepository, @Named(CACHE_SCOPE) scope: CoroutineScope): CacheUtils {
        return CacheUtils(repository, scope)
    }
}