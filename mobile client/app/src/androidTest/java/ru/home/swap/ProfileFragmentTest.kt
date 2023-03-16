package ru.home.swap

import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import org.junit.Ignore
import org.junit.Test
import org.junit.runner.RunWith
import ru.home.swap.robots.ProfileRobot
import ru.home.swap.utils.monitorActivity
import java.time.LocalDateTime

@LargeTest
@Ignore("There is an idling resource issue. Current design split tx minting and server side operations " +
        "into separate flows and at I haven't found yet a way to keep operations in both flows as " +
        "a single resource")
@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest : BaseFragmentTest() {

    private val robot = ProfileRobot()

    @Test
    fun onAddNewOffer_validOfferAndEnvironmentIsOk_seesNewOffer() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TODO I haven't found yet a way to automatically generate this input data, so it requires to do it manually
        // TODO wallet account key is not injected by DI, it replaces manually and should be provided by DI
        val timeExtension: String = LocalDateTime.now().toString()
        val newName = "911 ${timeExtension}"
        val newPhone = "John"
        val secret = "onemoretime"
        val newWallet = "0x3DB851dFd07a1665F95E58cE327d33f2D8E8a030" // 911's (3rd) wallet


        robot.enterCustomDebugData(newName, newPhone, secret, newWallet)
        robot
            .seesName(newName)
            .seesContact(newPhone)
            .seesSecret(secret)
            .seesWalletAddress(newWallet)
            .clickSubmitButton()

        robot
            .seesNavView()
//            .seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff
            .clickAddItemButton()

        val newOffer = "Custom Software Development ${System.currentTimeMillis()}"
        robot
            .seesAddNewItemDialog()
            .enterNewOffer(newOffer)

        robot.clickSaveNewItemButton()

        robot.seesNewOffer(order = 0, newOfferText = newOffer)

        activityScenario.close()
    }

}