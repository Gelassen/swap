package ru.home.swap.core.tests

import androidx.test.espresso.idling.net.UriIdlingResource


object NetworkIdlingResource {

    private const val NETWORK = "NETWORK"
    private const val TIMEOUT = 3000L
    /**
     * There was an idling resource issue. Current design split tx minting and server side operations
     * into separate flows. Solution was to switch on UriIdlingResource
     * https://developer.android.com/training/testing/espresso/idling-resource#example-implementations
     */
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