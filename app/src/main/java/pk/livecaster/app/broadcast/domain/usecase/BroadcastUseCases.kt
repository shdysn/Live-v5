package pk.livecaster.app.broadcast.domain.usecase

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.repository.BroadcastRepository
import pk.livecaster.app.core.common.Resource

class GetBroadcastsUseCase(private val repository: BroadcastRepository) {
    operator fun invoke(): Flow<List<Broadcast>> = repository.getAllBroadcasts()
    fun getById(id: Long): Flow<Broadcast?> = repository.getBroadcastById(id)
    fun getActive(): Flow<Broadcast?> = repository.getActiveBroadcast()
}

class CreateBroadcastUseCase(private val repository: BroadcastRepository) {
    suspend operator fun invoke(broadcast: Broadcast): Resource<Long> {
        if (broadcast.title.isBlank()) {
            return Resource.Error("Broadcast title cannot be empty")
        }
        if (broadcast.rtmpUrl.isBlank()) {
            return Resource.Error("RTMP Server URL cannot be empty")
        }
        if (broadcast.streamKey.isBlank()) {
            return Resource.Error("Stream key is required")
        }
        return repository.createBroadcast(broadcast)
    }
}

class UpdateBroadcastStatusUseCase(private val repository: BroadcastRepository) {
    suspend operator fun invoke(id: Long, status: BroadcastStatus): Resource<Unit> {
        return repository.updateBroadcastStatus(id, status)
    }
}
