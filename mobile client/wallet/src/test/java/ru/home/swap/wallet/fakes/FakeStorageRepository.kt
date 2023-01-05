package ru.home.swap.wallet.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.home.swap.wallet.model.Transaction
import ru.home.swap.wallet.repository.IStorageRepository
import java.util.concurrent.ConcurrentLinkedQueue

class FakeStorageRepository: IStorageRepository {

    val txPool = ConcurrentLinkedQueue<Transaction>()

    override fun createChainTx(tx: Transaction): Flow<Transaction> {
        return flow {
            txPool.add(tx)
            emit(tx)
        }
    }

    override fun getAllChainTransactions(): Flow<List<Transaction>> {
        return flow {
            emit(txPool.toList())
        }
    }

    override suspend fun removeChainTransaction(tx: Transaction) {
        txPool.filter { it -> !it.equals(tx) }
    }

    fun reset() {
        txPool.clear()
    }
}