package ru.home.swap.core.model

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class Service(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("title")
    val title: String,
    val date: Long,
    val index: List<String>,
    @SerializedName("chainService")
    val chainService: ChainService = ChainService()
)

fun Service.toJson(): String {
    return Gson().toJson(this)
}