package ru.home.swap.robots

import android.view.KeyEvent
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.scrollTo
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.*
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.core.StringContains
import ru.home.swap.R
import ru.home.swap.utils.RecyclerViewMatcher
import ru.home.swap.utils.Utils.atPositionByTitle

/**
 * For current size of application it is a reasonable to keep a single robot for all screens
 * and use case scenarios. With time app might grow further, in such case consider refactor
 * this logic into multiple robots per screen or business scenario use cases.
 * */
class ProfileRobot {

    fun seesName(name: String): ProfileRobot {
        onView(withId(R.id.name))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString(name))))
        return this
    }

    fun seesContact(input: String): ProfileRobot {
        onView(withId(R.id.contact_phone))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString(input))))
        return this
    }

    fun seesSecret(input: String): ProfileRobot {
        onView(withId(R.id.secret))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString(input))))
        return this
    }

    fun seesWalletAddress(input: String): ProfileRobot {
        onView(withId(R.id.wallet_address))
            .check(matches(isDisplayed()))
            .check(matches(withText(containsString(input))))
        return this
    }

    // main flow

    fun seesNavView(): ProfileRobot {
        onView(withId(R.id.navigation_bar))
            .check(matches(isDisplayed()))
        return this
    }

    fun seesProfileTitle(input: String): ProfileRobot {
        onView(withId(R.id.profile_title))
            .check(matches(withText(input)))
        onView(withId(R.id.offers_title))
            .check(matches(withText(R.string.title_offers)))
        onView(withId(R.id.demands_title))
            .check(matches(withText(R.string.demands_title)))
        return this
    }

    fun seesErrorDialog(input: String): ProfileRobot {
        onView(withId(android.R.id.message)/*withText(R.string.default_error_title_dialog)*/)
            .inRoot(isDialog())
            .check(matches(withText(input)))
        return this
    }

    // Profile screen

    fun seesAddNewItemDialog(): ProfileRobot {
        onView(withId(R.id.offer_option))
            .check(matches(isDisplayed()))
        onView(withId(R.id.demand_option))
            .check(matches(isDisplayed()))
        onView(withId(R.id.save))
            .check(matches(isDisplayed()))
        return this
    }

    fun seesNewOffer(order: Int, newOfferText: String): ProfileRobot {
        onView(withId(R.id.offers_list))
            .check(matches(isDisplayed()))
        onView(withId(R.id.offers_list))
            .check(matches(
                atPositionByTitle(
                    position = order,
                    itemMatcher = withText(StringContains.containsString(newOfferText)),
                    title = R.id.text_service)
                )
            )
        return this
    }

    fun seesNewDemand(order: Int, newDemandText: String): ProfileRobot {
        onView(withId(R.id.demands_list))
            .check(matches(isDisplayed()))
        onView(withId(R.id.demands_list))
            .check(matches(
                atPositionByTitle(
                    position = order,
                    itemMatcher = withText(StringContains.containsString(newDemandText)),
                    title = R.id.text_service
                )
            ))
        return this
    }

    fun doesNotSeeAnItemInOffers(item: String): ProfileRobot {
        onView(withId(R.id.offers_list))
            .check(matches(RecyclerViewMatcher(R.id.offers_list).hasNotAnItem(item)))
        return this
    }

    fun doesNotSeeAnItemInDemands(item: String): ProfileRobot {
        onView(withId(R.id.demands_list))
            .check(matches(RecyclerViewMatcher(R.id.demands_list).hasNotAnItem(item)))
        return this
    }

    fun seesMatch(myDemand: String, listId: Int = R.id.offers_list) {
        onView(withId(listId))
            .check(matches(
                RecyclerViewMatcher(listId)
                    .hasItemWithRequestedText(
                        text =  myDemand,
                        occurrences = 1
                    )
            ))
    }

    // Actions

    fun clickDebugButton() {
        onView(withId(R.id.debugBadge))
            .perform(ViewActions.click())
    }

    fun clickSubmitButton() {
        onView(withId(R.id.confirm))
            .perform(ViewActions.click())
    }

    fun clickAddItemButton() {
        onView(withId(R.id.fab))
            .perform(ViewActions.click())
    }

    fun enterNewOffer(query: String) {
        onView(withId(R.id.editText))
            .perform(ViewActions.replaceText(query))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))
        /*.perform(typeText(query), pressKey(KeyEvent.KEYCODE_ENTER)) // Unicode chars is not supported here */
    }

    fun enterNewDemand(query: String) {
        onView(withId(R.id.demand_option))
            .perform(click())
        onView(withId(R.id.editText))
            .perform(ViewActions.replaceText(query))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))
        /*.perform(typeText(query), pressKey(KeyEvent.KEYCODE_ENTER)) // Unicode chars is not supported here */
    }

    fun clickSaveNewItemButton() {
        onView(withId(R.id.save))
            .perform(click())
    }

    fun enterCustomDebugData(name: String, phone: String, secret: String, wallet: String) {
        onView(withId(R.id.name))
            .perform(ViewActions.replaceText(name))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))
        /*.perform(typeText(query), pressKey(KeyEvent.KEYCODE_ENTER)) // Unicode chars is not supported here */
        onView(withId(R.id.contact_phone))
            .perform(ViewActions.replaceText(phone))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.secret))
            .perform(ViewActions.replaceText(secret))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))
        onView(withId(R.id.wallet_address))
            .perform(ViewActions.replaceText(wallet))
            .perform(ViewActions.pressKey(KeyEvent.KEYCODE_ENTER))
    }

    fun scrollToDemandsSection() {
        onView(withId(R.id.demands_list))
            .perform(scrollTo(), click())
    }

    fun clickRemoveOffer(order: Int, newOfferText: String) {
        seesNewOffer(order, newOfferText)
        onView(RecyclerViewMatcher(R.id.offers_list).atPositionOnView(order, R.id.remove_service))
            .perform(click())
    }

    fun clickRemoveDemand(order: Int, newDemandText: String) {
        seesNewDemand(order, newDemandText)
        onView(RecyclerViewMatcher(R.id.demands_list).atPositionOnView(order, R.id.remove_service))
            .perform(click())
    }

    fun clickChainsNavigationTab() {
        onView(withId(R.id.navigation_chains))
            .perform(click())
    }

    fun clickDemandsNavigationTab() {
        onView(withId(R.id.navigation_demands))
            .perform(click())
    }

}