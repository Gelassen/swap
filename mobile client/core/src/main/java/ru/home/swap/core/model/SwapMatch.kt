package ru.home.swap.core.model

import com.google.gson.annotations.SerializedName

data class SwapMatch(
    @SerializedName("id")
    var id: Int,
    @SerializedName("userFirstProfileId")
    var userFirstProfileId: Int,
    @SerializedName("userSecondProfileId")
    var userSecondProfileId: Int,
    @SerializedName("userFirstServiceId")
    var userFirstServiceId: Int,
    @SerializedName("userSecondServiceId")
    var userSecondServiceId: Int,
    @SerializedName("approvedByFirstUser")
    var approvedByFirstUser: Boolean,
    @SerializedName("approvedBySecondUser")
    var approvedBySecondUser: Boolean,
    @SerializedName("userFirstProfileName")
    var userFirstProfileName: String,
    @SerializedName("userSecondProfileName")
    var userSecondProfileName: String,
    @SerializedName("userFirstServiceTitle")
    var userFirstServiceTitle: String,
    @SerializedName("userSecondServiceTitle")
    var userSecondServiceTitle : String,
    @SerializedName("userFirstService")
    var userFirstService : ChainService = ChainService(),
    @SerializedName("userSecondService")
    var userSecondService: ChainService = ChainService()
)