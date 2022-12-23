package ru.home.swap.wallet.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.home.swap.wallet.contract.Value
import ru.home.swap.wallet.model.Transaction
import ru.home.swap.wallet.model.fromDomain
import ru.home.swap.wallet.model.toDomain
import ru.home.swap.wallet.storage.ChainTransactionEntity
import ru.home.swap.wallet.storage.ChainTransactionDao

class StorageRepository(val chainTransactionDao: ChainTransactionDao) {

/*    suspend fun createChainTx(uid: Int, to: String, value: Value, uri: String, status: String) {
        val tx = ChainTransactionEntity(uid, to, value, uri, status)
        chainTransactionDao.insertAll(tx)
    }*/

    fun createChainTx(tx: Transaction): Flow<Transaction> {
        return flow {
            val rowId = chainTransactionDao.insert(tx.fromDomain(tx))
            val cachedTx = chainTransactionDao.getById(rowId)
            emit(tx.toDomain(cachedTx))
        }
    }

    fun getAllChainTransactions() = chainTransactionDao.getAll()

    suspend fun removeChainTransaction(tx: Transaction) {
        chainTransactionDao.delete(tx.fromDomain(tx))
    }

}