package ru.home.swap.model

import com.google.gson.Gson
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.storage.ServerRequestTransactionEntity

fun Service.fromDomain(chainServiceId: Long): ServerRequestTransactionEntity {
    return ServerRequestTransactionEntity(
        uid = this.id,
        requestType = "Just a stub",
        payloadAsJson = Gson().toJson(this),
        txChainId = chainServiceId
    )
}