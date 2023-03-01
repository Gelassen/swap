package ru.home.swap.wallet.storage

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ChainTransactionDao {

    @Query("SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME}")
    fun getAll(): Flow<List<ChainTransactionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg transactions: ChainTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ChainTransactionEntity): Long

/*    @Query("SELECT * FROM ${Const.tableName} WHERE uid = :uid")
    fun getById(uid: Long): Flow<ChainTransactionEntity>*/

    @Query("SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} WHERE uid = :uid")
    suspend fun getById(uid: Long): ChainTransactionEntity

    @Query("SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} ORDER BY uid ASC LIMIT :limit OFFSET :offset")
    suspend fun getByPage(limit: Int, offset: Int): List<ChainTransactionEntity>

    @Delete
    suspend fun delete(transactions: ChainTransactionEntity)

}