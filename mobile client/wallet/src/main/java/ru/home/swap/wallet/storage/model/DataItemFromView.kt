package ru.home.swap.wallet.storage.model

import androidx.room.DatabaseView
import ru.home.swap.wallet.storage.model.Schema.*

@DatabaseView(
    viewName = Views.ChainTxWithServerMetadata.VIEW_NAME,
    value =
            "SELECT " +
                    "${ChainTransaction.TABLE_NAME}.${ChainTransaction.UID}, " +
                    "${ChainTransaction.TABLE_NAME}.${ChainTransaction.TX_TYPE}, " +
                    "${ChainTransaction.TABLE_NAME}.${ChainTransaction.PAYLOAD_AS_JSON}, " +
                    "${ChainTransaction.TABLE_NAME}.${ChainTransaction.STATUS}, " +
                    "${ServerMetadata.TABLE_NAME}.${ServerMetadata.UID} AS sUid, " +
                    "${ServerMetadata.TABLE_NAME}.${ServerMetadata.REQUEST_TYPE} AS sRequestType, " +
                    "${ServerMetadata.TABLE_NAME}.${ServerMetadata.PAYLOAD_AS_JSON} AS sPayloadAsJson, " +
                    "${ServerMetadata.TABLE_NAME}.${ServerMetadata.TX_CHAIN_ID} AS sTxChainId, " +
                    "${ServerMetadata.TABLE_NAME}.${ServerMetadata.STATUS} AS sStatus " +
            "FROM ${ChainTransaction.TABLE_NAME} " +
            "INNER JOIN ${ServerMetadata.TABLE_NAME} " +
            "ON ${ChainTransaction.TABLE_NAME}.${ChainTransaction.UID} " +
            "   = ${ServerMetadata.TABLE_NAME}.${ServerMetadata.TX_CHAIN_ID}"
)
data class DataItemFromView(
    val uid: Long,
    val txType: String,
    val payloadAsJsonString: String,
    val status: String,
    val sUid: Long,
    val sRequestType: String,
    val sPayloadAsJson: String,
    val sTxChainId: String,
    val sStatus: String
)