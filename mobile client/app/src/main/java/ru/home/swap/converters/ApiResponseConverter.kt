package ru.home.swap.converters

import com.google.gson.GsonBuilder
import ru.home.swap.core.network.model.ApiResponse

class ApiResponseConverter {

    fun toDomain(json: String): ApiResponse<String> {
        val gson = GsonBuilder().create()
        return gson.fromJson<ApiResponse<String>>(json, ApiResponse::class.java)
    }

}