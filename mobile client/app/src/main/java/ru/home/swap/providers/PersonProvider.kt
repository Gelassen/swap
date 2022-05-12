package ru.home.swap.providers

import android.text.TextUtils
import android.util.Patterns

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

}