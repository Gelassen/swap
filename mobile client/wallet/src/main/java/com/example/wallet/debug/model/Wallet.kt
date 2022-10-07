package com.example.wallet.debug.model

import java.math.BigInteger

class Wallet {

    private var balance: BigInteger = BigInteger.valueOf(0)

    fun getBalance(): BigInteger {
        return this.balance
    }

    fun setBalance(value: Int) {
        setBalance(BigInteger.valueOf(value.toLong()))
    }

    fun setBalance(value: BigInteger) {
        this.balance = value
    }

}