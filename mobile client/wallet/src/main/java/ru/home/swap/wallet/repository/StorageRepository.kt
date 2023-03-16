package ru.home.swap.wallet.repository

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import ru.home.swap.core.App
import ru.home.swap.core.extensions.attachIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.storage.*
import ru.home.swap.wallet.storage.Schema.ChainTransaction.DEFAULT_PAGE_SIZE

class StorageRepository(
    val chainTransactionDao: ChainTransactionDao,
    val serverDao: ServerTransactionDao,
    val pagedDataSource: TxDataSource)
    : IStorageRepository {

/*    suspend fun createChainTx(uid: Int, to: String, value: Value, uri: String, status: String) {
        val tx = ChainTransactionEntity(uid, to, value, uri, status)
        chainTransactionDao.insertAll(tx)
    }*/
    val logger: Logger = Logger.getInstance()

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

    override suspend fun createServerTx(service: Service, txChainId: Long, isProcessed: Boolean): Service {
        val rowId = serverDao.insert(service.fromDomain(txChainId, isProcessed))
        val cachedTx = serverDao.getById(rowId)
        return cachedTx.toDomainObject()
    }

    override fun getAllChainTransactions(): Flow<List<Pair<ITransaction,Service>>> {
        return chainTransactionDao.getAll(RequestStatus.WAITING)
            .map {
                val result = mutableListOf<Pair<ITransaction,Service>>()
                for (item in it) {
                    result.add(item.toDomain())
                }
                result
            }
    }

    override fun getAllChainTx(): Flow<List<Pair<ITransaction, Service>>> {
        return chainTransactionDao.getAll()
            .map {
                val result = mutableListOf<Pair<ITransaction,Service>>()
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