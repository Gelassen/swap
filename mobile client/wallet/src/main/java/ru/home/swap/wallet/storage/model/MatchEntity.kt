package ru.home.swap.wallet.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Schema.ChainMatch.TABLE_NAME)
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Schema.ChainMatch.UID)
    val uid: Long,
    @ColumnInfo("id")
    var id: Int,
    @ColumnInfo("userFirstProfileId")
    var userFirstProfileId: Int,
    @ColumnInfo("userSecondProfileId")
    var userSecondProfileId: Int,
    @ColumnInfo("userFirstServiceId")
    var userFirstServiceId: Int,
    @ColumnInfo("userSecondServiceId")
    var userSecondServiceId: Int,
    @ColumnInfo("approvedByFirstUser")
    var approvedByFirstUser: Boolean,
    @ColumnInfo("approvedBySecondUser")
    var approvedBySecondUser: Boolean,
    @ColumnInfo("userFirstProfileName")
    var userFirstProfileName: String,
    @ColumnInfo("userSecondProfileName")
    var userSecondProfileName: String,
    @ColumnInfo("userFirstServiceTitle")
    var userFirstServiceTitle: String,
    @ColumnInfo("userSecondServiceTitle")
    var userSecondServiceTitle : String
)
