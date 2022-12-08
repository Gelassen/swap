package ru.home.swap.wallet.contract

import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import java.math.BigInteger

/**
 * All java classes which reflects solidity contracts are just a wrappers which under API
 * provides calls to the chain contract's functions. Therefore they don't need a whole
 * logic implementation neither inheritance hierarchy -- just a wrap on chain function call.
 * */
interface ISwapValue {

    object Functions {
        val FUNC_SAFE_MINT: String = "safeMint"
        val FUNC_GET_OFFER: String = "offer"
        val FUNC_GETTOKENSIDSFORUSER = "getTokensIdsForUser";
    }

    fun safeMint(
        to: String,
        value: Value,
        uri: String
    ): RemoteFunctionCall<TransactionReceipt>

    fun safeMint(
        to: String,
        value: Value,
        uri: String,
        wei: BigInteger
    ): RemoteFunctionCall<TransactionReceipt>

    fun getOffer(tokenId: String): RemoteFunctionCall<Value>

    fun getTokensIdsForUser(userAddress: String): RemoteFunctionCall<List<*>>

    @Deprecated(message = "Redundant")
    fun testReturnCall(): RemoteFunctionCall<TransactionReceipt>
}