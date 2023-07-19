package ru.home.swap.wallet.di

import android.app.Application
import androidx.work.WorkManager
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
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.network.interceptors.DefaultInterceptor
import ru.home.swap.wallet.R
import ru.home.swap.wallet.model.ChainConfig
import ru.home.swap.wallet.repository.*
import ru.home.swap.wallet.storage.CacheUtils
import ru.home.swap.wallet.storage.dao.ChainTransactionDao
import ru.home.swap.wallet.storage.model.Schema
import ru.home.swap.wallet.storage.dao.ServerTransactionDao
import ru.home.swap.wallet.storage.dao.SwapMatchDao
import javax.inject.Named

@Module
open class WalletModule(val context: Application) {

    companion object {
        const val CACHE_SCOPE = "cache"
        const val CONFIG_SCOPE = "config"
    }

    // network

    @WalletMainScope
    @Provides
    fun providesInterceptor(): Interceptor {
        return DefaultInterceptor(context)
    }

    @WalletMainScope
    @Provides
    open fun providesWeb3jHttpService(
        interceptor: Interceptor,
        @Named(CONFIG_SCOPE) ethereumEndpoint: String): HttpService {

        val url: String = ethereumEndpoint
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient
            .Builder()
            .addInterceptor(interceptor)
            .addInterceptor(logging)
            .build()
        return HttpService(url, client)
    }

    @WalletMainScope
    @Provides
    fun providesWorkManager(app: Application): WorkManager {
        return WorkManager.getInstance(app)
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
    fun providesWalletRepository(httpService: HttpService, chainConfig: ChainConfig): IWalletRepository {
        return WalletRepository(context, httpService, chainConfig)
    }

    @WalletMainScope
    @Provides
    fun providesStorageRepository(
        dao: ChainTransactionDao,
        serverDao: ServerTransactionDao,
        swapMatchDao: SwapMatchDao,
        pagedDataSource: TxDataSource): IStorageRepository {

        return StorageRepository(
            chainTransactionDao = dao,
            serverDao = serverDao,
            matchDao = swapMatchDao,
            pagedDataSource = pagedDataSource)
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
    fun providesSwapMatchDao(database: AppDatabase): SwapMatchDao {
        return database.matchDao()
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

    // config

    @WalletMainScope
    @Provides
    @Named(CONFIG_SCOPE)
    open fun providesEthereumEndpoint(): String {
        return context.getString(R.string.ethereum_api_endpoint)
    }

    @WalletMainScope
    @Provides
    open fun providesChainConfig(): ChainConfig {
        return ChainConfig(
            chainId = context.getString(R.string.chain_id).toLong(),
            swapTokenAddress = context.getString(R.string.swap_value_contract_address),
            swapMarketAddress = context.getString(R.string.swap_chain_contract_address),
            accountPrivateKey = context.getString(R.string.first_acc_private_key)
        )
    }
}