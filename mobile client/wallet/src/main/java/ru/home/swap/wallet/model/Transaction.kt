package ru.home.swap.wallet.model

import ru.home.swap.wallet.contract.Value

data class Transaction(
    val to: String,
    val value: Value,
    val uri: String
)
