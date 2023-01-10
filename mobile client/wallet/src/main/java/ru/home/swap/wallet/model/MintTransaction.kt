package ru.home.swap.wallet.model

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.converters.ValueTypeAdapter
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxStatus
import ru.home.swap.wallet.storage.TxType

data class MintTransaction(
    override var uid: Long = -1,
    override var status: String = TxStatus.TX_PENDING,
    val to: String = "",
    val value: Value = Value(),
    val uri: String = ""
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

/*    override fun toDomain(tx: ChainTransactionEntity): ITransaction {
        val result = Gson().fromJson(tx.payloadAsJson, MintTransaction::class.java)
        result.uid = tx.uid // object is recovered from cached json and its uid is not automatically incremented
        return result
    }*/
}
