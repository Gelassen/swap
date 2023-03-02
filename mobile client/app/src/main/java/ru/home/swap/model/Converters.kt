package ru.home.swap.model

import com.google.gson.Gson
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.storage.ServerTransactionMetadataEntity

fun Service.fromDomain(chainServiceId: Long): ServerTransactionMetadataEntity {
    return ServerTransactionMetadataEntity(
        uid = this.id,
        txType = "Just a stub",
        payloadAsJson = Gson().toJson(this),
        txChainId = chainServiceId
    )
}