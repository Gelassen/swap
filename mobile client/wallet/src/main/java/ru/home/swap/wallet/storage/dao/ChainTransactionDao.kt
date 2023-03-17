package ru.home.swap.wallet.storage.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.home.swap.wallet.storage.model.ChainTransactionEntity
import ru.home.swap.wallet.storage.model.Schema
import ru.home.swap.wallet.storage.model.TxWithMetadataEntity

@Dao
interface ChainTransactionDao {

    @Query("" +
            "SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} " +
            "INNER JOIN ${Schema.ServerMetadata.TABLE_NAME} " +
            "ON ${Schema.ChainTransaction.TABLE_NAME}.${Schema.ChainTransaction.UID} " +
            "   = ${Schema.ServerMetadata.TABLE_NAME}.${Schema.ServerMetadata.TX_CHAIN_ID} " +
            "WHERE ${Schema.ServerMetadata.TABLE_NAME}.${Schema.ServerMetadata.STATUS} = :status;")
    fun getAll(status: String): Flow<List<TxWithMetadataEntity>>

    @Query("" +
            "SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} " +
            "INNER JOIN ${Schema.ServerMetadata.TABLE_NAME} " +
            "ON ${Schema.ChainTransaction.TABLE_NAME}.${Schema.ChainTransaction.UID} " +
            "   = ${Schema.ServerMetadata.TABLE_NAME}.${Schema.ServerMetadata.TX_CHAIN_ID}; ")
    fun getAll(): Flow<List<TxWithMetadataEntity>>

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

    @Query("SELECT COUNT(*) FROM ${Schema.ChainTransaction.TABLE_NAME}")
    suspend fun getNumberOfRecordsInStorage(): Long

    @Query("DELETE FROM ${Schema.ChainTransaction.TABLE_NAME} " +
            "WHERE ${Schema.ChainTransaction.UID} IN " +
            "(" +
                "SELECT ${Schema.ChainTransaction.UID} FROM ${Schema.ChainTransaction.TABLE_NAME} " +
                "ORDER BY ${Schema.ChainTransaction.UID} ASC LIMIT :limit" +
            ")")
    suspend fun removeCachedData(limit: Int)

}