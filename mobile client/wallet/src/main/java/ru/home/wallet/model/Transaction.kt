package ru.home.wallet.model

import com.example.wallet.contract.Value

data class Transaction(
    val to: String,
    val value: Value,
    val uri: String
)
