package ru.home.swap.di

import android.content.Context
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.home.swap.R
import ru.home.swap.network.IApi
import ru.home.swap.network.MockInterceptor
import javax.inject.Singleton

@Module
class NetworkModule(val context: Context) {

    @Singleton
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

    @Singleton
    @Provides
    fun providesClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val httpClient = OkHttpClient
            .Builder()
//            .addInterceptor(MockInterceptor(context))
            .addInterceptor(logging)
            .build()

        return httpClient
    }
}