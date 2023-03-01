package ru.home.swap.wallet.storage

import androidx.room.Embedded
import androidx.room.Relation

data class TxWithMetadata(
    @Embedded val serverMetadata: ServerTransactionMetadataEntity,
    @Relation(
        parentColumn = "txChainId",
        entityColumn = "uid"
    )
    val tx: ChainTransactionEntity
)