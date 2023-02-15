package ru.home.swap.wallet.model

import ru.home.swap.wallet.storage.ChainTransactionEntity

interface ITransaction {
    var uid: Long
    var status: String
    var type: String
        get() = this.javaClass.simpleName
        set(value) = TODO()

    fun fromDomain(): ChainTransactionEntity

    /*fun toDomain(tx: ChainTransactionEntity): ITransaction*/
}

fun ITransaction.equals(other: ITransaction) : Boolean {
    return this.uid == other.uid
            && this.status == other.status
            && this.type == other.type
}