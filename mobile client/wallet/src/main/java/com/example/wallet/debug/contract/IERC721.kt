package com.example.wallet.debug.contract

import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

interface IERC721 {

    object Functions {
        val SAFE_MINT: String = "safeMint"
    }

    fun balanceOf(owner: String): Int

    fun ownerOf(tokenId: String): String

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);
    fun safeTransferFrom(from: String, to: String, tokenId: String)

    fun transferFrom(from: String, to: String, tokenId: String)

    fun approve(to: String, tokenId: String)

    fun setApprovalForAll(operator: String, approved: Boolean)

    fun getApproved(tokenId: String): String

    fun isApprovedForAll(owner: String, operator: String): Boolean

    fun burn(tokenId: String)

    fun safeMint(
        to: String,
        value: Value,
        uri: String,
        wei: BigInteger
    ): RemoteFunctionCall<TransactionReceipt>
}