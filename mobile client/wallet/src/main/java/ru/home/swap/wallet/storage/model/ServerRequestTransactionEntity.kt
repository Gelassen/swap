package ru.home.swap.wallet.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.converters.ValueTypeAdapter
import java.lang.reflect.Type

@Entity(tableName = Schema.ServerMetadata.TABLE_NAME)
data class ServerRequestTransactionEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "requestType") val requestType: String = "",
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = Schema.ServerMetadata.TX_CHAIN_ID) val txChainId: Long,
    @ColumnInfo(name = Schema.ServerMetadata.STATUS) val status: String
/*    @Deprecated("Not used in V2 concept, but left as a possible improvement for the future")
    @ColumnInfo(name = "status") val status: String = ""*/
)
// TODO extend this class to support others request types besides 'offers'
fun ServerRequestTransactionEntity.toDomainObject(): Service {
    val gson = GsonBuilder()
        .registerTypeAdapter(Value::class.java, ValueTypeAdapter())
        .create()
    val fooType: Type = object : TypeToken<Service>() {}.getType()
    val result = gson.fromJson<Service>(this.payloadAsJson, fooType)
    result.status = this.status
    result.id = this.uid
    return result
}

fun Service.fromDomain(chainServiceId: Long, isProcessed: Boolean = false): ServerRequestTransactionEntity {
    return ServerRequestTransactionEntity(
        uid = this.id,
        requestType = "Just a stub",
        payloadAsJson = Gson().toJson(this),
        txChainId = chainServiceId,
        status = if (isProcessed) RequestStatus.PROCESSED else RequestStatus.WAITING
    )
}

object RequestStatus {
    const val PROCESSED = "processed"
    const val WAITING = "waiting"
}