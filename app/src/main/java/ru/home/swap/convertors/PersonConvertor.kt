package ru.home.swap.convertors

import ru.home.swap.model.Person
import ru.home.swap.model.PersonView
import java.util.*

class PersonConvertor {

    // TODO parse service in demands too
    fun toView(person: Person): List<PersonView> {
        val result = mutableListOf<PersonView>()
        for (service in person.offers) {
            result.add(
                PersonView(
                    person.name,
                    service.title,
                    Date(service.date).toString()
                )
            )
        }
        return result
    }
}