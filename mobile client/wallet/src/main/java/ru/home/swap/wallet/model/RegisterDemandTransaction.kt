package ru.home.swap.wallet.model

import com.google.gson.Gson
import ru.home.swap.wallet.storage.model.ChainTransactionEntity
import ru.home.swap.wallet.storage.model.TxStatus
import ru.home.swap.wallet.storage.model.TxType

data class RegisterDemandTransaction(
    override var uid: Long = 0,
    override var status: String = TxStatus.TX_PENDING,
    val userAddress: String,
    val demand: String
): ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        return ChainTransactionEntity(
            uid = this.uid,
            status = this.status,
            payloadAsJson = Gson().toJson(this, RegisterDemandTransaction::class.java),
            txType = TxType.TX_REGISTER_DEMAND
        )
    }
}