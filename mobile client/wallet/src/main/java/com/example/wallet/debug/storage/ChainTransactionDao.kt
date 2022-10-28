package com.example.wallet.debug.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChainTransactionDao {

    object Const {
        const val tableName: String = "ChainTransaction"
    }

    @Query("SELECT * FROM ${Const.tableName}")
    fun getAll(): Flow<List<ChainTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg transactions: ChainTransaction)

    @Delete
    suspend fun delete(transactions: ChainTransaction)

}