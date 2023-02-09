package ru.home.swap.wallet.providers

import org.web3j.crypto.WalletUtils

class WalletProvider {

    fun isValidEthereumAddress(input: String): Boolean {
        return WalletUtils.isValidAddress(input)
    }
}