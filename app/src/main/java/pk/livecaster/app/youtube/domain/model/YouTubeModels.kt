package pk.livecaster.app.youtube.domain.model

data class YouTubeChannel(
    val id: String,
    val title: String,
    val description: String,
    val customUrl: String? = null,
    val subscriberCount: Long = 0,
    val defaultRtmpServer: String = "rtmp://a.rtmp.youtube.com/live2",
    val isEnabled: Boolean = true
)

data class YouTubeBroadcast(
    val id: String,
    val streamId: String,
    val title: String,
    val description: String,
    val rtmpIngestUrl: String,
    val streamNameKey: String,
    val lifeCycleStatus: String, // live, complete, created
    val backupIngestUrl: String? = null
)
