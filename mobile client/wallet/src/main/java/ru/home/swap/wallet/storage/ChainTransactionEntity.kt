package ru.home.swap.wallet.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.home.swap.wallet.contract.Value

@Entity(tableName = ChainTransactionDao.Const.tableName)
data class ChainTransactionEntity (
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "txType") val txType: String,
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = "status") val status: String = ""
)

object TxType {
    const val TX_MINT_TOKEN = "mintToken"
    const val TX_REGISTER_USER = "registerUser"
}

object TxStatus {
    const val TX_PENDING = "pending"
    const val TX_MINED = "mined"
    const val TX_REVERTED = "reverted"
    const val TX_EXCEPTION = "exception"
    const val TX_REJECTED = "rejected" // TODO 'rejected' status was mentioned somewhere in docs, but never met - require more research
    const val TX_DECLINED = "declined" // TODO 'declined' status was mentioned somewhere in docs, but never met - require more research
}