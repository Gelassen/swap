package ru.home.swap.core.logger

import android.util.Log
import ru.home.swap.core.App
import ru.home.swap.core.BuildConfig
import java.lang.Exception

class Logger {

    companion object {

        private val logger: Logger = Logger()

        fun getInstance(): Logger {
            return logger
        }
    }

    fun e(str: String, exception: Throwable) {
        if (BuildConfig.DEBUG) {
            Log.e(App.TAG, str, exception)
        }
    }

    fun d(str: String) {
        Log.d(App.TAG, str)
    }
}