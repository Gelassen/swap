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

    /**
     * Several options here:
     * 1. Manually pre-configurate test user here and run single test here
     * 2. Implement /logout option and setup 2nd user over test
     * 3. Implement a couple of grade tasks which will pre-configurate test environment
     *    including 2nd user and run test against it.
     * */
    @Ignore("By undiscovered reason open an 'chains' or any other tab doesn't  work smoothly as a part of integration test, " +
            "open it when the rest code has been commented out works as expected")
    @Test
    fun onOpenOffersScreen_matchesHasBeenSetup_singleOfferIsShown() {
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

        // profile screen: offers
        val newOffer = "Custom Software Development"  //${System.currentTimeMillis()}
        robot
            .seesNavView()
            .seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff
            .clickAddItemButton()
        robot
            .seesAddNewItemDialog()
            .enterNewOffer(newOffer)
        robot.clickSaveNewItemButton()
        robot.seesNewOffer(
            order = 0,
            newOfferText = newOffer
        )
        // profile screen: demands
        val newDemand = "Farmer products"
        robot
            .seesNavView()
            .seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff
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

        // offers screen
        robot.clickChainsNavigationTab()
        robot.seesMatch(/*newDemand*/"Farmer products", R.id.txList)

        activityScenario.close()
    }

    // TODO test matching demands
    @Test
    fun onOpenDemandsScreen_matchesHasBeenSetup_singleDemandIsShown() {
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

        // profile screen: offers
        val newOffer = "Custom Software Development"  //${System.currentTimeMillis()}
        robot
            .seesNavView()
            .seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff
            .clickAddItemButton()
        robot
            .seesAddNewItemDialog()
            .enterNewOffer(newOffer)
        robot.clickSaveNewItemButton()
        robot.seesNewOffer(
            order = 0,
            newOfferText = newOffer
        )
        // profile screen: demands
        val newDemand = "Farmer products"
        robot
            .seesNavView()
            .seesProfileTitle("\n\n\n${newName}") // issue with time -- there is a strange diff
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

        // offers screen
        robot.clickDemandsNavigationTab()
        robot.seesMatch(newOffer)

        activityScenario.close()
    }

}