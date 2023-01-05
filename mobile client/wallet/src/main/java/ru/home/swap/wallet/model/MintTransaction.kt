package ru.home.swap.wallet.model

import com.google.gson.Gson
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.TxType

data class MintTransaction(
    var uid: Long = -1,
    val to: String = "",
    val value: Value = Value(),
    val uri: String = "",
    var status: String = ""
)

fun MintTransaction.fromDomain(tx: MintTransaction): ChainTransactionEntity {
    return ChainTransactionEntity(
        uid = tx.uid,
        txType = TxType.TX_MINT_TOKEN,
        payloadAsJson = Gson().toJson(tx),
        status = tx.status
    )
}

fun MintTransaction.toDomain(tx: ChainTransactionEntity): MintTransaction {
    val result = Gson().fromJson(tx.payloadAsJson, MintTransaction::class.java)
    result.uid = tx.uid // object is recovered from cached json and its uid is not automatically incremented
    return result
}
