package ru.home.swap.core.tests

import androidx.test.espresso.idling.net.UriIdlingResource


object NetworkIdlingResource {

    private const val NETWORK = "NETWORK"
    private const val TIMEOUT = 3000L

    @JvmField val countingIdlingResource = UriIdlingResource(NETWORK, TIMEOUT)//CountingIdlingResource(NETWORK)

    fun increment() {
        countingIdlingResource.beginLoad("Some network related thing is started")
    }

    fun decrement() {
        if (!countingIdlingResource.isIdleNow) {
            countingIdlingResource.endLoad("Some network related thing is finished")
        }
    }
}