package ru.home.swap

import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.assertion.ViewAssertions.matches
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

    // Actions

    fun clickDebugButton() {
        onView(withId(R.id.debugBadge))
            .perform(ViewActions.click())
    }

    fun clickSubmitButton() {
        onView(withId(R.id.confirm))
            .perform(ViewActions.click())
    }

}