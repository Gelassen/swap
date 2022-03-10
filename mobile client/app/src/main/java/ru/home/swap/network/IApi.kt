package ru.home.swap.network

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import ru.home.swap.model.Person
import ru.home.swap.model.PersonProfile
import ru.home.swap.network.model.ApiResponse

interface IApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST
    suspend fun createProfile(person: Person): Response<ApiResponse<String>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET
    suspend fun getProfile() : Response<ApiResponse<PersonProfile>>
}