package ru.home.swap.network

import retrofit2.Response
import retrofit2.http.*
import ru.home.swap.model.PersonProfile
import ru.home.swap.model.Service
import ru.home.swap.network.model.EmptyPayload
import ru.home.swap.network.model.ProfileResponse

interface IApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/v1/account")
    suspend fun createProfile(
        @Header("Authorization") credentials: String,
        @Body person: PersonProfile): Response<ProfileResponse<EmptyPayload>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/v1/account")
    suspend fun getProfile(
        @Header("Authorization") credentials: String
    ) : Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/v1/account/offers")
    suspend fun addOffer(
        @Header("Authorization") credentials: String,
        @Body service: Service): Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/v1/account/demands")
    suspend fun addDemand(
        @Header("Authorization") credentials: String,
        @Body service: Service): Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("/api/v1/account/offers/{id}")
    suspend fun removeOffer(
        @Header("Authorization") credentials: String,
        @Path("id") serviceId: Long): Response<ProfileResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("/api/v1/account/offers/{id}")
    suspend fun removeDemand(
        @Header("Authorization") credentials: String,
        @Path("id") serviceId: Long): Response<ProfileResponse<PersonProfile>>
}