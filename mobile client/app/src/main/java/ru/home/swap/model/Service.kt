package ru.home.swap.model

import com.google.gson.annotations.SerializedName

data class Service(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("title")
    val title: String,
    val date: Long,
    val index: List<String>)