package ru.home.swap.wallet.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import ru.home.swap.core.App
import ru.home.swap.wallet.storage.dao.ChainTransactionDao
import ru.home.swap.wallet.storage.dao.ServerTransactionDao
import ru.home.swap.wallet.storage.dao.SwapMatchDao
import ru.home.swap.wallet.storage.model.*

@Database(
    entities = [
        ChainTransactionEntity::class,
        ServerRequestTransactionEntity::class,
        MatchEntity::class,
        ChainServiceEntity::class],
    views = [DataItemFromView::class],
    version = 22,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {

    abstract fun chainTransactionDao(): ChainTransactionDao

    abstract fun serverTransactionDao(): ServerTransactionDao

    abstract fun matchDao(): SwapMatchDao

    companion object {
        // For Singleton instantiation
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, App.DATABASE_NAME)
                .addCallback(
                    object : RoomDatabase.Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
/*                        val request = OneTimeWorkRequestBuilder<SeedDatabaseWorker>()
                            .setInputData(workDataOf(KEY_FILENAME to PLANT_DATA_FILENAME))
                            .build()
                        WorkManager.getInstance(context).enqueue(request)*/
                        }
                    }
                )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}