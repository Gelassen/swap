package ru.home.swap

import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.IdlingRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignInFragmentTest {

    private val dataBindingIdlingResource = DataBindingIdlingResource()

    private val robot = ProfileRobot()

    @Before
    fun setUp() {
//        IdlingRegistry.getInstance().register(NetworkIdlingResource.countingIdlingResource)
//        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun tearDown() {
//        IdlingRegistry.getInstance().unregister(NetworkIdlingResource.countingIdlingResource)
//        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Test
    fun onStart_enterDebugDataWIthRegisteredWalletAndSubmit_seesErrorDialog() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        robot.clickDebugButton()

        robot
            .seesName("Dmitry")
            .seesContact("+79207008090")
            .seesSecret("onemoretime")
            .seesWalletAddress("0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd")
            .clickSubmitButton()
    }

}