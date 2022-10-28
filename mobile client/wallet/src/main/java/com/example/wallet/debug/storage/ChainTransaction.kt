package com.example.wallet.debug.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.wallet.debug.contract.Value

@Entity
data class ChainTransaction (
    @PrimaryKey(autoGenerate = true) val uid: Int,
    @ColumnInfo(name = "to") val to: String,
    @ColumnInfo(name = "value") val value: Value,
    @ColumnInfo(name = "uri") val uri: String
)