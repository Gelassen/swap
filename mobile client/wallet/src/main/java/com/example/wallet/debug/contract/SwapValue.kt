package com.example.wallet.debug.contract

import org.web3j.abi.datatypes.Function
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.RemoteFunctionCall
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.tx.gas.ContractGasProvider
import java.math.BigInteger
import java.util.*

class SwapValue(
    contractBinary: String?,
    contractAddress: String?,
    web3j: Web3j?,
    credentials: Credentials?,
    gasProvider: ContractGasProvider?
) : ERC721impl(contractBinary, contractAddress, web3j, credentials, gasProvider) {

    override fun safeMint(to: String, value: Value, uri: String, wei: BigInteger): RemoteFunctionCall<TransactionReceipt> {
        return super.safeMint(to, value, uri, wei)
        // TODO complete me

    }
}