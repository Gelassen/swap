package com.example.wallet.debug.contract

import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicStruct
import org.web3j.abi.datatypes.generated.Uint256
import java.math.BigInteger

internal class TxDetail : DynamicStruct {
    var amount: BigInteger
    var toAddress: String

    constructor(amount: BigInteger, toAddress: String) : super(
        Uint256(amount),
        Address(toAddress)
    ) {
        this.amount = amount
        this.toAddress = toAddress
    }

    constructor(amount: Uint256, toAddress: Address) : super(amount, toAddress) {
        this.amount = amount.value
        this.toAddress = toAddress.value
    }
}