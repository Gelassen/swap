package com.example.wallet.model

import com.example.wallet.contract.Value

data class Token(
    val tokenId: Long,
    val value: Value
)