package pk.livecaster.app.broadcast.domain.repository

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.model.Destination
import pk.livecaster.app.core.common.Resource

interface BroadcastRepository {
    fun getAllBroadcasts(): Flow<List<Broadcast>>
    fun getBroadcastById(id: Long): Flow<Broadcast?>
    fun getActiveBroadcast(): Flow<Broadcast?>
    suspend fun createBroadcast(broadcast: Broadcast): Resource<Long>
    suspend fun updateBroadcastStatus(id: Long, status: BroadcastStatus): Resource<Unit>
    suspend fun updateTelemetry(id: Long, duration: Long, viewers: Long): Resource<Unit>
    suspend fun deleteBroadcast(id: Long): Resource<Unit>

    fun getAllDestinations(): Flow<List<Destination>>
    suspend fun saveDestination(destination: Destination): Resource<Long>
    suspend fun deleteDestination(id: Long): Resource<Unit>
}
