package ru.home.swap

import org.junit.Test

import org.junit.Assert.*
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
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
        val personOne = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )
        val personTwo = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )

        assertEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_bothHasSameId_equal() {
        val personOne = PersonProfile(
            id = 10L,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )
        val personTwo = PersonProfile(
            id = 10L,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )

        assertEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_bothHasDifferentId_notEqual() {
        val personOne = PersonProfile(
            id = 10L,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )
        val personTwo = PersonProfile(
            id = 9L,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_personsAreNotEqual_notEqual() {
        val personOne = PersonProfile(
            id = null,
            name = "Jane Ostin",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )
        val personTwo = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808081",
            secret = "no_money-no_honey",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_areNotEqualByService_notEqual() {
        val personOne = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )
        val personTwo = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Selling flowers",
                    Stubs.getDate(2022, 6, 18), listOf("Selling flowers")
                )
            ),
            demands = emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }

    @Test
    fun testTwoPersons_areNotEqualByAnotherService_notEqual() {
        val personOne = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 18), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )
        val personTwo = PersonProfile(
            id = null,
            name = "Jenny",
            contact = "808080",
            secret = "no_pain-no_gain",
            offers = listOf(
                Service(
                    0L,
                    "Software Engineering on Android",
                    Stubs.getDate(2022, 6, 10), listOf("Software Engineering")
                )
            ),
            demands = emptyList()
        )

        assertNotEquals(personOne, personTwo)
    }


}