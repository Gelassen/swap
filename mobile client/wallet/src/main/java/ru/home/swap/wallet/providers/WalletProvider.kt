package ru.home.swap.wallet.providers

import org.web3j.crypto.WalletUtils
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.contract.convertToJson

class WalletProvider {

    fun isValidEthereumAddress(input: String): Boolean {
        return WalletUtils.isValidAddress(input)
    }

    fun getValueAsJson(value: Value): String {
        return value.convertToJson()
    }
}