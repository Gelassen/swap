package ru.home.swap.core.extensions

import android.app.Application
import ru.home.swap.core.BuildConfig
import ru.home.swap.core.tests.NetworkIdlingResource

class Extensions {

    fun Application.registerIdlingResource() {
        if (BuildConfig.DEBUG) {
            NetworkIdlingResource.increment()
        }
    }

    fun Application.unregisterIdlingResource() {
        if (BuildConfig.DEBUG) {
            NetworkIdlingResource.decrement()
        }
    }

}