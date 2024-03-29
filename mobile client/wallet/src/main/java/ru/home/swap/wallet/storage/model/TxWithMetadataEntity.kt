package ru.home.swap.wallet.storage.model

import androidx.room.Embedded
import androidx.room.Relation
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction

data class TxWithMetadataEntity(
    @Embedded val serverMetadata: ServerRequestTransactionEntity,
    @Relation(
        parentColumn = Schema.ServerMetadata.TX_CHAIN_ID,
        entityColumn = Schema.ChainTransaction.UID
    )
    val tx: ChainTransactionEntity
)

// TODO expand me to generic domain conversion
fun TxWithMetadataEntity.toDomain() : Pair<ITransaction, ServerTransaction> {
    val transaction = tx.toDomain()
    val service = serverMetadata.toDomainObject()
    return Pair(transaction, service)
}
