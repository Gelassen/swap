package ru.home.swap.core.network.model

import com.google.gson.annotations.SerializedName

data class ApiResponse<T> (
    @SerializedName("payload") var payload : T,
    @SerializedName("msg") var message : String
)