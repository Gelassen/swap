package ru.home.swap

import org.junit.Assert
import org.junit.Test
import ru.home.swap.model.Service
import ru.home.swap.ui.profile.Model
import ru.home.swap.ui.profile.StateFlag

class ModelUnitTest {

    @Test
    fun compareModel_modelsAreEqual_theSameModel() {
        val offers = mutableListOf(Service(0L,"cooking", 10L, listOf<String>()))
        val model = Model(
            contact = "Jennifer@list.ru",
            secret = "",
            name = "Jennifer",
            offers = offers,
            demands = mutableListOf(),
            isLoading = false,
            errors = listOf(),
            status = StateFlag.NONE
        )

        val theSameModel = model.copy()

        Assert.assertEquals(model, theSameModel)
    }

    @Test
    fun compareModel_modelsAreDifferent_theDifferentModel() {
        val offers = mutableListOf(Service(0L,"Forging steel", 10L, listOf<String>()))
        val model = Model(
            contact = "Jennifer@list.ru",
            secret = "",
            name = "Jennifer",
            offers = offers,
            demands = mutableListOf(),
            isLoading = false,
            errors = listOf(),
            status = StateFlag.NONE
        )

        val updatedOffers = mutableListOf<Service>()
        updatedOffers.addAll(offers)
        updatedOffers.add(Service(0L,"Carpenting", 10L, listOf()))
        val theSameModel = model.copy(offers = updatedOffers)

        Assert.assertNotEquals(model, theSameModel)
    }

    @Test
    fun compareModel_modelsAreDifferentTheSameObject_theDifferentModel() {
        val offers = mutableListOf(Service(0L,"Forging steel", 10L, listOf<String>()))
        val model = Model(
            contact = "Jennifer@list.ru",
            secret = "",
            name = "Jennifer",
            offers = offers,
            demands = mutableListOf(),
            isLoading = false,
            errors = listOf(),
            status = StateFlag.NONE
        )

        val offersTmp = model.offers
        offersTmp.add(Service(0L,"Carpenting", 10L, listOf()))
        val theSameModel = model.copy(offers = offersTmp)

        Assert.assertNotEquals(model, theSameModel)
    }

}