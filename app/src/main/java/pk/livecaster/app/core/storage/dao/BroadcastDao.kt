package pk.livecaster.app.core.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.storage.entity.BroadcastEntity

@Dao
interface BroadcastDao {
    @Query("SELECT * FROM broadcasts ORDER BY createdAt DESC")
    fun getAllBroadcasts(): Flow<List<BroadcastEntity>>

    @Query("SELECT * FROM broadcasts WHERE id = :id LIMIT 1")
    fun getBroadcastById(id: Long): Flow<BroadcastEntity?>

    @Query("SELECT * FROM broadcasts WHERE status = 'LIVE' LIMIT 1")
    fun getActiveBroadcast(): Flow<BroadcastEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBroadcast(broadcast: BroadcastEntity): Long

    @Update
    suspend fun updateBroadcast(broadcast: BroadcastEntity)

    @Query("UPDATE broadcasts SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("UPDATE broadcasts SET durationSeconds = :duration, peakViewers = :viewers WHERE id = :id")
    suspend fun updateTelemetry(id: Long, duration: Long, viewers: Long)

    @Delete
    suspend fun deleteBroadcast(broadcast: BroadcastEntity)

    @Query("DELETE FROM broadcasts WHERE id = :id")
    suspend fun deleteById(id: Long)
}
