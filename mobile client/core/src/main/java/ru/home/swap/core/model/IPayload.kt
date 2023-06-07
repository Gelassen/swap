package ru.home.swap.core.model

interface IPayload {
    fun toJson(): String
}

object RequestType {
    const val TX_REGISTER_USER = "registerUser"
    const val TX_APPROVE_TOKEN_MANAGER = "approveTokenManager"
    const val TX_APPROVE_SWAP = "txApproveSwap"
    const val TX_SWAP = "txSwap"
    const val TX_REGISTER_OFFER = "txRegisterOffer"
    const val TX_REGISTER_DEMAND = "txRegisterDemand"
    const val TX_BURN = "burn"
}

object RequestStatus {
    const val PROCESSED = "processed"
    const val WAITING = "waiting"
}