package pk.livecaster.app.youtube.domain.usecase

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.youtube.domain.model.YouTubeBroadcast
import pk.livecaster.app.youtube.domain.model.YouTubeChannel
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

class GetYouTubeChannelsUseCase(private val repository: YouTubeRepository) {
    operator fun invoke(): Flow<List<YouTubeChannel>> = repository.getChannels()
    suspend fun refresh(): Resource<List<YouTubeChannel>> = repository.refreshChannels()
}

class CreateYouTubeLiveStreamUseCase(private val repository: YouTubeRepository) {
    suspend operator fun invoke(
        channelId: String,
        title: String,
        description: String
    ): Resource<YouTubeBroadcast> {
        if (title.isBlank()) return Resource.Error("Broadcast title is required for YouTube Live")
        return repository.createLiveBroadcast(channelId, title, description)
    }
}
