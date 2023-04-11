package ru.home.swap.repository

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import ru.home.swap.R
import ru.home.swap.core.converters.ApiResponseConverter
import ru.home.swap.core.extensions.attachIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.core.network.IApi
import ru.home.swap.core.network.Response
import ru.home.swap.utils.AppCredentials
import java.net.HttpURLConnection

class PersonRepository(val api: IApi, val cache: Cache, val context: Context): IPersonRepository {

    private var logger: Logger = Logger.getInstance()

    override fun createAccountAsFlow(person: PersonProfile): Flow<Response<PersonProfile>> {
        return flow {
            logger.d( "[a] createProfile() call")
            val response = api.createProfile(
                credentials = AppCredentials.basic(person.contact, person.secret),
                person = person
            )
            logger.d( "[b] get profile response")
            if (response.isSuccessful) {
                logger.d( "[c] response is ok")
                var payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                logger.d( "[d] response is not ok")
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                emit(Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}"))
            }
        }
            .catch { ex ->
                logger.e( "Failed to create account and process result on network layer", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override suspend fun createAccount(person: PersonProfile): Response<PersonProfile> {
        lateinit var result: Response<PersonProfile>
        try {
            logger.d( "[a] createProfile() call")
            val response = api.createProfile(
                credentials = AppCredentials.basic(person.contact, person.secret),
                person = person
            )
            logger.d( "[b] get profile response")
            if (response.isSuccessful) {
                logger.d( "[c] response is ok")
                var payload = response.body()!!
                result = Response.Data(payload.payload)
            } else {
                logger.d( "[d] response is not ok")
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                result = Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}")
            }
        } catch (ex: Exception) {
            logger.e("Failed to create account and process result on network layer", ex)
            result = Response.Error.Exception(ex)
        }
        return result
    }

    override fun cacheAccountAsFlow(person: PersonProfile): Flow<PersonProfile> {
        return flow {
            cache.saveProfile(person)
            emit(person)
        }
    }

    override suspend fun cacheAccount(person: PersonProfile): PersonProfile {
        cache.saveProfile(person)
        return person
    }

    override fun getCachedAccount(): Flow<PersonProfile> {
        return cache.getProfile()
    }

    override fun getAccount(person: PersonProfile): Flow<Response<PersonProfile>> {
        return flow {
            logger.d( "[a] getProfile() call")
            val response = api.getProfile(
                credentials = AppCredentials.basic(person.contact, person.secret)
            )
            if (response.isSuccessful) {
                logger.d( "[b1] get body and emit")
                if (response.code() == HttpURLConnection.HTTP_NO_CONTENT) {
                    emit(Response.Error.Message(context.getString(R.string.error_no_data_for_your_params)))
                } else {
                    val payload = response.body()!!
                    emit(Response.Data(payload.payload))
                }
            } else {
                logger.d( "[b2] get error and emit")
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                logger.d("[c] get an exception")
                logger.e( "Exception on getAccount() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override fun addOffer(contact: String, secret: String, newService: Service):  Flow<Response<PersonProfile>> {
        return flow {
            logger.d( "[add offer] start")
            val response = api.addOffer(
                credentials = AppCredentials.basic(contact, secret),
                service = newService
            )
            /*
            * architecture decision is to use server as a single source of truth and keep operations
            * with server resources atomic at the same time
            * */
            if (response.isSuccessful) {
                logger.d( "[add offer] success case")
                val profileResponse = api.getProfile(
                    credentials = AppCredentials.basic(contact, secret)
                )
                val payload = profileResponse.body()!!
                emit(Response.Data(payload.payload))
            } else {
                logger.d( "[add offer] error case")
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                emit(Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}"))
            }
            logger.d( "[add offer] end")
        }
            .attachIdlingResource()
            .catch { ex ->
                logger.e( "Exception on addOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override fun addDemand(contact: String, secret: String, newService: Service): Flow<Response<PersonProfile>> {
        return flow {
            val response = api.addDemand(
                credentials = AppCredentials.basic(contact, secret),
                service = newService
            )
            if (response.isSuccessful) {
                logger.d( "[add demand] success case")
                val profileResponse = api.getProfile(
                    credentials = AppCredentials.basic(contact, secret)
                )
                val payload = profileResponse.body()!!
                emit(Response.Data(payload.payload))
            } else {
                val errorPayload = ApiResponseConverter().toDomain(response.errorBody()?.string()!!);
                emit(Response.Error.Message("${response.message()}:\n\n${errorPayload.payload}"))
            }
        }
            .catch { ex ->
                logger.e( "Exception on addDemand() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override fun removeOffer(contact: String, secret: String, id: Long): Flow<Response<PersonProfile>> {
        return flow {
            val response = api.removeOffer(
                credentials = AppCredentials.basic(contact, secret),
                serviceId = id
            )
            if (response.isSuccessful) {
                logger.d( "[delete offer] success case")
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
                logger.e( "Exception on removeOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override fun removeDemand(contact: String, secret: String, id: Long): Flow<Response<PersonProfile>>  {
        return flow {
            val response = api.removeDemand(
                credentials = AppCredentials.basic(contact, secret),
                serviceId = id
            )
            if (response.isSuccessful) {
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
                logger.e( "Exception on removeOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override fun getContacts(contact: String, secret: String, serviceId: Long): Flow<Response<PersonProfile>> {
        return flow {
            val response = api.getContacts(
                credentials = AppCredentials.basic(contact, secret),
                serviceId = serviceId
            )
            if (response.isSuccessful) {
                val payload = response.body()!!
                emit(Response.Data(payload.payload))
            } else {
                emit(Response.Error.Message(response.message()))
            }
        }
            .catch { ex ->
                logger.e( "Exception on removeOffer() call", ex)
                emit(Response.Error.Exception(ex))
            }
    }

    override fun cleanCachedAccount(): Flow<Any> {
        return cache.cleanProfile()
    }

}