package ru.home.swap.wallet.model

import com.google.gson.Gson
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxStatus
import ru.home.swap.wallet.storage.TxType

data class ApproveTokenManagerTransaction(
    override var uid: Long = 0,
    override var status: String = TxStatus.TX_PENDING,
    val swapMarketContractAddress: String,
    val isApproved: Boolean = false
    ) : ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        return ChainTransactionEntity(
            uid = this.uid,
            txType = TxType.TX_APPROVE_TOKEN_MANAGER,
            payloadAsJson = Gson().toJson(this),
            status = this.status
        )
    }
}