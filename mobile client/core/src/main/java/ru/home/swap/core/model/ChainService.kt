package ru.home.swap.core.model

import com.google.gson.annotations.SerializedName

data class ChainService(
    @SerializedName("id")
    val id: Long = 0L,
    val userWalletAddress: String = "",
    val tokenId: Int = -1,
    val serverServiceId: Int = -1
)