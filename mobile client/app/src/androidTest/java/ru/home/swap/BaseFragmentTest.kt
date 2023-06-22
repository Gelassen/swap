package ru.home.swap

import android.content.Context
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import ru.home.swap.core.tests.NetworkIdlingResource
import ru.home.swap.utils.DataBindingIdlingResource

@LargeTest
@RunWith(AndroidJUnit4::class)
open class BaseFragmentTest {
    protected lateinit var appContext: Context
    protected val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun setUp() {
        appContext = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext

        IdlingRegistry.getInstance().register(NetworkIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun tearDown() {
        IdlingRegistry.getInstance().unregister(NetworkIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }
}