package ru.home.swap.providers

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Patterns
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.wallet.providers.WalletProvider
import java.text.SimpleDateFormat
import java.util.*

class PersonProvider(val isDemand: Boolean = false) {

    fun isAnyOfCredentialsEmpty(contact: String, secret: String) : Boolean {
        return TextUtils.isEmpty(contact)
                || TextUtils.isEmpty(secret)
    }

    fun isValidEmail(str: String) : Boolean {
        return !TextUtils.isEmpty(str)
                && Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

    fun isInputEmpty(input: String): Boolean {
        return input.isEmpty()
    }

    fun getPersonNameForViewItem(person: String): String {
        return "by ${person}"
    }

    fun getDateInHumanReadableFormat(date: Long): String {
        if (date == 0L)
            return "till date is not available"

        return "till ${SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            .format(Date(date))}"
    }

    fun getNameIfAvailable(name: String) : String {
        return if (TextUtils.isEmpty(name)) "Not available" else name
    }

    fun contactIsPhone(contact: String) : Boolean {
        return Patterns.PHONE.matcher(contact).matches()
    }

    fun contactIsEmail(contact: String) : Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(contact).matches()
    }

    fun inputIsEthereumAddress(input: String) : Boolean {
        return WalletProvider().isValidEthereumAddress(input)
    }

    fun prepareOfferForCaller(swapMatch: SwapMatch, callerId: String): String {
        val caller = callerId.toInt()
        return if (caller == swapMatch.userFirstProfileId) {
            swapMatch.userSecondServiceTitle
        } else if (caller == swapMatch.userSecondProfileId) {
            swapMatch.userFirstServiceTitle
        } else {
            // it is illegal state, but instead of throwing an exception handle it gracefully
            Logger.getInstance().d("Illegal state for SwapMatch profile id, but it would be handled gracefully")
            ""
        }
    }

    fun prepareOfferFromCaller(swapMatch: SwapMatch, callerId: String): String {
        val caller = callerId.toInt()
        val callerOffer: String = if (caller == swapMatch.userFirstProfileId) {
            swapMatch.userFirstServiceTitle
        } else if (caller == swapMatch.userSecondProfileId) {
            swapMatch.userSecondServiceTitle
        } else {
            // it is illegal state, but instead of throwing an exception handle it gracefully
            Logger.getInstance().d("Illegal state for SwapMatch profile id, but it would be handled gracefully")
            ""
        }
        return "in exchange for ${callerOffer}"
    }

    fun prepareOfferOwner(swapMatch: SwapMatch, callerId: String): String {
        val caller = callerId.toInt()
        return "by " + if (caller == swapMatch.userFirstProfileId) {
            swapMatch.userSecondProfileName
        } else if (caller == swapMatch.userSecondProfileId) {
            swapMatch.userFirstProfileName
        } else {
            // it is illegal state, but instead of throwing an exception handle it gracefully
            Logger.getInstance().d("Illegal state for SwapMatch profile id, but it would be handled gracefully")
            ""
        }
    }

//    fun prepareDemandForCaller(swapMatch: SwapMatch, callerId: String): String {
//        val caller = callerId.toInt()
//        return if (caller == swapMatch.userFirstProfileId) {
//            swapMatch.userSecondServiceTitle
//        } else if (caller == swapMatch.userSecondProfileId) {
//            swapMatch.userFirstServiceTitle
//        } else {
//            // it is illegal state, but instead of throwing an exception handle it gracefully
//            Logger.getInstance().d("Illegal state for SwapMatch profile id, but it would be handled gracefully")
//            ""
//        }
//    }

}