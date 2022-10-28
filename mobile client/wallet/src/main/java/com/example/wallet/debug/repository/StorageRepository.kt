package com.example.wallet.debug.repository

import com.example.wallet.debug.contract.Value
import com.example.wallet.debug.storage.ChainTransaction
import com.example.wallet.debug.storage.ChainTransactionDao

class StorageRepository(val chainTransactionDao: ChainTransactionDao) {

    suspend fun createChainTx(to: String, value: Value, uri: String) {
        val tx = ChainTransaction(0, to, value, uri)
        chainTransactionDao.insertAll(tx)
    }

    fun getAllChainTransactions() = chainTransactionDao.getAll()

    suspend fun removeChainTransaction(uid: Int, to: String, value: Value, uri: String) {
        val tx = ChainTransaction(uid, to, value, uri)
        chainTransactionDao.delete(tx)
    }

}