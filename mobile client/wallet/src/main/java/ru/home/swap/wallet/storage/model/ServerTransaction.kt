package ru.home.swap.wallet.storage.model

import ru.home.swap.core.model.IPayload
import ru.home.swap.core.model.RequestStatus

/**
 * This class has been created to unify processing of server dependent data for each tx
 * */
data class ServerTransaction(
    override var uid: Long = 0L,
    override val requestType: String,
    override var txChainId: Long = 0L, // just a stub
    override var status: String = RequestStatus.WAITING,
    override val payload: IPayload
) : IServerTransaction

fun ServerTransaction.fromDomain() : ServerRequestTransactionEntity {
    return ServerRequestTransactionEntity(
        uid = this.uid,
        requestType = this.requestType,
        status = this.status,
        txChainId = this.txChainId,
        payloadAsJson = payload.toJson()
    )
}