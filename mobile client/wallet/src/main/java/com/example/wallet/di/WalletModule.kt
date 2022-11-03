package com.example.wallet.di

import android.content.Context
import com.example.wallet.R
import com.example.wallet.repository.IWalletRepository
import com.example.wallet.repository.StorageRepository
import com.example.wallet.repository.WalletRepository
import com.example.wallet.storage.AppDatabase
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
        val url: String = context.getString(R.string.test_infura_api)
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