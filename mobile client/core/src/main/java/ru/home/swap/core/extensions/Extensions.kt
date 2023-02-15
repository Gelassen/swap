package ru.home.swap.core.extensions

import android.app.Application
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import ru.home.swap.core.BuildConfig
import ru.home.swap.core.tests.NetworkIdlingResource

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

fun <T> Flow<T>.attachIdlingResource(): Flow<T> {
    return this
        .onStart {
            if (BuildConfig.DEBUG) {
                NetworkIdlingResource.increment()
            }
        }
        .onEach {
            if (BuildConfig.DEBUG) {
                NetworkIdlingResource.decrement()
            }

        }
}