package ru.home.swap.core.di

import android.content.Context
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.home.swap.core.R
import ru.home.swap.core.network.IApi
import javax.inject.Named
import javax.inject.Singleton

@Module
class NetworkModule(val context: Context) {

    companion object {
        const val DISPATCHER_IO = "Dispatchers.IO"
        const val DISPATCHER_MAIN = "Dispatchers.Main"
    }

    @CoreMainScope
    @Provides
    fun providesApi(httpClient: OkHttpClient): IApi {
//        val customGson = GsonBuilder().registerTypeAdapterFactory(CustomTypeAdapterFactory()).create()
        val url = context.getString(R.string.url)
        val retrofit = Retrofit.Builder()
//            .addConverterFactory(GsonConverterFactory.create(customGson))
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient)
            .baseUrl(url)
            .build()

        return retrofit.create(IApi::class.java)
    }

    @CoreMainScope
    @Provides
    fun providesClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient
            .Builder()
            .addInterceptor(logging)
            .build()

        return httpClient
    }

    @CoreMainScope
    @Provides
    @Named(DISPATCHER_IO)
    fun providesNetworkDispatcher(): CoroutineDispatcher {
        return Dispatchers.IO
    }
}