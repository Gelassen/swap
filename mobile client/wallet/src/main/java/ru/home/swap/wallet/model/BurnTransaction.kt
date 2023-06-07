package ru.home.swap.wallet.model

import com.google.gson.GsonBuilder
import ru.home.swap.wallet.storage.model.ChainTransactionEntity
import ru.home.swap.wallet.storage.model.TxStatus
import ru.home.swap.wallet.storage.model.TxType

data class BurnTransaction(
    override var uid: Long = 0L,
    override var status: String = TxStatus.TX_PENDING,
    val owner: String,
    val tokenId: Long
    ): ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        return ChainTransactionEntity(
            uid = uid,
            txType = TxType.TX_BURN,
            payloadAsJson = GsonBuilder().create().toJson(this),
            status = this.status
        )
    }
}
