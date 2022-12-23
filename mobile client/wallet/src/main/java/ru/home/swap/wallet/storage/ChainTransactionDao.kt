package ru.home.swap.wallet.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChainTransactionDao {

    object Const {
        const val tableName: String = "ChainTransaction"
    }

    @Query("SELECT * FROM ${Const.tableName}")
    fun getAll(): Flow<List<ChainTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg transactions: ChainTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ChainTransactionEntity): Long

//    @Query("SELECT * FROM ${Const.tableName} WHERE uid = :uid")
//    fun getById(uid: Long): Flow<ChainTransactionEntity>

    @Query("SELECT * FROM ${Const.tableName} WHERE uid = :uid")
    suspend fun getById(uid: Long): ChainTransactionEntity

    @Delete
    suspend fun delete(transactions: ChainTransactionEntity)

}