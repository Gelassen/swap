package ru.home.swap.wallet.storage.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.home.swap.wallet.storage.model.ChainServiceEntity
import ru.home.swap.wallet.storage.model.MatchEntity
import ru.home.swap.wallet.storage.model.Schema
import ru.home.swap.wallet.storage.model.SwapMatchEntity

@Dao
interface SwapMatchDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: MatchEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ChainServiceEntity): Long

    @Transaction
    suspend fun insertAll(vararg transactions: SwapMatchEntity): List<Long> {
        val result = mutableListOf<Long>()
        for (tx in transactions) {
            val id = insert(tx.matchEntity)
            result.add(id)

            insert(tx.userFirstService)
            insert(tx.userSecondService)
        }
        return result
    }

    @Query("SELECT * FROM ${Schema.ChainMatch.TABLE_NAME}")
    fun getAll(): Flow<List<SwapMatchEntity>>
}