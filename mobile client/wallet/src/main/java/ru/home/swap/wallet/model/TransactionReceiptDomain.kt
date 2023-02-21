package ru.home.swap.wallet.model

import android.util.Log
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Numeric
import ru.home.swap.core.App
import java.math.BigInteger

data class TransactionReceiptDomain(
    private var status: String? = null,
    private var revertReason: String? = null,
    var topics: MutableList<String> = mutableListOf()
) {
    fun getStatus(): String? {
        return status
    }

    fun isStatusOK(): Boolean {
        if (null == getStatus()) {
            return true
        }
        val statusQuantity = Numeric.decodeQuantity(getStatus())
        return BigInteger.ONE == statusQuantity
    }

    fun getRevertReason(): String {
        return if (revertReason == null) "" else revertReason!!
    }
}

fun TransactionReceipt.toDomain(): TransactionReceiptDomain {
    Log.d(App.TAG, "[tx receipt] ${this}")
    val tx = TransactionReceiptDomain(
        status = this.status,
        revertReason = this.revertReason
    )
    this.logs.map { it.topics.forEach { it -> tx.topics.add(it) } }
    return tx
}