package pk.livecaster.app.core.storage.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.storage.entity.DestinationEntity

@Dao
interface DestinationDao {
    @Query("SELECT * FROM destinations ORDER BY createdAt DESC")
    fun getAllDestinations(): Flow<List<DestinationEntity>>

    @Query("SELECT * FROM destinations WHERE isEnabled = 1")
    fun getActiveDestinations(): Flow<List<DestinationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDestination(destination: DestinationEntity): Long

    @Update
    suspend fun updateDestination(destination: DestinationEntity)

    @Delete
    suspend fun deleteDestination(destination: DestinationEntity)

    @Query("DELETE FROM destinations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
