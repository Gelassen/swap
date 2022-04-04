package ru.home.swap.stub

import ru.home.swap.model.Person
import ru.home.swap.model.Service
import ru.home.swap.stub.Stubs
import java.util.*

object Stubs {
    fun generatePersons(): List<Person> {
        val result: MutableList<Person> = ArrayList()
        result.add(
            Person(
                "Jow Dow", emptyList(), emptyList()
            )
        )
        result.add(
            Person(
                "Jane Ostin", listOf(
                    Service(
                        0L,
                        "Software Engineering on Android",
                        getDate(2022, 6, 18), listOf("Software Engineering")
                    )
                ), emptyList()
            )
        )
        result.add(
            Person(
                "Garry Truman",
                trumanOffers,
                trumanDemands
            )
        )
        return result
    }

    private val trumanDemands: List<Service>
        private get() {
            val result: MutableList<Service> = ArrayList()
            result.add(
                Service(
                    0L,
                    "Software Engineering",
                    getDate(2022, 12, 31), listOf("Software Engineering")
                )
            )
            return result
        }
    private val trumanOffers: List<Service>
        private get() {
            val result: MutableList<Service> = ArrayList()
            result.add(
                Service(
                    0L,
                    "Fresh apples",
                    getDate(2022, 10, 18), listOf("apples")
                )
            )
            result.add(
                Service(
                    0L,
                    "Potatoes",
                    getDate(2022, 10, 18), listOf("potatoes")
                )
            )
            return result
        }

    /*private*/ fun getDate(year: Int, month: Int, day: Int): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = 0L
        cal[year, month] = day
        return cal.timeInMillis
    }
}