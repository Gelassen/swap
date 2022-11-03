package ru.home.wallet.contract

import org.web3j.protocol.core.RemoteFunctionCall
import java.math.BigInteger

interface IERC721 {

    object Functions {
        val FUNC_BALANCEOF = "balanceOf"
    }

    fun balanceOf(owner: String): RemoteFunctionCall<BigInteger>

    fun ownerOf(tokenId: String): String

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);
    fun safeTransferFrom(from: String, to: String, tokenId: String)

    fun transferFrom(from: String, to: String, tokenId: String)

    fun approve(to: String, tokenId: String)

    fun setApprovalForAll(operator: String, approved: Boolean)

    fun getApproved(tokenId: String): String

    fun isApprovedForAll(owner: String, operator: String): Boolean
}