package ru.home.swap.repository

import kotlinx.coroutines.flow.Flow
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service

interface IPersonRepository {
    fun createAccountAsFlow(person: PersonProfile): Flow<PersonRepository.Response<PersonProfile>>
    suspend fun createAccount(person: PersonProfile): PersonRepository.Response<PersonProfile>
    fun cacheAccountAsFlow(person: PersonProfile): Flow<PersonProfile>
    suspend fun cacheAccount(person: PersonProfile): PersonProfile
    fun getCachedAccount(): Flow<PersonProfile>
    fun getAccount(person: PersonProfile): Flow<PersonRepository.Response<PersonProfile>>
    fun addOffer(contact: String, secret: String, newService: Service):  Flow<PersonRepository.Response<PersonProfile>>
    fun addDemand(contact: String, secret: String, newService: Service): Flow<PersonRepository.Response<PersonProfile>>
    fun removeOffer(contact: String, secret: String, id: Long): Flow<PersonRepository.Response<PersonProfile>>
    fun removeDemand(contact: String, secret: String, id: Long): Flow<PersonRepository.Response<PersonProfile>>
    fun getContacts(contact: String, secret: String, serviceId: Long): Flow<PersonRepository.Response<PersonProfile>>
    fun cleanCachedAccount(): Flow<Any>

}