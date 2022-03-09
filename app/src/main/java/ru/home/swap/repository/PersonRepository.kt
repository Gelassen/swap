package ru.home.swap.repository

import android.util.Log
import ru.home.swap.App
import ru.home.swap.convertors.PersonConvertor
import ru.home.swap.model.Person
import ru.home.swap.model.PersonView
import ru.home.swap.stub.Stubs
import java.util.*

class PersonRepository {

    fun getPersons(): List<Person> {
        return Stubs.generatePersons()
    }

    private val convertor: PersonConvertor = PersonConvertor()

    fun getOffers(): List<PersonView> {
        val result = mutableListOf<PersonView>()
        for (person in getPersons()) {
            result.addAll(convertor.toView(person))
        }
        return result
    }
}