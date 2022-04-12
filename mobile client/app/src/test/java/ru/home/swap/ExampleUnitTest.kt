package ru.home.swap

import org.junit.Test

import org.junit.Assert.*
import ru.home.swap.model.Person
import ru.home.swap.model.Service
import ru.home.swap.stub.Stubs

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testTwoPersons_personsAreEqual_equal() {
        val personOne = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            emptyList()
        )
        val personTwo = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            emptyList()
        )

        assertEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_personsAreNotEqual_notEqual() {
        val personOne = Person(
            "Jane Ostin",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            emptyList()
        )
        val personTwo = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_areNotEqualByService_notEqual() {
        val personOne = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            emptyList()
        )
        val personTwo = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Selling flowers",
                    Stubs.getDate(2022, 6, 18), listOf("Selling flowers")
                )
            ),
            emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_areNotEqualByAnotherService_notEqual() {
        val personOne = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            emptyList()
        )
        val personTwo = Person(
            "Jenny",
            listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 10), listOf("Software Engineering")
                )
            ),
            emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }


}