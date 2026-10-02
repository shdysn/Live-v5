package pk.livecaster.app.youtube.data.api

import pk.livecaster.app.youtube.data.dto.YouTubeChannelsResponse
import pk.livecaster.app.youtube.data.dto.YouTubeLiveStreamItem
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface YouTubeApiService {
    @GET("channels")
    suspend fun getMyChannels(
        @Query("part") part: String = "snippet,contentDetails,statistics",
        @Query("mine") mine: Boolean = true
    ): YouTubeChannelsResponse

    @POST("liveStreams")
    suspend fun insertLiveStream(
        @Query("part") part: String = "snippet,cdn,status"
    ): YouTubeLiveStreamItem
}
