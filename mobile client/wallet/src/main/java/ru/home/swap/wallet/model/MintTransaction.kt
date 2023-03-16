package ru.home.swap.wallet.model

import com.google.gson.GsonBuilder
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.converters.ValueTypeAdapter
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxStatus
import ru.home.swap.wallet.storage.TxType

data class MintTransaction(
    override var uid: Long = 0L,
    override var status: String = TxStatus.TX_PENDING,
    val to: String = "",
    val value: Value = Value(),
    val uri: String = "",
    var tokenId: Int = -1
) : ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        val gson = GsonBuilder()
            .registerTypeAdapter(Value::class.java, ValueTypeAdapter())
            .create()
        return ChainTransactionEntity(
            uid = this.uid,
            txType = TxType.TX_MINT_TOKEN,
            payloadAsJson = gson.toJson(this),
            status = this.status
        )
    }
}
