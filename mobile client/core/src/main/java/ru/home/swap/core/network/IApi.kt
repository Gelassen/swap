package ru.home.swap.core.network

import retrofit2.Response
import retrofit2.http.*
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.Service
import ru.home.swap.core.network.model.ApiResponse

interface IApi {

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/v1/account")
    suspend fun createProfile(
        @Header("Authorization") credentials: String,
        @Body person: PersonProfile
    ): Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/v1/account")
    suspend fun getProfile(
        @Header("Authorization") credentials: String
    ) : Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/v1/account/offers")
    suspend fun addOffer(
        @Header("Authorization") credentials: String,
        @Body service: Service
    ): Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @POST("/api/v1/account/demands")
    suspend fun addDemand(
        @Header("Authorization") credentials: String,
        @Body service: Service): Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("/api/v1/account/offers/{id}")
    suspend fun removeOffer(
        @Header("Authorization") credentials: String,
        @Path("id") serviceId: Long): Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @DELETE("/api/v1/account/offers/{id}")
    suspend fun removeDemand(
        @Header("Authorization") credentials: String,
        @Path("id") serviceId: Long): Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/v1/offers")
    suspend fun getOffers(
        @Header("Authorization") credentials: String,
        @Query("page") page: Int,
        @Query("size") size: Int): Response<ApiResponse<Collection<Service>>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/v1/demands")
    suspend fun getDemands(
        @Header("Authorization") credentials: String,
        @Query("page") page: Int,
        @Query("size") size: Int): Response<ApiResponse<Collection<Service>>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/v1/contacts")
    suspend fun getContacts(
        @Header("Authorization") credentials: String,
        @Query("serviceId") serviceId: Long): Response<ApiResponse<PersonProfile>>

    @Headers("Content-Type: application/json; charset=utf-8")
    @GET("/api/v1/account/matches")
    suspend fun getMatchesForUserDemands(
        @Header("Authorization") credentials: String,
        @Query("page") page: Int,
        @Query("size") size: Int
    ): Response<ApiResponse<Collection<Any>>>
}