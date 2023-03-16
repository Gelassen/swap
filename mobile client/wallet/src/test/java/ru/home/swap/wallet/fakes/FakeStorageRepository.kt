package ru.home.swap.wallet.fakes

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.home.swap.core.model.Service
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.repository.IStorageRepository
import java.util.concurrent.ConcurrentLinkedQueue

class FakeStorageRepository: IStorageRepository {

    val txPool = ConcurrentLinkedQueue<Pair<ITransaction, Service>>()

    override fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction> {
        return flow {
            txPool.add(Pair(tx, Service()))
            emit(tx)
        }
    }

    override suspend fun createChainTx(tx: ITransaction): ITransaction {
        TODO("Not yet implemented")
    }

    override suspend fun createServerTx(service: Service, txChainId: Int): Service {
        TODO("Not yet implemented")
    }

    override fun getAllChainTransactions(): Flow<List<Pair<ITransaction, Service>>> {
        return flow {
            emit(txPool.toList())
        }
    }

    override fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeChainTransaction(tx: ITransaction) {
        txPool.filter { it -> !it.equals(tx) }
    }

    fun reset() {
        txPool.clear()
    }
}