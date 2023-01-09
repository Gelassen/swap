package ru.home.swap.wallet.model

import ru.home.swap.wallet.storage.ChainTransactionEntity

interface ITransaction {
    var uid: Long
    var status: String

    fun fromDomain(): ChainTransactionEntity

    /*fun toDomain(tx: ChainTransactionEntity): ITransaction*/
}