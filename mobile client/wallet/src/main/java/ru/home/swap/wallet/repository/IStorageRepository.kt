package ru.home.swap.wallet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction

interface IStorageRepository {

    fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction>

    suspend fun createChainTx(tx: ITransaction): ITransaction

    suspend fun createServerTx(service: Service, txChainId: Long): Service

    fun getAllChainTransactions(): Flow<List<Pair<ITransaction,Service>>>

    fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>>

    suspend fun removeChainTransaction(tx: ITransaction)

}