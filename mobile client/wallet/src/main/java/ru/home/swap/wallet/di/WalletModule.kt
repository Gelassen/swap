package ru.home.swap.wallet.di

import android.app.Application
import android.content.Context
import com.example.wallet.R
import ru.home.swap.wallet.storage.AppDatabase
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.protocol.http.HttpService
import ru.home.swap.core.network.interceptors.DefaultInterceptor
import ru.home.swap.wallet.repository.*
import ru.home.swap.wallet.storage.ChainTransactionDao

@Module
class WalletModule(val context: Application) {

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

    @WalletMainScope
    @Provides
    fun providesWalletRepository(httpService: HttpService): IWalletRepository {
        return WalletRepository(context, httpService)
    }

    @WalletMainScope
    @Provides
    fun providesDatabase(): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @WalletMainScope
    @Provides
    fun providesChainTransactionDao(database: AppDatabase): ChainTransactionDao {
        return database.chainTransactionDao()
    }

    @WalletMainScope
    @Provides
    fun providesTxDataSource(dao: ChainTransactionDao): TxDataSource {
        return TxDataSource(context, dao, ChainTransactionDao.Const.DEFAULT_PAGE_SIZE)
    }

    @WalletMainScope
    @Provides
    fun providesStorageRepository(dao: ChainTransactionDao, pagedDataSource: TxDataSource): IStorageRepository {
        return StorageRepository(dao, pagedDataSource)
    }
}