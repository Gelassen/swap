package ru.home.swap.wallet.repository

import kotlinx.coroutines.flow.Flow
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction

interface IStorageRepository {

    fun createChainTx(tx: ITransaction): Flow<ITransaction>

    fun getAllChainTransactions(): Flow<List<ITransaction>>

    suspend fun removeChainTransaction(tx: ITransaction)

}