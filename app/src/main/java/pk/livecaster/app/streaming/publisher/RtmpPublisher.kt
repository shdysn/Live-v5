package pk.livecaster.app.streaming.publisher

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import pk.livecaster.app.core.constants.StreamConstants
import pk.livecaster.app.streaming.encoder.AudioEncoderConfig
import pk.livecaster.app.streaming.encoder.AudioMediaCodecEncoder
import pk.livecaster.app.streaming.encoder.VideoEncoderConfig
import pk.livecaster.app.streaming.encoder.VideoMediaCodecEncoder
import pk.livecaster.app.streaming.rtmp.MultiRtmpDispatcher
import pk.livecaster.app.streaming.rtmp.RtmpConnection
import pk.livecaster.app.streaming.state.StreamHealth
import pk.livecaster.app.streaming.state.StreamStatus
import pk.livecaster.app.streaming.state.StreamTelemetry
import java.util.concurrent.CopyOnWriteArrayList

data class RtmpEndpoint(
    val name: String,
    val rtmpUrl: String,
    val streamKey: String
)

class RtmpPublisher(
    private val scope: CoroutineScope
) {
    private var streamJob: Job? = null
    private val activeConnections = CopyOnWriteArrayList<RtmpConnection>()
    private var videoEncoder: VideoMediaCodecEncoder? = null
    private var audioEncoder: AudioMediaCodecEncoder? = null

    private val _telemetry = MutableStateFlow(StreamTelemetry())
    val telemetry: StateFlow<StreamTelemetry> = _telemetry.asStateFlow()

    private var targetBitrateKbps: Int = StreamConstants.DEFAULT_BITRATE_KBPS
    private var targetFps: Int = StreamConstants.DEFAULT_FPS

    fun startPublishing(
        rtmpUrl: String,
        streamKey: String,
        videoConfig: VideoEncoderConfig,
        audioConfig: AudioEncoderConfig
    ) {
        val endpoint = RtmpEndpoint(name = "Primary", rtmpUrl = rtmpUrl, streamKey = streamKey)
        startPublishing(listOf(endpoint), videoConfig, audioConfig)
    }

    fun startPublishing(
        endpoints: List<RtmpEndpoint>,
        videoConfig: VideoEncoderConfig,
        audioConfig: AudioEncoderConfig
    ) {
        if (_telemetry.value.status == StreamStatus.LIVE || _telemetry.value.status == StreamStatus.CONNECTING) {
            return
        }

        val validEndpoints = endpoints.filter { it.rtmpUrl.isNotBlank() && it.streamKey.isNotBlank() }
        if (validEndpoints.isEmpty()) {
            _telemetry.value = _telemetry.value.copy(
                status = StreamStatus.ERROR,
                errorMessage = "No valid streaming destinations provided"
            )
            return
        }

        targetBitrateKbps = (videoConfig.bitrateKbps + audioConfig.bitrateKbps) * validEndpoints.size
        targetFps = videoConfig.frameRate

        _telemetry.value = _telemetry.value.copy(
            status = StreamStatus.CONNECTING,
            durationSeconds = 0,
            verificationCountdownSeconds = 15,
            droppedFrames = 0,
            currentViewers = 0,
            errorMessage = null
        )

        streamJob?.cancel()
        streamJob = scope.launch(Dispatchers.IO) {
            try {
                activeConnections.clear()
                val errors = mutableListOf<String>()

                for (endpoint in validEndpoints) {
                    try {
                        val conn = RtmpConnection()
                        conn.onStatusListener = { msg, isError ->
                            if (isError) {
                                android.util.Log.e("RtmpPublisher", "[${endpoint.name}] $msg")
                                _telemetry.value = _telemetry.value.copy(
                                    errorMessage = "${endpoint.name}: $msg",
                                    health = StreamHealth.POOR
                                )
                            } else {
                                android.util.Log.d("RtmpPublisher", "[${endpoint.name}] $msg")
                            }
                        }
                        conn.onPublishFailed = { errMsg ->
                            _telemetry.value = _telemetry.value.copy(
                                status = StreamStatus.ERROR,
                                errorMessage = errMsg,
                                health = StreamHealth.CRITICAL
                            )
                        }
                        conn.connect(
                            rtmpUrl = endpoint.rtmpUrl,
                            streamKey = endpoint.streamKey,
                            width = videoConfig.width,
                            height = videoConfig.height,
                            fps = videoConfig.frameRate,
                            videoBitrateKbps = videoConfig.bitrateKbps
                        )
                        activeConnections.add(conn)
                        android.util.Log.d("RtmpPublisher", "Successfully connected socket to ${endpoint.name}")
                    } catch (e: Exception) {
                        android.util.Log.e("RtmpPublisher", "Failed to connect to ${endpoint.name}", e)
                        errors.add("${endpoint.name}: ${e.message ?: "Failed"}")
                    }
                }

                if (activeConnections.isEmpty()) {
                    throw IllegalStateException("Failed to connect to any destination: ${errors.joinToString(", ")}")
                }

                val dispatcher = MultiRtmpDispatcher(activeConnections)

                // 2. Start hardware video and audio encoders feeding all active destinations
                val vEnc = VideoMediaCodecEncoder(
                    rtmpSink = dispatcher,
                    width = videoConfig.width,
                    height = videoConfig.height,
                    fps = videoConfig.frameRate,
                    bitrateKbps = videoConfig.bitrateKbps
                )
                videoEncoder = vEnc
                vEnc.start()

                val aEnc = AudioMediaCodecEncoder(
                    rtmpSink = dispatcher,
                    sampleRate = audioConfig.sampleRate,
                    channelCount = audioConfig.channelCount,
                    bitrate = audioConfig.bitrateKbps * 1000
                )
                audioEncoder = aEnc
                aEnc.start()

                // 3. Verification countdown phase (15 seconds)
                // App stays in CONNECTING until Facebook/YouTube actually confirms NetStream.Publish.Start
                var verified = false
                var remainingCountdown = 15
                while (isActive && remainingCountdown > 0 && !verified) {
                    delay(1000)
                    remainingCountdown--

                    // Check if server rejected or disconnected
                    if (_telemetry.value.status == StreamStatus.ERROR) {
                        break
                    }

                    val hasAlive = activeConnections.any { it.isConnected() }
                    if (!hasAlive) {
                        _telemetry.value = _telemetry.value.copy(
                            status = StreamStatus.ERROR,
                            errorMessage = "Server closed connection during verification. Check your stream key in Facebook Live Producer.",
                            health = StreamHealth.CRITICAL
                        )
                        break
                    }

                    // Check if server confirmed live transmission
                    if (activeConnections.any { it.isPublishVerified() }) {
                        verified = true
                        break
                    }

                    _telemetry.value = _telemetry.value.copy(
                        status = StreamStatus.CONNECTING,
                        verificationCountdownSeconds = remainingCountdown,
                        currentFps = targetFps,
                        currentBitrateKbps = targetBitrateKbps
                    )
                }

                if (!verified && _telemetry.value.status != StreamStatus.ERROR) {
                    _telemetry.value = _telemetry.value.copy(
                        status = StreamStatus.ERROR,
                        errorMessage = "Live stream could not be verified on Facebook (15s timeout). Ensure your Stream Key in Facebook Live Producer is active and not expired.",
                        health = StreamHealth.POOR
                    )
                    stopPublishing()
                    return@launch
                }

                if (_telemetry.value.status == StreamStatus.ERROR) {
                    stopPublishing()
                    return@launch
                }

                // 4. Server has verified live stream! Switch to LIVE!
                _telemetry.value = _telemetry.value.copy(
                    status = StreamStatus.LIVE,
                    verificationCountdownSeconds = 0,
                    currentFps = targetFps,
                    currentBitrateKbps = targetBitrateKbps,
                    health = StreamHealth.EXCELLENT,
                    errorMessage = null
                )

                var secondsElapsed = 0L
                while (isActive && _telemetry.value.status == StreamStatus.LIVE) {
                    delay(1000)
                    secondsElapsed++

                    val hasAlive = activeConnections.any { it.isConnected() }
                    if (!hasAlive) {
                        _telemetry.value = _telemetry.value.copy(
                            status = StreamStatus.ERROR,
                            errorMessage = "Streaming server disconnected. Check your stream key in Facebook Live Producer.",
                            health = StreamHealth.CRITICAL
                        )
                        break
                    }

                    _telemetry.value = _telemetry.value.copy(
                        durationSeconds = secondsElapsed,
                        currentFps = targetFps,
                        currentBitrateKbps = targetBitrateKbps,
                        droppedFrames = 0,
                        health = StreamHealth.EXCELLENT
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("RtmpPublisher", "Publishing error", e)
                _telemetry.value = _telemetry.value.copy(
                    status = StreamStatus.ERROR,
                    errorMessage = e.message ?: "Failed to connect to ingest server"
                )
                stopPublishing()
            }
        }
    }

    fun encodeVideoFrame(yuvBytes: ByteArray) {
        if (_telemetry.value.status == StreamStatus.LIVE || _telemetry.value.status == StreamStatus.CONNECTING) {
            videoEncoder?.encodeYuv(yuvBytes)
        }
    }

    fun stopPublishing() {
        streamJob?.cancel()
        streamJob = null

        audioEncoder?.stop()
        audioEncoder = null

        videoEncoder?.stop()
        videoEncoder = null

        for (conn in activeConnections) {
            try {
                conn.close()
            } catch (_: Exception) {}
        }
        activeConnections.clear()

        _telemetry.value = _telemetry.value.copy(
            status = StreamStatus.STOPPED,
            currentFps = 0,
            currentBitrateKbps = 0
        )
    }

    fun toggleMicMute(): Boolean {
        val newMuted = !_telemetry.value.isMicMuted
        _telemetry.value = _telemetry.value.copy(isMicMuted = newMuted)
        return newMuted
    }

    fun toggleTorch(): Boolean {
        val newTorch = !_telemetry.value.isTorchOn
        _telemetry.value = _telemetry.value.copy(isTorchOn = newTorch)
        return newTorch
    }

    fun toggleCameraFacing(): Boolean {
        val newFacing = !_telemetry.value.isFrontCamera
        _telemetry.value = _telemetry.value.copy(isFrontCamera = newFacing)
        return newFacing
    }
}

