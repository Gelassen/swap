package ru.home.swap.wallet.model

import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.utils.Numeric
import java.math.BigInteger

data class TransactionReceiptDomain(
    private var status: String? = null,
    private var revertReason: String? = null
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
    return TransactionReceiptDomain(
        status = this.status,
        revertReason = this.revertReason
    )
}