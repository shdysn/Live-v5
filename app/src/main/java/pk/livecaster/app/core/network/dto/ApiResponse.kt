package pk.livecaster.app.core.network.dto

data class ApiResponse<T>(
    val success: Boolean,
    val message: String? = null,
    val data: T? = null,
    val error: String? = null
)

data class StreamTelemetryDto(
    val broadcastId: String,
    val rtmpUrl: String,
    val streamKey: String,
    val isLive: Boolean,
    val fps: Int,
    val bitrateKbps: Int,
    val viewersCount: Long,
    val uptimeSeconds: Long
)
