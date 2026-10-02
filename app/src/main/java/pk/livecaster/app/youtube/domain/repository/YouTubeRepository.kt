package pk.livecaster.app.youtube.domain.repository

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.youtube.domain.model.YouTubeBroadcast
import pk.livecaster.app.youtube.domain.model.YouTubeChannel

interface YouTubeRepository {
    fun getChannels(): Flow<List<YouTubeChannel>>
    suspend fun refreshChannels(): Resource<List<YouTubeChannel>>
    suspend fun createLiveBroadcast(
        channelId: String,
        title: String,
        description: String,
        privacyStatus: String = "public"
    ): Resource<YouTubeBroadcast>
    suspend fun linkChannel(title: String, channelId: String, customUrl: String?): Resource<YouTubeChannel>
    suspend fun unlinkChannel(channelId: String): Resource<Unit>
}
