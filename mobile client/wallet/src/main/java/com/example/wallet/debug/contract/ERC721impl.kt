package com.example.wallet.debug.contract

import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.Contract
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import java.util.*

open class ERC721impl(
    contractBinary: String?,
    contractAddress: String?,
    web3j: Web3j?,
    credentials: Credentials?,
    gasProvider: ContractGasProvider?
) : IERC721, Contract(contractBinary, contractAddress, web3j, credentials, gasProvider) {
    override fun balanceOf(owner: String): Int {
        TODO("Not yet implemented")
    }

    override fun ownerOf(tokenId: String): String {
        TODO("Not yet implemented")
    }

    override fun safeTransferFrom(from: String, to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun transferFrom(from: String, to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun approve(to: String, tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun setApprovalForAll(operator: String, approved: Boolean) {
        TODO("Not yet implemented")
    }

    override fun getApproved(tokenId: String): String {
        TODO("Not yet implemented")
    }

    override fun isApprovedForAll(owner: String, operator: String): Boolean {
        TODO("Not yet implemented")
    }

    override fun safeMint(
        to: String,
        value: Value,
        uri: String,
        wei: BigInteger
    ): RemoteFunctionCall<TransactionReceipt> {
        val function: Function = Function(IERC721.Functions.SAFE_MINT,
            listOf(Utf8String(to), value, Utf8String(uri)),
            Collections.emptyList()
        )
        return executeRemoteCallTransaction(function, wei)
    }

    override fun burn(tokenId: String) {
        TODO("Not yet implemented")
    }
}