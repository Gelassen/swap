package ru.home.swap.wallet.providers

import org.web3j.crypto.WalletUtils
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.contract.convertToJson
import java.math.BigInteger
import java.util.*

/**
 * We have to define this functions in the wallet module as a
 * workaround to mitigate dependencies conflicts.
 * */
class WalletProvider {

    fun isValidEthereumAddress(input: String): Boolean {
        return WalletUtils.isValidAddress(input)
    }

    fun getAYearAfter(): BigInteger {
        val calendar = Calendar.getInstance()
        calendar.time = Date(System.currentTimeMillis())
        calendar.set(Calendar.YEAR, calendar.get(Calendar.YEAR).plus(1))
        return BigInteger.valueOf(calendar.time.time)
    }

    fun getValueAsJson(value: Value): String {
        return value.convertToJson()
    }
}