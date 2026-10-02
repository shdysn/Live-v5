package pk.livecaster.app.broadcast.presentation.control

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.model.PlatformType
import pk.livecaster.app.broadcast.domain.repository.BroadcastRepository
import pk.livecaster.app.broadcast.domain.usecase.GetBroadcastsUseCase
import pk.livecaster.app.broadcast.domain.usecase.UpdateBroadcastStatusUseCase
import pk.livecaster.app.streaming.encoder.AudioEncoderConfig
import pk.livecaster.app.streaming.encoder.VideoEncoderConfig
import pk.livecaster.app.streaming.publisher.RtmpEndpoint
import pk.livecaster.app.streaming.publisher.RtmpPublisher
import pk.livecaster.app.streaming.service.LiveStreamingService
import pk.livecaster.app.streaming.state.StreamStatus
import pk.livecaster.app.streaming.state.StreamTelemetry
import kotlin.random.Random

data class LiveChatMessage(
    val id: String,
    val sender: String,
    val message: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class BroadcastControlUiState(
    val broadcast: Broadcast? = null,
    val telemetry: StreamTelemetry = StreamTelemetry(),
    val chatMessages: List<LiveChatMessage> = emptyList(),
    val isLowerThirdVisible: Boolean = true,
    val audioVuLevel: Float = 0.65f, // 0.0 to 1.0
    val showEndConfirmDialog: Boolean = false
)

class BroadcastControlViewModel(
    private val broadcastId: Long,
    private val getBroadcastsUseCase: GetBroadcastsUseCase,
    private val updateStatusUseCase: UpdateBroadcastStatusUseCase,
    private val broadcastRepository: BroadcastRepository,
    private val publisher: RtmpPublisher,
    private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(BroadcastControlUiState())
    val uiState: StateFlow<BroadcastControlUiState> = _uiState.asStateFlow()

    private var vuMeterJob: Job? = null

    init {
        observeBroadcast()
        observeTelemetry()
        startAudioVuSimulation()
    }

    private fun observeBroadcast() {
        viewModelScope.launch {
            getBroadcastsUseCase.getById(broadcastId).collect { bc ->
                _uiState.value = _uiState.value.copy(broadcast = bc)
            }
        }
    }

    private fun observeTelemetry() {
        viewModelScope.launch {
            publisher.telemetry.collect { tele ->
                _uiState.value = _uiState.value.copy(telemetry = tele)
                // Persist telemetry updates periodically
                if (tele.status == StreamStatus.LIVE && tele.durationSeconds % 5 == 0L) {
                    broadcastRepository.updateTelemetry(broadcastId, tele.durationSeconds, tele.currentViewers)
                }
            }
        }
    }

    fun startLiveStream() {
        val bc = _uiState.value.broadcast ?: return
        val videoConfig = VideoEncoderConfig(
            bitrateKbps = bc.bitrateKbps,
            frameRate = bc.fps
        )
        val audioConfig = AudioEncoderConfig()

        LiveStreamingService.startService(appContext, bc.title)
        val endpoints = parseEndpoints(bc.rtmpUrl, bc.streamKey, bc.platform)
        publisher.startPublishing(endpoints, videoConfig, audioConfig)

        viewModelScope.launch {
            updateStatusUseCase(broadcastId, BroadcastStatus.LIVE)
        }
    }

    private fun parseEndpoints(url: String, key: String, platform: PlatformType): List<RtmpEndpoint> {
        if (url.contains("|") && key.contains("|")) {
            val urls = url.split("|")
            val keys = key.split("|")
            return urls.zip(keys).mapIndexed { idx, (u, k) ->
                val name = when {
                    u.contains("facebook", ignoreCase = true) -> "Facebook"
                    u.contains("youtube", ignoreCase = true) -> "YouTube"
                    else -> "Destination ${idx + 1}"
                }
                RtmpEndpoint(name = name, rtmpUrl = u.trim(), streamKey = k.trim())
            }
        }
        return listOf(RtmpEndpoint(name = platform.name, rtmpUrl = url.trim(), streamKey = key.trim()))
    }

    fun stopLiveStream() {
        publisher.stopPublishing()
        LiveStreamingService.stopService(appContext)

        viewModelScope.launch {
            updateStatusUseCase(broadcastId, BroadcastStatus.ENDED)
        }
    }

    fun toggleMic(): Boolean = publisher.toggleMicMute()
    fun toggleTorch(): Boolean = publisher.toggleTorch()
    fun toggleCamera(): Boolean = publisher.toggleCameraFacing()

    fun feedVideoFrame(yuvBytes: ByteArray) {
        publisher.encodeVideoFrame(yuvBytes)
    }

    fun toggleLowerThird() {
        _uiState.value = _uiState.value.copy(isLowerThirdVisible = !_uiState.value.isLowerThirdVisible)
    }

    fun promptEndConfirmation(show: Boolean) {
        _uiState.value = _uiState.value.copy(showEndConfirmDialog = show)
    }

    private fun startAudioVuSimulation() {
        vuMeterJob?.cancel()
        vuMeterJob = viewModelScope.launch {
            while (true) {
                delay(120)
                if (!_uiState.value.telemetry.isMicMuted && _uiState.value.telemetry.status == StreamStatus.LIVE) {
                    val level = Random.nextFloat().coerceIn(0.2f, 0.95f)
                    _uiState.value = _uiState.value.copy(audioVuLevel = level)
                } else {
                    _uiState.value = _uiState.value.copy(audioVuLevel = 0.05f)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        vuMeterJob?.cancel()
    }
}
