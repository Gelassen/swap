package ru.home.swap.wallet.contract

interface IERC721Metadata {

    fun name(): String

    fun symbol(): String

    fun tokenUri(tokenId: String): String
}