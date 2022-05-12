package ru.home.swap.network.model

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ApiResponse<T> (
    @SerializedName("payload") @Expose val payload: T
)