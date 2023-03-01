package ru.home.swap.wallet.storage

class Schema {

    object ChainTransaction {
        const val TABLE_NAME: String = "ChainTransaction"
        const val DEFAULT_PAGE_SIZE = 20
    }

    object ServerMetadata {
        const val TABLE_NAME: String = "ServerTransactionMetadata"
    }
}