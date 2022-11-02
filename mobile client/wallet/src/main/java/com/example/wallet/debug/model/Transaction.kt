package com.example.wallet.debug.model

import com.example.wallet.debug.contract.Value

data class Transaction(
    val to: String,
    val value: Value,
    val uri: String
)
