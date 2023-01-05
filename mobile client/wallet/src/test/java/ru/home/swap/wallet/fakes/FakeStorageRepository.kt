package ru.home.swap.wallet.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.repository.IStorageRepository
import java.util.concurrent.ConcurrentLinkedQueue

class FakeStorageRepository: IStorageRepository {

    val txPool = ConcurrentLinkedQueue<MintTransaction>()

    override fun createChainTx(tx: MintTransaction): Flow<MintTransaction> {
        return flow {
            txPool.add(tx)
            emit(tx)
        }
    }

    override fun getAllChainTransactions(): Flow<List<MintTransaction>> {
        return flow {
            emit(txPool.toList())
        }
    }

    override suspend fun removeChainTransaction(tx: MintTransaction) {
        txPool.filter { it -> !it.equals(tx) }
    }

    fun reset() {
        txPool.clear()
    }
}