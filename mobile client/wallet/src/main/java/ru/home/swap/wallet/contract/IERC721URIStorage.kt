package ru.home.swap.wallet.contract

import java.math.BigInteger

interface IERC721URIStorage {

    fun tokenUri(tokenId: BigInteger): String

    // fun _setTokenURI(tokenId, _tokenURI)

    // fun _burn(tokenId)


}