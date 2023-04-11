package ru.home.swap.wallet.fakes

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import ru.home.swap.wallet.model.ITransaction
import ru.home.swap.wallet.repository.IStorageRepository
import ru.home.swap.wallet.storage.model.ServerTransaction
import java.util.concurrent.ConcurrentLinkedQueue

class FakeStorageRepository: IStorageRepository {

    var counter: Long = 0
    val txPool = ConcurrentLinkedQueue<Pair<ITransaction, ServerTransaction>>()

    override suspend fun createChainTxAndServerTx(
        tx: ITransaction,
        serverTx: ServerTransaction
    ): Pair<Long, Long> {
        tx.uid = counter++
        serverTx.uid = counter++
        txPool.add(Pair(tx, serverTx))
        return Pair(tx.uid, serverTx.uid)
    }

/*    override fun createChainTxAsFlow(tx: ITransaction): Flow<ITransaction> {
        return flow {
            txPool.add(Pair(tx, Service()))
            emit(tx)
        }
    }*/

    override suspend fun createChainTx(tx: ITransaction): Long {
        TODO("Not yet implemented")
    }

    override suspend fun getChainTx(id: Long): ITransaction {
        TODO("Not yet implemented")
    }

    override fun getAllChainTransactions(): Flow<List<Pair<ITransaction, ServerTransaction>>> {
        return flow {
            emit(txPool.toList())
        }
    }

    override fun getAllChainTx(): Flow<List<Pair<ITransaction, ServerTransaction>>> {
        TODO("Not yet implemented")
    }

    override fun getChainTransactionsByPage(): Flow<PagingData<ITransaction>> {
        TODO("Not yet implemented")
    }

    override suspend fun removeChainTransaction(tx: ITransaction) {
        txPool.filter { it -> !it.equals(tx) }
    }

    override suspend fun getStorageRecordsCount(): Long {
        TODO("Not yet implemented")
    }

    override suspend fun removeCachedData(limit: Int) {
        TODO("Not yet implemented")
    }

    fun reset() {
        txPool.clear()
    }
}