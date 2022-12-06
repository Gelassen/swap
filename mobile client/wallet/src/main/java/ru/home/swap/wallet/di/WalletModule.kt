package ru.home.swap.wallet.di

import android.content.Context
import com.example.wallet.R
import ru.home.swap.wallet.repository.IWalletRepository
import ru.home.swap.wallet.repository.StorageRepository
import ru.home.swap.wallet.repository.WalletRepository
import ru.home.swap.wallet.storage.AppDatabase
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.protocol.http.HttpService
import ru.home.swap.core.network.interceptors.DefaultInterceptor
import javax.inject.Singleton

@Module
class WalletModule(val context: Context) {

    @Singleton
    @Provides
    fun providesInterceptor(): Interceptor {
        return DefaultInterceptor(context)
    }

    @Singleton
    @Provides
    fun providesWeb3jHttpService(interceptor: Interceptor): HttpService {
        val url: String = context.getString(R.string.ethereum_api_endpoint)
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)
        val client = OkHttpClient
            .Builder()
            .addInterceptor(DefaultInterceptor(context))
            .addInterceptor(logging)
            .build()
        return HttpService(url, client)
    }

    @Singleton
    @Provides
    fun providesWalletRepository(httpService: HttpService): IWalletRepository {
        return WalletRepository(context, httpService)
    }

    @Singleton
    @Provides
    fun providesDatabase(): AppDatabase {
        return AppDatabase.getInstance(context)
    }

    @Singleton
    @Provides
    fun providesStorageRepository(database: AppDatabase): StorageRepository {
        return StorageRepository(database.chainTransactionDao())
    }
}