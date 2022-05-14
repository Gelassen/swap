package ru.home.swap.repository

import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ru.home.swap.App
import ru.home.swap.R
import ru.home.swap.converters.ApiResponseConverter
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.Service
import ru.home.swap.network.IApi
import ru.home.swap.network.model.EmptyPayload
import ru.home.swap.network.model.ProfileResponse
import ru.home.swap.utils.AppCredentials
import java.net.HttpURLConnection

class PersonRepository(val api: IApi, val cache: Cache, val context: Context) {

    fun createAccount(person: PersonProfile): Flow<Response<EmptyPayload>> {
        return flow {
            Log.d(App.TAG, "[a] createProfile() call")
            val response = api.createProfile(
                credentials = AppCredentials.basic(person.contact, person.secret),
                person = person
            )
            Log.d(App.TAG, "[b] get profile response")
            if (response.isSuccessful) {
                Log.d(App.TAG, "[c] response is ok")
                var payload = response.body()!!
                payload = if (payload.payload == null) ProfileResponse(EmptyPayload()) else payload
                emit(Response.Data(payload.payload))
            } else {
                Log.d(App.TAG, "[d] response is not ok")
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                emit(Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}"))
            }
        }
            .catch { ex ->
                Log.e(App.TAG, "Failed to create account and process result on network layer", ex)
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
            val response = api.getProfile(
                credentials = AppCredentials.basic(person.contact, person.secret)
            )
            if (response.isSuccessful) {
                Log.d(App.TAG, "[b1] get body and emit")
                if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                    emit(Response.Error.Message(context.getString(R.string.error_no_data_for_your_params)))
                } else {
                    val payload = response.body()!!
                    emit(Response.Data(payload.payload))
                }
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
        return flow {
            Log.d(App.TAG, "[add offer] start")
            val response = api.addOffer(
                credentials = AppCredentials.basic(contact, secret),
                service = newService
            )
            /*
            * architecture decision is to use server as a single source of truth and keep operations
            * with server resources atomic at the same time
            * */
            if (response.isSuccessful) {
                Log.d(App.TAG, "[add offer] success case")
                val profileResponse = api.getProfile(
                    credentials = AppCredentials.basic(contact, secret)
                )
                val payload = profileResponse.body()!!
                emit(Response.Data(payload.payload))
            } else {
                Log.d(App.TAG, "[add offer] error case")
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                emit(Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}"))
            }
            Log.d(App.TAG, "[add offer] end")
        }
            .catch { ex ->
                Log.e(App.TAG, "Exception on addOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    fun addDemand(contact: String, secret: String, newService: Service): Flow<Response<PersonProfile>> {
        return flow {
            val response = api.addDemand(
                credentials = AppCredentials.basic(contact, secret),
                service = newService
            )
            if (response.isSuccessful) {
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                emit(Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}"))
            }
        }
            .catch { ex ->
                Log.e(App.TAG, "Exception on addDemand() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    fun removeOffer(contact: String, secret: String, id: Long): Flow<Response<PersonProfile>> {
        return flow {
            val response = api.removeOffer(
                credentials = AppCredentials.basic(contact, secret),
                serviceId = id
            )
            if (response.isSuccessful) {
                Log.d(App.TAG, "[delete offer] success case")
                val profileResponse = api.getProfile(
                    credentials = AppCredentials.basic(contact, secret)
                )
                val payload = profileResponse.body()!!
                emit(Response.Data(payload.payload))
            } else {
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                Log.e(App.TAG, "Exception on removeOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    fun removeDemand(contact: String, secret: String, id: Long): Flow<Response<PersonProfile>>  {
        return flow {
            val response = api.removeDemand(
                credentials = AppCredentials.basic(contact, secret),
                serviceId = id
            )
            if (response.isSuccessful) {
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                Log.e(App.TAG, "Exception on removeOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    fun cleanCachedAccount(): Flow<Any> {
        return cache.cleanProfile()
    }

    sealed class Response<out T: Any> {
        data class Data<out T: Any>(val data: T): Response<T>()
        sealed class Error: Response<Nothing>() {
            data class Exception(val error: Throwable): Error()
            data class Message(val msg: String): Error()
        }
        /*data class Loading<Boolean>(val isLoading: Boolean): Response<kotlin.Boolean>()*/
    }
}