package ru.home.swap.network

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import ru.home.swap.model.Person
import ru.home.swap.model.PersonProfile
import ru.home.swap.network.model.ApiResponse
import ru.home.swap.network.model.ProfileResponse

interface IApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/account")
    suspend fun createProfile(@Body person: PersonProfile): Response<ProfileResponse>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/account")
    suspend fun getProfile() : Response<ApiResponse<PersonProfile>>
}