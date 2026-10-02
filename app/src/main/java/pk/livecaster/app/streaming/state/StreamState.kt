package pk.livecaster.app.streaming.state

enum class StreamStatus {
    IDLE,
    CONNECTING,
    LIVE,
    RECONNECTING,
    PAUSED,
    STOPPED,
    ERROR
}

data class StreamTelemetry(
    val status: StreamStatus = StreamStatus.IDLE,
    val durationSeconds: Long = 0,
    val currentFps: Int = 0,
    val currentBitrateKbps: Int = 0,
    val droppedFrames: Long = 0,
    val currentViewers: Long = 0,
    val health: StreamHealth = StreamHealth.EXCELLENT,
    val errorMessage: String? = null,
    val verificationCountdownSeconds: Int = 0,
    val isMicMuted: Boolean = false,
    val isTorchOn: Boolean = false,
    val isFrontCamera: Boolean = false
)

enum class StreamHealth {
    EXCELLENT,
    GOOD,
    POOR,
    CRITICAL
}
