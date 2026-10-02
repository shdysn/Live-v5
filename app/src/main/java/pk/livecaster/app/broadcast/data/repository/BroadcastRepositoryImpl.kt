package pk.livecaster.app.broadcast.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.model.Destination
import pk.livecaster.app.broadcast.domain.model.PlatformType
import pk.livecaster.app.broadcast.domain.repository.BroadcastRepository
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.core.storage.dao.BroadcastDao
import pk.livecaster.app.core.storage.dao.DestinationDao
import pk.livecaster.app.core.storage.entity.BroadcastEntity
import pk.livecaster.app.core.storage.entity.DestinationEntity

class BroadcastRepositoryImpl(
    private val broadcastDao: BroadcastDao,
    private val destinationDao: DestinationDao
) : BroadcastRepository {

    override fun getAllBroadcasts(): Flow<List<Broadcast>> {
        return broadcastDao.getAllBroadcasts().map { list ->
            list.map { it.toDomain() }
        }
    }

    override fun getBroadcastById(id: Long): Flow<Broadcast?> {
        return broadcastDao.getBroadcastById(id).map { it?.toDomain() }
    }

    override fun getActiveBroadcast(): Flow<Broadcast?> {
        return broadcastDao.getActiveBroadcast().map { it?.toDomain() }
    }

    override suspend fun createBroadcast(broadcast: Broadcast): Resource<Long> {
        return try {
            val id = broadcastDao.insertBroadcast(broadcast.toEntity())
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save broadcast session", e)
        }
    }

    override suspend fun updateBroadcastStatus(id: Long, status: BroadcastStatus): Resource<Unit> {
        return try {
            broadcastDao.updateStatus(id, status.name)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update status", e)
        }
    }

    override suspend fun updateTelemetry(id: Long, duration: Long, viewers: Long): Resource<Unit> {
        return try {
            broadcastDao.updateTelemetry(id, duration, viewers)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to update telemetry", e)
        }
    }

    override suspend fun deleteBroadcast(id: Long): Resource<Unit> {
        return try {
            broadcastDao.deleteById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete broadcast", e)
        }
    }

    override fun getAllDestinations(): Flow<List<Destination>> {
        return destinationDao.getAllDestinations().map { list ->
            list.map { it.toDomain() }
        }
    }

    override suspend fun saveDestination(destination: Destination): Resource<Long> {
        return try {
            val id = destinationDao.insertDestination(destination.toEntity())
            Resource.Success(id)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to save destination", e)
        }
    }

    override suspend fun deleteDestination(id: Long): Resource<Unit> {
        return try {
            destinationDao.deleteById(id)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to delete destination", e)
        }
    }

    private fun BroadcastEntity.toDomain(): Broadcast {
        return Broadcast(
            id = id,
            title = title,
            description = description,
            rtmpUrl = rtmpUrl,
            streamKey = streamKey,
            platform = try { PlatformType.valueOf(platform) } catch (_: Exception) { PlatformType.CUSTOM_RTMP },
            status = try { BroadcastStatus.valueOf(status) } catch (_: Exception) { BroadcastStatus.DRAFT },
            durationSeconds = durationSeconds,
            resolution = resolution,
            bitrateKbps = bitrateKbps,
            fps = fps,
            peakViewers = peakViewers,
            startedAt = startedAt,
            endedAt = endedAt,
            createdAt = createdAt
        )
    }

    private fun Broadcast.toEntity(): BroadcastEntity {
        return BroadcastEntity(
            id = id,
            title = title,
            description = description,
            rtmpUrl = rtmpUrl,
            streamKey = streamKey,
            platform = platform.name,
            status = status.name,
            durationSeconds = durationSeconds,
            resolution = resolution,
            bitrateKbps = bitrateKbps,
            fps = fps,
            peakViewers = peakViewers,
            startedAt = startedAt,
            endedAt = endedAt,
            createdAt = createdAt
        )
    }

    private fun DestinationEntity.toDomain(): Destination {
        return Destination(
            id = id,
            name = name,
            platform = try { PlatformType.valueOf(platform) } catch (_: Exception) { PlatformType.CUSTOM_RTMP },
            rtmpUrl = rtmpUrl,
            streamKey = streamKey,
            pageOrChannelId = pageOrChannelId,
            isEnabled = isEnabled
        )
    }

    private fun Destination.toEntity(): DestinationEntity {
        return DestinationEntity(
            id = id,
            name = name,
            platform = platform.name,
            rtmpUrl = rtmpUrl,
            streamKey = streamKey,
            pageOrChannelId = pageOrChannelId,
            isEnabled = isEnabled
        )
    }
}
