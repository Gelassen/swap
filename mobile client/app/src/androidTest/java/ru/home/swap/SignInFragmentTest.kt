package ru.home.swap

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Test
import org.junit.runner.RunWith
import ru.home.swap.robots.ProfileRobot
import ru.home.swap.utils.monitorActivity
import java.time.LocalDateTime

@LargeTest
@RunWith(AndroidJUnit4::class)
class SignInFragmentTest : BaseFragmentTest() {

    private val robot = ProfileRobot()

    @Test
    fun onStart_whenServerAreTurnedOffEnterDebugDataAndSubmit_seesErrorDialog() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        robot.clickDebugButton()

        robot
            .seesName(TestAppApplication.FIRST_USER_NAME)
            .seesContact(TestAppApplication.FIRST_USER_CONTACT)
            .seesSecret(TestAppApplication.FIRST_USER_SECRET)
            .seesWalletAddress(TestAppApplication.FIRST_USER_ADDRESS)
            .clickSubmitButton()

        robot.seesErrorDialog("Something went wrong")

        activityScenario.close()
    }

    @Test
    fun onStart_enterDebugDataWithRegisteredWalletAndSubmit_seesErrorDialog() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        robot.clickDebugButton()

        robot
            .seesName(TestAppApplication.FIRST_USER_NAME)
            .seesContact(TestAppApplication.FIRST_USER_CONTACT)
            .seesSecret(TestAppApplication.FIRST_USER_SECRET)
            .seesWalletAddress(TestAppApplication.FIRST_USER_ADDRESS)
            .clickSubmitButton()

        robot.seesErrorDialog("Failed register the profile on chain with status exception")

        activityScenario.close()
    }

    @Test
    fun onStart_enterDebugDataWithNotRegisteredAccountAndSubmit_seesEmptyProfileScreen() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TODO I haven't found yet a way to automatically generate this input data, so it requires to do it manually
        val newName = "Dmitry ${LocalDateTime.now()}"
        val newPhone = "+7920700803"
        val secret = "onemoretime"
        val newWallet = TestAppApplication.TestConfig.FIRST_USER_ADDRESS//"0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2006"

        robot.enterCustomDebugData(newName, newPhone, secret, newWallet)
        robot
            .seesName(newName)
            .seesContact(newPhone)
            .seesSecret(secret)
            .seesWalletAddress(newWallet)
            .clickSubmitButton()

        robot
            .seesNavView()
            .seesProfileTitle("\n\n\n${newName}")

        activityScenario.close()
    }

}