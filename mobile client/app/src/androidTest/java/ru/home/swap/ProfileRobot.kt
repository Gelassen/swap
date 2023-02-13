package ru.home.swap

import android.view.KeyEvent
import android.widget.EditText
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.isDialog
import androidx.test.espresso.matcher.ViewMatchers
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.containsString

class ProfileRobot {

    fun seesName(name: String): ProfileRobot {
        onView(withId(R.id.name))
            .check(matches(ViewMatchers.isDisplayed()))
            .check(matches(withText(containsString(name))))
        return this
    }

    fun seesContact(input: String): ProfileRobot {
        onView(withId(R.id.contact_phone))
            .check(matches(ViewMatchers.isDisplayed()))
            .check(matches(withText(containsString(input))))
        return this
    }

    fun seesSecret(input: String): ProfileRobot {
        onView(withId(R.id.secret))
            .check(matches(ViewMatchers.isDisplayed()))
            .check(matches(withText(containsString(input))))
        return this
    }

    fun seesWalletAddress(input: String): ProfileRobot {
        onView(withId(R.id.wallet_address))
            .check(matches(ViewMatchers.isDisplayed()))
            .check(matches(withText(containsString(input))))
        return this
    }

    // main flow

    fun seesNavView(): ProfileRobot {
        onView(withId(R.id.navigation_bar))
            .check(matches(ViewMatchers.isDisplayed()))
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

    // Actions

    fun clickDebugButton() {
        onView(withId(R.id.debugBadge))
            .perform(ViewActions.click())
    }

    fun clickSubmitButton() {
        onView(withId(R.id.confirm))
            .perform(ViewActions.click())
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

}