package ru.home.swap.core.network.interceptors

import android.content.Context
import okhttp3.Interceptor
import okhttp3.Response

class DefaultInterceptor(val context: Context): Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}