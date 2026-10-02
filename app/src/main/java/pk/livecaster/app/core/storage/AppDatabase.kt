package pk.livecaster.app.core.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import pk.livecaster.app.core.constants.AppConstants
import pk.livecaster.app.core.storage.dao.BroadcastDao
import pk.livecaster.app.core.storage.dao.DestinationDao
import pk.livecaster.app.core.storage.dao.StreamLogDao
import pk.livecaster.app.core.storage.entity.BroadcastEntity
import pk.livecaster.app.core.storage.entity.DestinationEntity
import pk.livecaster.app.core.storage.entity.StreamLogEntity

@Database(
    entities = [
        BroadcastEntity::class,
        DestinationEntity::class,
        StreamLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun broadcastDao(): BroadcastDao
    abstract fun destinationDao(): DestinationDao
    abstract fun streamLogDao(): StreamLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    AppConstants.DATABASE_NAME
                )
                .fallbackToDestructiveMigration()
                .build()
                .also { INSTANCE = it }
            }
        }
    }
}
