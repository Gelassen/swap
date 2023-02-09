package ru.home.swap.providers

import android.content.Context
import android.telephony.PhoneNumberUtils
import android.text.TextUtils
import android.util.Patterns
import ru.home.swap.wallet.providers.WalletProvider
import java.text.SimpleDateFormat
import java.util.*

class PersonProvider {

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
        return android.util.Patterns.PHONE.matcher(contact).matches()
    }

    fun contactIsEmail(contact: String) : Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(contact).matches()
    }

    fun inputIsEthereumAddress(input: String) : Boolean {
        return WalletProvider().isValidEthereumAddress(input)
    }

}