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
import java.util.concurrent.atomic.AtomicInteger

class SwapValue(
    contractBinary: String?,
    contractAddress: String?,
    web3j: Web3j?,
    credentials: Credentials?,
    gasProvider: ContractGasProvider?
) : ISwapValue, Contract(contractBinary, contractAddress, web3j, credentials, gasProvider) {

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

    override fun burn(tokenId: String) {
        TODO("Not yet implemented")
    }

    override fun safeMint(to: String, value: Value, uri: String, wei: BigInteger): RemoteFunctionCall<TransactionReceipt> {
/*        // TODO complete me
        *//*
        * TODO it all goes as a single tx
        uint256 tokenId = _tokenIdCounter.current(); // TODO have to be cached to keep state on the second app launch
        _tokenIdCounter.increment();
        _safeMint(to, tokenId); // implemented
        _setTokenURI(tokenId, uri); // TODO complete me
        _setOffer(value, tokenId); // TODO complete me
        *
        * *//*
        val tokenId = idCounter.getAndIncrement()
        return super.safeMint(to, value, uri, wei)*/

        val function: Function = Function(ISwapValue.Functions.SAFE_MINT,
            listOf(Utf8String(to), value, Utf8String(uri)),
            Collections.emptyList()
        )
        return executeRemoteCallTransaction(function, wei)
    }

    override fun tokenUri(tokenId: BigInteger): String {
        TODO("Not yet implemented")
    }

    override fun testReturnCall(): RemoteFunctionCall<TransactionReceipt> {
        TODO("Not yet implemented")
    }


}