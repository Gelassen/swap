package ru.home.swap.wallet.storage.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import ru.home.swap.core.model.IPayload
import ru.home.swap.core.model.PersonProfile
import ru.home.swap.core.model.RequestType
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.contract.Match
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.converters.MatchTypeAdapter
import ru.home.swap.wallet.converters.ValueTypeAdapter
import java.lang.IllegalArgumentException
import java.lang.reflect.Type

@Entity(
    tableName = Schema.ServerMetadata.TABLE_NAME,
    foreignKeys = [ForeignKey(
        entity = ChainTransactionEntity::class,
        parentColumns = arrayOf(Schema.ChainTransaction.UID),
        childColumns = arrayOf(Schema.ServerMetadata.TX_CHAIN_ID),
        onDelete = ForeignKey.CASCADE
    )]
)
data class ServerRequestTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = Schema.ServerMetadata.UID)
    val uid: Long = 0L,
    @ColumnInfo(name = "requestType") val requestType: String = "",
    @ColumnInfo(name = "payloadAsJsonString") val payloadAsJson: String,
    @ColumnInfo(name = Schema.ServerMetadata.TX_CHAIN_ID) var txChainId: Long = 0L,
    @ColumnInfo(name = Schema.ServerMetadata.STATUS) val status: String
/*    @Deprecated("Not used in V2 concept, but left as a possible improvement for the future")
    @ColumnInfo(name = "status") val status: String = ""*/
)

inline fun <reified T: IPayload> ServerRequestTransactionEntity.toDomainGeneric(): ServerTransaction {
    val gson = GsonBuilder()
        .registerTypeAdapter(Value::class.java, ValueTypeAdapter())
        .registerTypeAdapter(Match::class.java, MatchTypeAdapter())
        .create()
    val fooType: Type = object : TypeToken<T>() {}.getType()
    val result = gson.fromJson<T>(this.payloadAsJson, fooType)
    return ServerTransaction(
        uid = this.uid,
        requestType = this.requestType,
        txChainId = this.txChainId,
        status = this.status,
        payload = result
    )
}

// TODO extend this class to support others request types besides 'offers'
fun ServerRequestTransactionEntity.toDomainObject(): ServerTransaction {
    return when (this.requestType) {
        RequestType.TX_REGISTER_OFFER -> { this.toDomainGeneric<Service>() }
        RequestType.TX_REGISTER_USER -> { this.toDomainGeneric<PersonProfile>() }
        else -> { throw IllegalArgumentException("RequestType from cache is unknown. Did you forget to add support of a new RequestType?") }
    }
}
