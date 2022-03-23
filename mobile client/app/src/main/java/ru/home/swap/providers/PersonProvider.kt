package ru.home.swap.providers

import android.text.TextUtils
import android.util.Patterns
import ru.home.swap.model.Person

class PersonProvider {

    fun isCredentialsEmpty(contact: String, secret: String) : Boolean {
        return TextUtils.isEmpty(contact)
                && TextUtils.isEmpty(secret)
    }

    fun isValidEmail(str: String) : Boolean {
        return !TextUtils.isEmpty(str)
                && Patterns.EMAIL_ADDRESS.matcher(str).matches()
    }

}