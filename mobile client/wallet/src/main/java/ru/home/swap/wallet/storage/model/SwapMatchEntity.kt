package ru.home.swap.wallet.storage.model

import androidx.room.*


data class SwapMatchEntity(
    @Embedded
    val matchEntity: MatchEntity,
    @Relation(
        parentColumn = "userFirstServiceId",
        entityColumn = "serverServiceId"
    )
    var userFirstService : ChainServiceEntity = ChainServiceEntity(),
    @Relation(
        parentColumn = "userSecondServiceId",
        entityColumn = "serverServiceId"
    )
    var userSecondService: ChainServiceEntity = ChainServiceEntity()
)