package ru.home.swap

import android.net.Uri
import org.junit.Assert
import org.junit.Test

class UrlPathUnitTest {

    @Test
    fun testUrlPathSegments_path() {
        val str = "https://google.com/api/account"

        val uri = Uri.parse(str)

        Assert.assertEquals("/api/account", uri.pathSegments)
    }
}