package ru.home.swap.repository

import kotlinx.coroutines.flow.Flow
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.core.model.SwapMatch
import ru.home.swap.core.network.Response

interface IPersonRepository {
    fun createAccountAsFlow(person: PersonProfile): Flow<Response<PersonProfile>>
    suspend fun createAccount(person: PersonProfile): Response<PersonProfile>
    fun cacheAccountAsFlow(person: PersonProfile): Flow<PersonProfile>
    suspend fun cacheAccount(person: PersonProfile): PersonProfile
    fun getCachedAccount(): Flow<PersonProfile>
    fun getAccount(person: PersonProfile): Flow<Response<PersonProfile>>
    fun addOffer(contact: String, secret: String, newService: Service):  Flow<Response<PersonProfile>>
    fun addDemand(contact: String, secret: String, newService: Service): Flow<Response<PersonProfile>>
    fun removeOffer(contact: String, secret: String, id: Long): Flow<Response<PersonProfile>>
    fun removeDemand(contact: String, secret: String, id: Long): Flow<Response<PersonProfile>>
    fun getContacts(contact: String, secret: String, serviceId: Long): Flow<Response<PersonProfile>>
    suspend fun getMatches(contact: String, secret: String): Response<List<SwapMatch>>
    @Deprecated("Rewrite on single call within suspend function")
    fun cleanCachedAccount(): Flow<Any>
}