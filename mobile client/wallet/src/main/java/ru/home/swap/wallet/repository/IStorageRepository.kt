package ru.home.swap.wallet.repository

import kotlinx.coroutines.flow.Flow
import ru.home.swap.wallet.model.MintTransaction

interface IStorageRepository {

    fun createChainTx(tx: MintTransaction): Flow<MintTransaction>

    fun getAllChainTransactions(): Flow<List<MintTransaction>>

    suspend fun removeChainTransaction(tx: MintTransaction)

}