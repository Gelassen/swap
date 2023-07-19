package ru.home.swap.model

import android.content.Context
import ru.home.swap.R
import ru.home.swap.core.model.PersonProfile
import kotlin.collections.ArrayList

class DebugProfiles(val context: Context) {

    private val profiles: MutableCollection<PersonProfile> = ArrayList()

    init {
        profiles.add(
            PersonProfile(
                contact = "+79207008090",
                name = "Dmitry",
                secret = "onemoretime",
                userWalletAddress =  context.getString(ru.home.swap.wallet.R.string.first_account)
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79101000108",
                name = "Jane",
                secret = "catchme",
                userWalletAddress = context.getString(ru.home.swap.wallet.R.string.second_account)
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
                userWalletAddress = context.getString(ru.home.swap.wallet.R.string.third_account)
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79202000989",
                name = "Dmitry K.",
                secret = "expertise",
                userWalletAddress = context.getString(ru.home.swap.wallet.R.string.fourth_account)
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79201002030",
                name = "Jane Test",
                secret = "likevinniethepooh",
                userWalletAddress = "0x83c663a6787430897719F875f2a86Af445A92Bc2"
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