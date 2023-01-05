package ru.home.swap.wallet.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.model.fromDomain
import ru.home.swap.wallet.model.toDomain
import ru.home.swap.wallet.storage.ChainTransactionDao

class StorageRepository(val chainTransactionDao: ChainTransactionDao): IStorageRepository {

/*    suspend fun createChainTx(uid: Int, to: String, value: Value, uri: String, status: String) {
        val tx = ChainTransactionEntity(uid, to, value, uri, status)
        chainTransactionDao.insertAll(tx)
    }*/

    override fun createChainTx(tx: MintTransaction): Flow<MintTransaction> {
        return flow {
            val rowId = chainTransactionDao.insert(tx.fromDomain(tx))
            val cachedTx = chainTransactionDao.getById(rowId)
            emit(tx.toDomain(cachedTx))
        }
    }

    override fun getAllChainTransactions(): Flow<List<MintTransaction>> {
        return chainTransactionDao.getAll()
            .map {
                val result = mutableListOf<MintTransaction>()
                for (item in it) {
                    when(item.status) {
                        "" -> {}
                    }
                    result.add(MintTransaction().toDomain(item))
                }
                result
            }
    }

    override suspend fun removeChainTransaction(tx: MintTransaction) {
        chainTransactionDao.delete(tx.fromDomain(tx))
    }

}