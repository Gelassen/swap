package ru.home.swap.wallet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.home.swap.core.model.IPayload
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.storage.model.DataItemFromView
import ru.home.swap.wallet.storage.model.ServerTransaction

interface IStorageRepository {

    suspend fun createChainTxAndServerTx(tx: ITransaction, serverTx: ServerTransaction): Pair<Long, Long>

    /**
     * This method is still necessary for on-chain operations which does not
     * have corresponding POST request to the server
     * */
    suspend fun createChainTx(tx: ITransaction): Long

    suspend fun getChainTx(id: Long): ITransaction

    fun getAllChainTransactions(): Flow<List<Pair<ITransaction, ServerTransaction>>>

    fun getAllChainTx(): Flow<List<Pair<ITransaction,ServerTransaction>>>

    fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>>

    suspend fun removeChainTransaction(tx: ITransaction)

    suspend fun getStorageRecordsCount(): Long

    suspend fun removeCachedData(limit: Int)

}