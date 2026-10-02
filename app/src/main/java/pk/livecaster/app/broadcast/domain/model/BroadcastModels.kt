package pk.livecaster.app.broadcast.domain.model

enum class BroadcastStatus {
    DRAFT,
    CONNECTING,
    LIVE,
    ENDED,
    FAILED
}

enum class PlatformType {
    FACEBOOK,
    YOUTUBE,
    CUSTOM_RTMP,
    MULTI_DESTINATION
}

data class Broadcast(
    val id: Long = 0,
    val title: String,
    val description: String,
    val rtmpUrl: String,
    val streamKey: String,
    val platform: PlatformType,
    val status: BroadcastStatus,
    val durationSeconds: Long = 0,
    val resolution: String = "720p",
    val bitrateKbps: Int = 3500,
    val fps: Int = 30,
    val peakViewers: Long = 0,
    val startedAt: Long? = null,
    val endedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

data class Destination(
    val id: Long = 0,
    val name: String,
    val platform: PlatformType,
    val rtmpUrl: String,
    val streamKey: String,
    val pageOrChannelId: String? = null,
    val isEnabled: Boolean = true
)
