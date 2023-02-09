package ru.home.swap.core.model

import android.app.Person
import java.lang.IllegalArgumentException
import java.util.*
import kotlin.collections.ArrayList

class DebugProfiles {

    private val profiles: MutableCollection<PersonProfile> = ArrayList()

    init {
        profiles.add(
            PersonProfile(
                contact = "+79207008090",
                name = "Dmitry",
                secret = "onemoretime",
                userWalletAddress = "0x62F8DC8a5c80db6e8FCc042f0cC54a298F8F2FFd"
            )
        )
        profiles.add(
            PersonProfile(
                contact = "+79101000108",
                name = "Jane",
                secret = "catchme"
            )
        )
        profiles.add(
            PersonProfile(
                contact = "John",
                name = "911",
                secret = "007"
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
        if (index >= profiles.size) {
            throw IllegalArgumentException("There is no such data. Did you pass correct index?")
        }
        return profiles.elementAt(index)
    }
}