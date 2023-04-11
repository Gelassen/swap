package ru.home.swap.wallet.storage.model

import ru.home.swap.core.model.IPayload

interface IServerTransaction {
    var uid: Long // the uid should be the same/in sync with an uid in the payload
    val requestType: String
    val txChainId: Long
    var status: String
    val payload: IPayload
}