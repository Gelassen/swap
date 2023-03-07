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
class ProfileFragmentTest : BaseFragmentTest() {

    private val robot = ProfileRobot()

    @Test
    fun onAddNewOffer_validOfferAndEnvironmentIsOk_seesNewOffer() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TODO I haven't found yet a way to automatically generate this input data, so it requires to do it manually
        val newName = "Dmitry ${LocalDateTime.now()}"
        val newPhone = "+7920701803"
        val secret = "onemoretime"
        val newWallet = "0x0018DC8a5c80db6e8FCc042f0cC54a298F8F2006"

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