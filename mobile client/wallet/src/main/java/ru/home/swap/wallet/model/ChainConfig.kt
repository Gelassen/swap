package ru.home.swap.wallet.model

data class ChainConfig(
    val chainId: Long,
    val swapTokenAddress: String,
    val swapMarketAddress: String,
    val accountPrivateKey: String
)