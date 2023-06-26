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
@RunWith(AndroidJUnit4::class)
class ProfileFragmentTest : BaseFragmentTest() {

    private val robot = ProfileRobot()

    @Test
    fun onAddNewOffer_validOfferAndEnvironmentIsOk_seesNewOffer() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        // TODO I haven't found yet a way to automatically generate this input data, so it requires to do it manually
        // TODO wallet account key is not injected by DI, it replaces manually and should be provided by DI
        val newName = "Dmitry ${LocalDateTime.now()}"
        val newPhone = TestAppApplication.FIRST_USER_CONTACT
        val secret = TestAppApplication.FIRST_USER_SECRET
        val newWallet = TestAppApplication.FIRST_USER_ADDRESS

        robot.enterCustomDebugData(newName, newPhone, secret, newWallet)
        robot
            .seesName(newName)
            .seesContact(newPhone)
            .seesSecret(secret)
            .seesWalletAddress(newWallet)
            .clickSubmitButton()

        val newOffer = "Custom Software Development ${System.currentTimeMillis()}"
        robot
            .seesNavView()
            /*.seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff*/
            .clickAddItemButton()
        robot
            .seesAddNewItemDialog()
            .enterNewOffer(newOffer)
        robot.clickSaveNewItemButton()
        robot.seesNewOffer(
            order = 0,
            newOfferText = newOffer
        )

        activityScenario.close()
    }

    @Test
    fun onAddNewDemand_validDemandAndEnvironmentIsOk_seesNewDemand() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)
        val newName = "Dmitry ${LocalDateTime.now()}"
        val newPhone = TestAppApplication.FIRST_USER_CONTACT
        val secret = TestAppApplication.FIRST_USER_SECRET
        val newWallet = TestAppApplication.FIRST_USER_ADDRESS

        robot.enterCustomDebugData(newName, newPhone, secret, newWallet)
        robot
            .seesName(newName)
            .seesContact(newPhone)
            .seesSecret(secret)
            .seesWalletAddress(newWallet)
            .clickSubmitButton()


        val newDemand = "Farmer Products ${System.currentTimeMillis()}"
        robot
            .seesNavView()
            /*.seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff*/
            .clickAddItemButton()
        robot
            .seesAddNewItemDialog()
            .enterNewDemand(newDemand)
        robot.clickSaveNewItemButton()
        robot.scrollToDemandsSection()
        robot.seesNewDemand(
            order = 0,
            newDemandText = newDemand
        )

        activityScenario.close()
    }

    @Test
    fun onRemoveOffer_defaultConditions_offerHasBeenRemovedFromProfile() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        val newName = "Dmitry ${LocalDateTime.now()}"
        val newPhone = TestAppApplication.FIRST_USER_CONTACT
        val secret = TestAppApplication.FIRST_USER_SECRET
        val newWallet = TestAppApplication.FIRST_USER_ADDRESS

        robot.enterCustomDebugData(newName, newPhone, secret, newWallet)
        robot
            .seesName(newName)
            .seesContact(newPhone)
            .seesSecret(secret)
            .seesWalletAddress(newWallet)
            .clickSubmitButton()

        val newOffer = "Custom Software Development ${System.currentTimeMillis()}"
        robot
            .seesNavView()
            /*.seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff*/
            .clickAddItemButton()
        robot
            .seesAddNewItemDialog()
            .enterNewOffer(newOffer)
        robot.clickSaveNewItemButton()
        robot
            .seesNewOffer(
                order = 0,
                newOfferText = newOffer
            )
            .clickRemoveOffer(
                order = 0,
                newOfferText = newOffer
            )
        robot.doesNotSeeAnItemInOffers(newOffer)

        activityScenario.close()
    }
    // TODO test remove demands
    @Test
    fun onRemoveDemand_defaultConditions_demandHasBeenRemovedFromProfile() {
        val activityScenario = ActivityScenario.launch(MainActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        val newName = "Dmitry ${LocalDateTime.now()}"
        val newPhone = TestAppApplication.FIRST_USER_CONTACT
        val secret = TestAppApplication.FIRST_USER_SECRET
        val newWallet = TestAppApplication.FIRST_USER_ADDRESS

        robot.enterCustomDebugData(newName, newPhone, secret, newWallet)
        robot
            .seesName(newName)
            .seesContact(newPhone)
            .seesSecret(secret)
            .seesWalletAddress(newWallet)
            .clickSubmitButton()

        // profile screen
        val newDemand = "Farmer products ${System.currentTimeMillis()}"
        robot
            .seesNavView()
            /*.seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff*/
            .clickAddItemButton()
        robot
            .seesAddNewItemDialog()
            .enterNewDemand(newDemand)
        robot.clickSaveNewItemButton()
        robot.scrollToDemandsSection()
        robot
            .seesNewDemand(
            order = 0,
            newDemandText = newDemand
        )
            .clickRemoveDemand(
            order = 0,
            newDemandText = newDemand
        )
        robot.doesNotSeeAnItemInDemands(newDemand)

        activityScenario.close()
    }
    // TODO test matching offers
    // TODO test matching demands

}