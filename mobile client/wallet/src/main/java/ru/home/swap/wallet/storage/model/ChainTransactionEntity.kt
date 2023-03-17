package ru.home.swap.wallet.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.converters.ValueTypeAdapter
import ru.home.swap.wallet.model.*
import java.lang.IllegalArgumentException
import java.lang.reflect.Type

@Entity(tableName = Schema.ChainTransaction.TABLE_NAME)
data class ChainTransactionEntity (
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Schema.ChainTransaction.UID)
    val uid: Long,
    @ColumnInfo(name = "txType") val txType: String,
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = "status") val status: String = ""
)

inline fun <reified T : ITransaction> ChainTransactionEntity.toDomainGeneric(): ITransaction {
    val gson = GsonBuilder()
        .registerTypeAdapter(Value::class.java, ValueTypeAdapter())
        .create()
    val fooType: Type = object : TypeToken<T>() {}.getType()
    val result = gson.fromJson<T>(this.payloadAsJson, fooType)
    result.uid = this.uid // object is recovered from cached json and its uid is not automatically incremented
    return result
}

fun ChainTransactionEntity.toDomain(): ITransaction {
    return when(this.txType) {
        TxType.TX_MINT_TOKEN -> { this.toDomainGeneric<MintTransaction>() }
        TxType.TX_REGISTER_USER -> { this.toDomainGeneric<RegisterUserTransaction>() }
        TxType.TX_APPROVE_TOKEN_MANAGER -> { this.toDomainGeneric<ApproveTokenManagerTransaction>() }
        TxType.TX_REGISTER_DEMAND -> { this.toDomainGeneric<RegisterDemandTransaction>() }
        TxType.TX_APPROVE_SWAP -> { this.toDomainGeneric<ApproveSwapTransaction>() }
        TxType.TX_SWAP -> { this.toDomainGeneric<SwapTransaction>() }
        else -> { throw IllegalArgumentException("TxType from cache is unknown. Did you forget to add support of a new TxType?") }
    }
}

object TxType {
    const val TX_MINT_TOKEN = "mintToken"
    const val TX_REGISTER_USER = "registerUser"
    const val TX_APPROVE_TOKEN_MANAGER = "approveTokenManager"
    const val TX_APPROVE_SWAP = "txApproveSwap"
    const val TX_SWAP = "txSwap"
    const val TX_REGISTER_DEMAND = "txRegisterDemand"
}

object TxStatus {
    const val TX_NONE = "not-set-yet"
    const val TX_PENDING = "pending"
    const val TX_MINED = "mined"
    const val TX_REVERTED = "reverted"
    const val TX_EXCEPTION = "exception"
    const val TX_REJECTED = "rejected" // TODO 'rejected' status was mentioned somewhere in docs, but never met - require more research
    const val TX_DECLINED = "declined" // TODO 'declined' status was mentioned somewhere in docs, but never met - require more research
    const val TX_MINED_AND_PROCESSED = "mined&processed" // extra status for POSTing data to the backend
}