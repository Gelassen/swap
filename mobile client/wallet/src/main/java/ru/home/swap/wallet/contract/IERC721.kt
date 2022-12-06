package ru.home.swap.wallet.contract

import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

interface IERC721 {

    object Functions {
        val FUNC_BALANCEOF = "balanceOf"
        val FUNC_SETAPPROVALFORALL = "setApprovalForAll"
        val FUNC_ISAPPROVEDFORALL = "isApprovedForAll"
        val FUNC_GETAPPROVED = "getApproved"
    }

    fun balanceOf(owner: String): RemoteFunctionCall<BigInteger>

    fun ownerOf(tokenId: String): String

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);

    // void safeTransferFrom(ERC721Context ctx, String from, String to, String tokenId, String data);
    fun safeTransferFrom(from: String, to: String, tokenId: String)

    fun transferFrom(from: String, to: String, tokenId: String)

    fun approve(to: String, tokenId: String)

    fun setApprovalForAll(operator: String, approved: Boolean): RemoteFunctionCall<TransactionReceipt>

    fun getApproved(tokenId: Long): RemoteFunctionCall<String>

    fun isApprovedForAll(owner: String, operator: String): RemoteFunctionCall<Boolean>
}