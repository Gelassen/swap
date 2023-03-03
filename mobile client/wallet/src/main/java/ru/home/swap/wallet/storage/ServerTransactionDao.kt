package ru.home.swap.wallet.storage

import androidx.room.*

@Dao
interface ServerTransactionDao {

/*    @Query("SELECT * FROM ${Schema.ServerMetadata.TABLE_NAME}")
    fun getAll(): Flow<List<TxWithMetadata>>*/

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg transactions: ServerRequestTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ServerRequestTransactionEntity): Long

    @Query("SELECT * FROM ${Schema.ServerMetadata.TABLE_NAME} WHERE uid = :uid")
    suspend fun getById(uid: Long): ServerRequestTransactionEntity
/*
    @Query("SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} WHERE uid = :uid")
    suspend fun getById(uid: Long): ChainTransactionEntity

    @Query("SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} ORDER BY uid ASC LIMIT :limit OFFSET :offset")
    suspend fun getByPage(limit: Int, offset: Int): List<ChainTransactionEntity>

    @Delete
    suspend fun delete(transactions: ChainTransactionEntity)*/

}