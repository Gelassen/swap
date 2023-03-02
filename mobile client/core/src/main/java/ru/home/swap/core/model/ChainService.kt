package ru.home.swap.core.model

import com.google.gson.annotations.SerializedName

data class ChainService(
    @SerializedName("id")
    var id: Long = 0L,
    val userWalletAddress: String = "",
    val tokenId: Int = -1,
    var serverServiceId: Int = -1
)

fun ChainService.new(id: Long, serverServiceId: Int = -1, origin: ChainService): ChainService {
    return ChainService(
        id = id,
        userWalletAddress = origin.userWalletAddress,
        tokenId = origin.tokenId,
        serverServiceId = serverServiceId
    )
}