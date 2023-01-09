package ru.home.swap.wallet.storage

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.model.RegisterUserTransaction
import java.lang.IllegalArgumentException
import java.lang.reflect.Type

@Entity(tableName = ChainTransactionDao.Const.tableName)
data class ChainTransactionEntity (
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "txType") val txType: String,
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = "status") val status: String = ""
)

inline fun <reified T : ITransaction> ChainTransactionEntity.toDomainGeneric(): ITransaction {
    val fooType: Type = object : TypeToken<T>() {}.getType()
    val result = Gson().fromJson<T>(this.payloadAsJson, fooType)
    result.uid = this.uid // object is recovered from cached json and its uid is not automatically incremented
    return result
}

fun ChainTransactionEntity.toDomain(): ITransaction {
    return when(this.txType) {
        TxType.TX_MINT_TOKEN -> { this.toDomainGeneric<MintTransaction>() }
        TxType.TX_REGISTER_USER -> { this.toDomainGeneric<RegisterUserTransaction>() }
        else -> { throw IllegalArgumentException("TxType from cache is unknown. Did you forget to add support of a new TxType?") }
    }
}

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