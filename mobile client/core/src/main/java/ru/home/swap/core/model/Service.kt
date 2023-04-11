package ru.home.swap.core.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Service(
    var uid: Long = 0L,
    @SerializedName("title")
    val title: String = "",
    val date: Long = 0L,
    val index: List<String> = emptyList(),
    @SerializedName("chainService")
    val chainService: ChainService = ChainService(),
) : IPayload {
    override fun toJson(): String {
        return Gson().toJson(this)
    }
}

fun List<Service>.toJson(): String {
    return Gson().toJson(this)
}

fun Service.fromJson(json: String): Service {
    return Gson().fromJson(json, Service::class.java)
}