package ru.home.swap.wallet.storage

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
data class ServerTransactionMetadataEntity(
    @PrimaryKey(autoGenerate = true) val uid: Long,
    @ColumnInfo(name = "txType") val txType: String = "",
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = "txChainId") val txChainId: Long
/*    @Deprecated("Not used in V2 concept, but left as a possible improvement for the future")
    @ColumnInfo(name = "status") val status: String = ""*/
)

fun ServerTransactionMetadataEntity.toDomainObject(): Service {
    val gson = GsonBuilder()
        .registerTypeAdapter(Value::class.java, ValueTypeAdapter())
        .create()
    val fooType: Type = object : TypeToken<Service>() {}.getType()
    val result = gson.fromJson<Service>(this.payloadAsJson, fooType)
    return result
}

fun Service.fromDomain(chainServiceId: Long): ServerTransactionMetadataEntity {
    return ServerTransactionMetadataEntity(
        uid = this.id,
        txType = "Just a stub",
        payloadAsJson = Gson().toJson(this),
        txChainId = chainServiceId
    )
}