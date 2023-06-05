package ru.home.swap.wallet.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(tableName = Schema.ChainService.TABLE_NAME)
data class ChainServiceEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Schema.ChainService.UID)
    val uid: Long = 0L,
    @SerializedName("id")
    var id: Long = 0L,
    @ColumnInfo
    val userWalletAddress: String = "",
    @ColumnInfo
    var tokenId: Int = -1,
    @ColumnInfo
    var serverServiceId: Int = -1
)

fun ChainServiceEntity.new(id: Long, serverServiceId: Int = -1, origin: ChainServiceEntity): ChainServiceEntity {
    return ChainServiceEntity(
        id = id,
        userWalletAddress = origin.userWalletAddress,
        tokenId = origin.tokenId,
        serverServiceId = serverServiceId
    )
}
