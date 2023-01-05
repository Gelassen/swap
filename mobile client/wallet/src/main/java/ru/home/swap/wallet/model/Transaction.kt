package ru.home.swap.wallet.model

import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.storage.ChainTransactionEntity

data class Transaction(
    val uid: Long = -1,
    val to: String = "",
    val value: Value = Value(),
    val uri: String = "",
    var status: String = ""
)

fun Transaction.fromDomain(tx: Transaction): ChainTransactionEntity {
    return ChainTransactionEntity(
        uid = tx.uid,
        to = tx.to,
        value = tx.value,
        uri = tx.uri,
        status = tx.status
    )
}

fun Transaction.toDomain(tx: ChainTransactionEntity): Transaction {
    return Transaction(
        uid = tx.uid,
        to = tx.to,
        value = tx.value,
        uri = tx.uri,
        status = tx.status
    )
}
