package pk.livecaster.app.core.storage.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.storage.entity.StreamLogEntity

@Dao
interface StreamLogDao {
    @Query("SELECT * FROM stream_logs WHERE broadcastId = :broadcastId ORDER BY timestamp ASC")
    fun getLogsForBroadcast(broadcastId: Long): Flow<List<StreamLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: StreamLogEntity): Long

    @Query("DELETE FROM stream_logs WHERE broadcastId = :broadcastId")
    suspend fun clearLogs(broadcastId: Long)
}
