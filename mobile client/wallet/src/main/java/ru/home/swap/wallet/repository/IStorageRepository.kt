package ru.home.swap.wallet.repository

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction

interface IStorageRepository {

    fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction>

    suspend fun createChainTx(tx: ITransaction): ITransaction

    fun getAllChainTransactions(): Flow<List<ITransaction>>

    fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>>

    suspend fun removeChainTransaction(tx: ITransaction)

}