package ru.home.swap.network.model

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("payload" ) var payload : EmptyPayload? = EmptyPayload()
)