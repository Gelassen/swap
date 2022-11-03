package ru.home.swap.wallet.contract

import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt

interface ISwapChain {

    object Functions {
        const val FUNC_REGISTER_USER = "registerUser"
    }

    fun registerUser(userWalletAddress: String): RemoteFunctionCall<TransactionReceipt>

}