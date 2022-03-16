package ru.home.swap.model

data class Person(val name: String = "",
                  val offers: List<Service> = emptyList(),
                  val demands: List<Service> = emptyList())