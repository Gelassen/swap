package ru.home.swap.network

import retrofit2.Response
import retrofit2.http.*
import ru.home.swap.model.Person
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.Service
import ru.home.swap.network.model.ApiResponse
import ru.home.swap.network.model.EmptyPayload
import ru.home.swap.network.model.ProfileResponse

interface IApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/account")
    suspend fun createProfile(@Body person: PersonProfile): Response<ProfileResponse<EmptyPayload>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/account")
    suspend fun getProfile() : Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/account/offers")
    suspend fun addOffer(
        @Header("Authorization") credentials: String,
        @Body service: Service): Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/account/demands")
    suspend fun addDemand(
        @Header("Authorization") credentials: String,
        @Body service: Service): Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("/api/account/offers")
    suspend fun removeOffer(
        @Header("Authorization") credentials: String,
        @Query("id") serviceId: Long): Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("/api/account/offers")
    suspend fun removeDemand(
        @Header("Authorization") credentials: String,
        @Query("id") serviceId: Long): Response<ProfileResponse<PersonProfile>>
}