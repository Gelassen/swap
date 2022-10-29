package com.example.wallet.debug.di

import android.content.Context
import com.example.wallet.R
import dagger.Module
import dagger.Provides
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.web3j.protocol.http.HttpService
import ru.home.swap.core.network.DefaultInterceptor
import ru.home.swap.core.network.MockInterceptor
import javax.inject.Named
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
}