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
import ru.home.swap.network.IApi
import ru.home.swap.stub.Stubs

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

    fun createAccount(person: PersonProfile): Flow<Response<String>> {
        return flow {
            Log.d(App.TAG, "[a] createProfile() call")
            val response = api.createProfile(person)
            Log.d(App.TAG, "[b] get profile response")
            if (response.isSuccessful) {
                Log.d(App.TAG, "[c] response is ok")
                val payload = response.body()!!
                emit(Response.Data(""))
            } else {
                Log.d(App.TAG, "[d] response is not ok")
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                Response.Error.Exception(ex)
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
            val response = api.getProfile()
            if (response.isSuccessful) {
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                Response.Error.Exception(ex)
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