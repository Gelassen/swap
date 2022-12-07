package ru.home.swap.wallet.contract

import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt

interface ISwapChain {

    object Functions {
        const val FUNC_REGISTER_USER = "registerUser"
        const val FUNC_APPROVE_SWAP = "approveSwap"
        const val FUNC_REGISTER_DEMAND = "registerDemand"
    }

    fun registerUser(userWalletAddress: String): RemoteFunctionCall<TransactionReceipt>

    fun approveSwap(matchObj: Match): RemoteFunctionCall<TransactionReceipt>

    fun registerDemand(userAddress: String, demand: String): RemoteFunctionCall<TransactionReceipt>

}