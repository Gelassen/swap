package ru.home.swap.wallet.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.model.MintTransaction
import ru.home.swap.wallet.repository.IStorageRepository
import java.util.concurrent.ConcurrentLinkedQueue

class FakeStorageRepository: IStorageRepository {

    val txPool = ConcurrentLinkedQueue<ITransaction>()

    override fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction> {
        return flow {
            txPool.add(tx)
            emit(tx)
        }
    }

    override suspend fun createChainTx(tx: ITransaction): ITransaction {
        TODO("Not yet implemented")
    }

    override fun getAllChainTransactions(): Flow<List<ITransaction>> {
        return flow {
            emit(txPool.toList())
        }
    }

    override suspend fun removeChainTransaction(tx: ITransaction) {
        txPool.filter { it -> !it.equals(tx) }
    }

    fun reset() {
        txPool.clear()
    }
}