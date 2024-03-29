package ru.home.swap.core.model

import com.google.gson.*

data class PersonProfile(
    var id: Long? = -1L,
    var contact: String = "",
    var secret: String = "",
    var name: String = "",
    var userWalletAddress: String = "",
    var offers: List<Service> = emptyList(),
    var demands: List<Service> = emptyList(),
    ) : IPayload {

    constructor(profile: PersonProfile): this(
        profile.id, profile.contact,
        profile.secret, profile.name,
        profile.userWalletAddress, profile.offers,
        profile.demands) { }

    override fun toJson(): String {
        return Gson().toJson(this)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as PersonProfile

        if (id != other.id) return false
        if (contact != other.contact) return false
        if (secret != other.secret) return false
        if (name != other.name) return false
        if (offers != other.offers) return false
        if (demands != other.demands) return false
        if (userWalletAddress != other.userWalletAddress) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + contact.hashCode()
        result = 31 * result + secret.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + offers.hashCode()
        result = 31 * result + demands.hashCode()
        result = 31 * result + userWalletAddress.hashCode()
        return result
    }

    fun fromJson(json: String): PersonProfile {
        return Gson().fromJson(json, PersonProfile::class.java)
    }
}


