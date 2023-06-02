package ru.home.swap.wallet.model

import com.google.gson.annotations.SerializedName

data class ChainServiceEntity (
    @SerializedName("idChainService")
    var idChainService: Int,
    @SerializedName("userAddress")
    var userAddress: String,
    @SerializedName("tokenId")
    var tokenId: Int
)
