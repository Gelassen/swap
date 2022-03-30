package ru.home.swap.model

import com.google.gson.annotations.SerializedName

data class Service(
    @SerializedName("name")
    val title: String,
    val date: Long,
    val index: List<String>)