package ru.home.swap.wallet.model

import java.math.BigInteger

class Wallet {

    private var balance: BigInteger = BigInteger.valueOf(0)
    private var tokens: MutableList<Token> = mutableListOf()

    fun getBalance(): BigInteger {
        return this.balance
    }

    fun setBalance(value: Int) {
        setBalance(BigInteger.valueOf(value.toLong()))
    }

    fun setBalance(value: BigInteger) {
        this.balance = value
    }

    fun addToken(token: Token) {
        tokens.plus(token)
    }

}