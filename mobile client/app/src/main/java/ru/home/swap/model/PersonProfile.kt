package ru.home.swap.model

data class PersonProfile(
    val id: Long?,
    val contact: String,
    val secret: String,
    val name: String = "",
    val offers: List<Service> = emptyList(),
    val demands: List<Service> = emptyList(),
)