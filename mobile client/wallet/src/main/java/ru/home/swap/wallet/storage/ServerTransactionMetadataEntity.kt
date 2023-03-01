package ru.home.swap.wallet.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Schema.ServerMetadata.TABLE_NAME)
data class ServerTransactionMetadataEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "txType") val txType: String,
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = "txChainId") val txChainId: Long
/*    @Deprecated("Not used in V2 concept, but left as a possible improvement for the future")
    @ColumnInfo(name = "status") val status: String = ""*/
)