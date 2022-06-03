package ru.home.swap.model

import com.google.gson.*

data class PersonProfile(
    val id: Long?,
    val contact: String,
    val secret: String,
    val name: String = "",
    val offers: List<Service> = emptyList(),
    val demands: List<Service> = emptyList(),
)

fun List<Service>.toJson(): String {
    return Gson().toJson(this)
}

