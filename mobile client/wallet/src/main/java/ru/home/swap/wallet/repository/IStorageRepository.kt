package ru.home.swap.wallet.repository

import kotlinx.coroutines.flow.Flow
import ru.home.swap.wallet.model.Transaction

interface IStorageRepository {

    fun createChainTx(tx: Transaction): Flow<Transaction>

    fun getAllChainTransactions(): Flow<List<Transaction>>

    suspend fun removeChainTransaction(tx: Transaction)

}