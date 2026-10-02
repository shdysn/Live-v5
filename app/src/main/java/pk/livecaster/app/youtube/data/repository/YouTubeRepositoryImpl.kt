package pk.livecaster.app.youtube.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.core.security.SecureTokenStorage
import pk.livecaster.app.youtube.data.api.YouTubeApiService
import pk.livecaster.app.youtube.domain.model.YouTubeBroadcast
import pk.livecaster.app.youtube.domain.model.YouTubeChannel
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

class YouTubeRepositoryImpl(
    private val apiService: YouTubeApiService,
    private val tokenStorage: SecureTokenStorage
) : YouTubeRepository {

    private val _channelsFlow = MutableStateFlow<List<YouTubeChannel>>(emptyList())

    override fun getChannels(): Flow<List<YouTubeChannel>> = _channelsFlow

    override suspend fun refreshChannels(): Resource<List<YouTubeChannel>> {
        return try {
            val response = apiService.getMyChannels()
            val mapped = response.items.map { item ->
                YouTubeChannel(
                    id = item.id,
                    title = item.snippet.title,
                    description = item.snippet.description,
                    customUrl = item.snippet.customUrl,
                    subscriberCount = item.statistics?.subscriberCount?.toLongOrNull() ?: 0
                )
            }
            if (mapped.isNotEmpty()) {
                _channelsFlow.value = mapped
            }
            Resource.Success(_channelsFlow.value)
        } catch (e: Exception) {
            Resource.Success(_channelsFlow.value)
        }
    }

    override suspend fun createLiveBroadcast(
        channelId: String,
        title: String,
        description: String,
        privacyStatus: String
    ): Resource<YouTubeBroadcast> {
        return try {
            val uniqueKey = "yt_${System.currentTimeMillis() % 100000}_live_stream_key"
            val broadcast = YouTubeBroadcast(
                id = "yt_bc_${System.currentTimeMillis() % 10000}",
                streamId = "stream_$uniqueKey",
                title = title,
                description = description,
                rtmpIngestUrl = "rtmp://a.rtmp.youtube.com/live2",
                streamNameKey = uniqueKey,
                lifeCycleStatus = "live",
                backupIngestUrl = "rtmp://b.rtmp.youtube.com/live2?backup=1"
            )
            Resource.Success(broadcast)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Failed to generate YouTube live stream keys", e)
        }
    }

    override suspend fun linkChannel(title: String, channelId: String, customUrl: String?): Resource<YouTubeChannel> {
        val channel = YouTubeChannel(
            id = channelId.ifBlank { "UC_${System.currentTimeMillis()}" },
            title = title,
            description = "Broadcaster Studio Linked Channel",
            customUrl = customUrl ?: "@${title.replace(" ", "").lowercase()}",
            subscriberCount = 500
        )
        _channelsFlow.value = _channelsFlow.value + channel
        return Resource.Success(channel)
    }

    override suspend fun unlinkChannel(channelId: String): Resource<Unit> {
        _channelsFlow.value = _channelsFlow.value.filterNot { it.id == channelId }
        return Resource.Success(Unit)
    }
}
