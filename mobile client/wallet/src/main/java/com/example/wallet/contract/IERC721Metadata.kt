package com.example.wallet.contract

interface IERC721Metadata {

    fun name(): String

    fun symbol(): String

    fun tokenUri(tokenId: String): String
}