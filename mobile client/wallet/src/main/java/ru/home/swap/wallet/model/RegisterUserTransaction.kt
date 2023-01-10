package ru.home.swap.wallet.model

import com.google.gson.Gson
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxStatus
import ru.home.swap.wallet.storage.TxType

data class RegisterUserTransaction(
    override var uid: Long = -1L,
    override var status: String = TxStatus.TX_PENDING,
    val userWalletAddress: String
    ) : ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        return ChainTransactionEntity(
            uid = this.uid,
            txType = TxType.TX_REGISTER_USER,
            payloadAsJson = Gson().toJson(this),
            status = this.status
        )
    }
}