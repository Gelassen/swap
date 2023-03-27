package ru.home.swap.wallet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.storage.model.DataItemFromView

interface IStorageRepository {

    suspend fun createChainTxAndServeTx(tx: ITransaction, service: Service, isProcessed: Boolean)

    @Deprecated("Use createChainTxAndServeTx within @Transaction")
    fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction>

    @Deprecated("Use createChainTxAndServeTx within @Transaction")
    suspend fun createChainTx(tx: ITransaction): ITransaction

    @Deprecated("Use createChainTxAndServeTx within @Transaction")
    suspend fun createServerTx(service: Service, txChainId: Long, isProcessed: Boolean): Service

    fun getAllChainTransactions(): Flow<List<Pair<ITransaction, Service>>>

    fun getAllChainTx(): Flow<List<Pair<ITransaction,Service>>>

    fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>>

    suspend fun removeChainTransaction(tx: ITransaction)

    suspend fun getStorageRecordsCount(): Long

    suspend fun removeCachedData(limit: Int)

}