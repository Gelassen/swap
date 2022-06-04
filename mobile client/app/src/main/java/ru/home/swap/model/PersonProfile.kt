package ru.home.swap.model

import com.google.gson.*

data class PersonProfile(
    val id: Long? = -1L,
    var contact: String = "",
    var secret: String = "",
    var name: String = "",
    var offers: List<Service> = emptyList(),
    var demands: List<Service> = emptyList(),
)

fun List<Service>.toJson(): String {
    return Gson().toJson(this)
}

