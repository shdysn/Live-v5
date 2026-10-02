package pk.livecaster.app.youtube.data.dto

data class YouTubeChannelSnippet(
    val title: String,
    val description: String,
    val customUrl: String? = null
)

data class YouTubeChannelStatistics(
    val subscriberCount: String? = null,
    val videoCount: String? = null
)

data class YouTubeChannelItem(
    val id: String,
    val snippet: YouTubeChannelSnippet,
    val statistics: YouTubeChannelStatistics? = null
)

data class YouTubeChannelsResponse(
    val items: List<YouTubeChannelItem> = emptyList()
)

data class YouTubeLiveStreamIngestionInfo(
    val ingestionAddress: String,
    val streamName: String,
    val backupIngestionAddress: String? = null
)

data class YouTubeLiveStreamCdn(
    val ingestionInfo: YouTubeLiveStreamIngestionInfo
)

data class YouTubeLiveStreamItem(
    val id: String,
    val cdn: YouTubeLiveStreamCdn
)
