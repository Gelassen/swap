package ru.home.swap.wallet.storage.dao

import androidx.room.*
import ru.home.swap.wallet.storage.model.Schema
import ru.home.swap.wallet.storage.model.ServerRequestTransactionEntity

@Dao
interface ServerTransactionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg transactions: ServerRequestTransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ServerRequestTransactionEntity): Long

    @Query("SELECT * FROM ${Schema.ServerMetadata.TABLE_NAME} WHERE uid = :uid")
    suspend fun getById(uid: Long): ServerRequestTransactionEntity

}