package ru.home.swap.wallet.model

import ru.home.swap.wallet.contract.Value

data class Token(
    val tokenId: Long,
    val value: Value
)