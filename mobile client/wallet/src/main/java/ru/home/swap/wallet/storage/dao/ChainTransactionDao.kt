package ru.home.swap.wallet.storage.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.home.swap.core.logger.Logger
import ru.home.swap.wallet.storage.model.*

/**
 * Main problem is a listener is notified several times when data in underlying tables
 * has been changed. Possible options are:
 * - implement @DatabaseView (issue remains)
 * - add @Transaction annotation (issue remains)
 * - execute both inserts into correlated tables within transaction (checking)
 * */
@Dao
interface ChainTransactionDao {

    /**
     * Warning: it should be used outside of @Transaction
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ServerRequestTransactionEntity): Long

    @Transaction
    suspend fun insertWithinTransaction(transactionsOnChain: ChainTransactionEntity,
                                        transactionsOnServer: ServerRequestTransactionEntity): Pair<Long, Long> {
        Logger.getInstance().d("[insertWithinTransaction] ${transactionsOnChain} and ${transactionsOnServer}")
        val chainTxId = insert(transactionsOnChain)
        transactionsOnServer.txChainId = chainTxId // keep an eye to make sure this is a valid uid
        val serverTxId = insert(transactionsOnServer)
        return Pair(chainTxId, serverTxId)
    }

    @Transaction
    @Query("SELECT * FROM ${Schema.Views.ChainTxWithServerMetadata.VIEW_NAME}")
    fun getAllFromView(): Flow<List<DataItemFromView>>

    @Deprecated(message =
            "By not discovered yet cause, WHERE clause on sql query layer\n" +
            "doesn't work properly for loadAllTransactions(); querying all\n" +
            "transactions and filtration on the repository layer has solved\n" +
            "the issue")
    @Query("" +
            "SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} " +
            "LEFT OUTER JOIN ${Schema.ServerMetadata.TABLE_NAME} " +
            "ON ${Schema.ChainTransaction.TABLE_NAME}.${Schema.ChainTransaction.UID} " +
            "   = ${Schema.ServerMetadata.TABLE_NAME}.${Schema.ServerMetadata.TX_CHAIN_ID} " +
            "WHERE ${Schema.ServerMetadata.TABLE_NAME}.${Schema.ServerMetadata.STATUS} = :status")
    fun getAll(status: String): Flow<List<TxWithMetadataEntity>>

    @Transaction
    @Query("" +
            "SELECT * FROM ${Schema.ChainTransaction.TABLE_NAME} " +
            "LEFT OUTER JOIN ${Schema.ServerMetadata.TABLE_NAME} " +
            "ON ${Schema.ChainTransaction.TABLE_NAME}.${Schema.ChainTransaction.UID} " +
            "   = ${Schema.ServerMetadata.TABLE_NAME}.${Schema.ServerMetadata.TX_CHAIN_ID}; ")
    fun getAll(): Flow<List<TxWithMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg transactions: ChainTransactionEntity)

    /**
     * Warning: it should be used only within @Transaction. The exception is made
     * for on-chain tx which should not have a record on sever, e.g. approve
     * token manager
     * */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transactions: ChainTransactionEntity): Long


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