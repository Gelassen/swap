package ru.home.swap.core.tests

import androidx.test.espresso.idling.CountingIdlingResource


object NetworkIdlingResource {

    private const val NETWORK = "NETWORK"

    @JvmField val countingIdlingResource = CountingIdlingResource(NETWORK)

    fun increment() {
        countingIdlingResource.increment()
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.decrement()
        }
    }
}