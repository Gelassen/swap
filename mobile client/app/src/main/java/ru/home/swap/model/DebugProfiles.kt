package ru.home.swap.model

import android.content.Context
import ru.home.swap.core.R
import ru.home.swap.core.model.PersonProfile
import java.util.*
import kotlin.collections.ArrayList

class DebugProfiles(val context: Context) {

    private val profiles: MutableCollection<PersonProfile> = ArrayList()

    init {
        profiles.add(
            PersonProfile(
                contact = "+79207008090",
                name = "Dmitry",
                secret = "onemoretime",
                userWalletAddress =  context.getString(com.example.wallet.R.string.my_account)
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79101000108",
                name = "Jane",
                secret = "catchme",
                userWalletAddress = context.getString(com.example.wallet.R.string.my_account_2)
            )
        )
        profiles.add(
            PersonProfile(
                contact = "John",
                name = "911",
                secret = "007"
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79201000989",
                name = "Julia N.",
                secret = "wireframes",
                userWalletAddress = "0x60476837d7ebd4bd9d543888bec87e48648fa321"
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79202000989",
                name = "Dmitry K.",
                secret = "expertise",
                userWalletAddress = "0xa24c5c43bcf8efe680f81cfb1375b4fa97f2a43f"
            )
        )
    }

    private var next: Int = 0

    fun next() : PersonProfile {
        val result = next(next)
        next++
        return result
    }

    fun next(index: Int) : PersonProfile {
        if (index >= profiles.size - 1) {
            next = -1 // start from the beginning
            //throw IllegalArgumentException("There is no such data. Did you pass correct index?")
        }
        return profiles.elementAt(index)
    }
}