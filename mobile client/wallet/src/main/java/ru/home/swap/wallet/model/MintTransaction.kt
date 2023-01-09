package ru.home.swap.wallet.model

import com.google.gson.Gson
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxType

data class MintTransaction(
    override var uid: Long = -1,
    override var status: String = "",
    val to: String = "",
    val value: Value = Value(),
    val uri: String = ""
) : ITransaction {
    override fun fromDomain(): ChainTransactionEntity {
        return ChainTransactionEntity(
            uid = this.uid,
            txType = TxType.TX_MINT_TOKEN,
            payloadAsJson = Gson().toJson(this),
            status = this.status
        )
    }

/*    override fun toDomain(tx: ChainTransactionEntity): ITransaction {
        val result = Gson().fromJson(tx.payloadAsJson, MintTransaction::class.java)
        result.uid = tx.uid // object is recovered from cached json and its uid is not automatically incremented
        return result
    }*/
}
