package ru.home.swap.wallet.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.home.swap.core.extensions.attachIdlingResource
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.storage.ChainTransactionDao
import ru.home.swap.wallet.storage.Schema.ChainTransaction.DEFAULT_PAGE_SIZE
import ru.home.swap.wallet.storage.toDomain

class StorageRepository(
    val chainTransactionDao: ChainTransactionDao,
    val pagedDataSource: TxDataSource)
    : IStorageRepository {

/*    suspend fun createChainTx(uid: Int, to: String, value: Value, uri: String, status: String) {
        val tx = ChainTransactionEntity(uid, to, value, uri, status)
        chainTransactionDao.insertAll(tx)
    }*/

    override fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction> {
        return flow {
            val rowId = chainTransactionDao.insert(tx.fromDomain())
            val cachedTx = chainTransactionDao.getById(rowId)
            emit(cachedTx.toDomain())
        }
    }

    override suspend fun createChainTx(tx: ITransaction): ITransaction {
        val rowId = chainTransactionDao.insert(tx.fromDomain())
        val cachedTx = chainTransactionDao.getById(rowId)
        return cachedTx.toDomain()
    }

    override fun getAllChainTransactions(): Flow<List<ITransaction>> {
        return chainTransactionDao.getAll()
            .map {
                val result = mutableListOf<ITransaction>()
                for (item in it) {
                    result.add(item.toDomain())
                }
                result
            }
    }

    override fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>> {
        return Pager(
            config = PagingConfig(
                pageSize = DEFAULT_PAGE_SIZE,
                initialLoadSize = DEFAULT_PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                pagedDataSource
            }
        )
            .flow
            .attachIdlingResource()
    }

    override suspend fun removeChainTransaction(tx: ITransaction) {
        chainTransactionDao.delete(tx.fromDomain())
    }

}