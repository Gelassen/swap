package ru.home.swap.wallet.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import kotlinx.coroutines.flow.*
import ru.home.swap.core.extensions.attachIdlingResource
import ru.home.swap.core.logger.Logger
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.storage.model.Schema.ChainTransaction.DEFAULT_PAGE_SIZE
import ru.home.swap.wallet.storage.dao.ChainTransactionDao
import ru.home.swap.wallet.storage.dao.ServerTransactionDao
import ru.home.swap.wallet.storage.model.*

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

    override suspend fun createChainTxAndServeTx(tx: ITransaction, service: Service, isProcessed: Boolean) {
        chainTransactionDao.insertWithinTransaction(
            transactionsOnChain = tx.fromDomain(),
            transactionsOnServer = service.fromDomain(0L, isProcessed)
        )
    }

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
        logger.d("[insert] createServerTx ${service.fromDomain(txChainId, isProcessed)}")
        val rowId = serverDao.insert(service.fromDomain(txChainId, isProcessed))
        val cachedTx = serverDao.getById(rowId)
        return cachedTx.toDomainObject()
    }

    override fun getAllChainTransactions(): Flow<List<Pair<ITransaction, Service>>> {
        return chainTransactionDao.getAll()
            .map {
//                val filteredIt = it.filter { it -> it.serverMetadata.status.equals(RequestStatus.WAITING) }
                logger.d("[loadAllFromCache] 1. get items ${it.count()}")
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
                val filteredIt = it.filter { it -> it.serverMetadata.status.equals(RequestStatus.WAITING) }
                val result = mutableListOf<Pair<ITransaction,Service>>()
                for (item in filteredIt) {
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

    override suspend fun getStorageRecordsCount(): Long {
        return chainTransactionDao.getNumberOfRecordsInStorage()
    }

    override suspend fun removeCachedData(limit: Int) {
        chainTransactionDao.removeCachedData(limit)
    }

}