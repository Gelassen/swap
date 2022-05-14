package ru.home.swap.network.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse<T>(
    @SerializedName("payload") var payload : T
)