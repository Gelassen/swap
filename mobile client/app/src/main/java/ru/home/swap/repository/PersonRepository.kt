package ru.home.swap.repository

import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ru.home.swap.App
import ru.home.swap.convertors.PersonConvertor
import ru.home.swap.model.Person
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.PersonView
import ru.home.swap.model.Service
import ru.home.swap.network.IApi
import ru.home.swap.network.model.EmptyPayload
import ru.home.swap.network.model.ProfileResponse
import ru.home.swap.stub.Stubs
import ru.home.swap.utils.AppCredentials
import java.lang.RuntimeException

class PersonRepository(val api: IApi, val cache: Cache) {

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

    fun createAccount(person: PersonProfile): Flow<Response<EmptyPayload>> {
        return flow {
            Log.d(App.TAG, "[a] createProfile() call")
            val response = api.createProfile(person)
            Log.d(App.TAG, "[b] get profile response")
            if (response.isSuccessful) {
                Log.d(App.TAG, "[c] response is ok")
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                Log.d(App.TAG, "[d] response is not ok")
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                emit(Response.Error.Exception(ex))
            }
    }

    fun cacheAccount(person: PersonProfile): Flow<PersonProfile> {
        return flow {
            cache.saveProfile(person)
            emit(person)
        }
    }

    fun getCachedAccount(): Flow<PersonProfile> {
        return cache.getProfile()
    }

    fun getAccount(person: PersonProfile): Flow<Response<PersonProfile>> {
        return flow {
            Log.d(App.TAG, "[a] getProfile() call")
            val response = api.getProfile()
            if (response.isSuccessful) {
                Log.d(App.TAG, "[b1] get body and emit")
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                Log.d(App.TAG, "[b2] get error and emit")
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                Log.d(App.TAG, "[c] get an exception")
                Log.e(App.TAG, "Exception on getAccount() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    fun addOffer(contact: String, secret: String, newService: Service):  Flow<Response<PersonProfile>> {
        // TODO mock response for this web request and add processing it on the viewmodel layer
        return flow {
            val response = api.addOffer(
                credentials = AppCredentials.basic(contact, secret),
                service = newService
            )
            if (response.isSuccessful) {
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                Log.e(App.TAG, "Exception on addOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    sealed class Response<out T: Any> {
        data class Data<out T: Any>(val data: T): Response<T>()
        sealed class Error: Response<Nothing>() {
            data class Exception(val error: Throwable): Error()
            data class Message(val msg: String): Error()
        }
        data class Loading<Boolean>(val isLoading: Boolean): Response<kotlin.Boolean>()
    }
}