package pk.livecaster.app.broadcast.presentation.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.model.PlatformType
import pk.livecaster.app.broadcast.domain.usecase.CreateBroadcastUseCase
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.core.constants.StreamConstants
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

sealed class ConnectionTestResult {
    data class Success(val host: String, val port: Int, val latencyMs: Long) : ConnectionTestResult()
    data class Error(val message: String) : ConnectionTestResult()
}

data class BroadcastSetupUiState(
    val title: String = "",
    val description: String = "",
    val platform: PlatformType = PlatformType.MULTI_DESTINATION,
    val rtmpUrl: String = "rtmps://live-api-s.facebook.com:443/rtmp/|rtmp://a.rtmp.youtube.com/live2",
    val streamKey: String = "",
    val youtubeStreamKey: String = "",
    val resolution: String = "720p",
    val bitrateKbps: Int = StreamConstants.DEFAULT_BITRATE_KBPS,
    val fps: Int = StreamConstants.DEFAULT_FPS,
    val isLoading: Boolean = false,
    val isTestingConnection: Boolean = false,
    val testConnectionResult: ConnectionTestResult? = null,
    val errorMessage: String? = null,
    val createdBroadcastId: Long? = null
)

class BroadcastSetupViewModel(
    private val createBroadcastUseCase: CreateBroadcastUseCase,
    private val facebookRepository: FacebookRepository,
    private val youtubeRepository: YouTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BroadcastSetupUiState())
    val uiState: StateFlow<BroadcastSetupUiState> = _uiState.asStateFlow()

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
    }

    fun testConnection() {
        val current = _uiState.value
        val url = current.rtmpUrl.trim()
        if (url.isBlank()) {
            _uiState.value = current.copy(
                testConnectionResult = ConnectionTestResult.Error("Please enter an RTMP URL first")
            )
            return
        }

        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            _uiState.value = _uiState.value.copy(isTestingConnection = true, testConnectionResult = null)
            val startTime = System.currentTimeMillis()
            try {
                val host = extractHost(url)
                val port = extractPort(url)
                java.net.Socket().use { socket ->
                    socket.connect(java.net.InetSocketAddress(host, port), 4000)
                }
                val latency = System.currentTimeMillis() - startTime
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    testConnectionResult = ConnectionTestResult.Success(host, port, latency)
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isTestingConnection = false,
                    testConnectionResult = ConnectionTestResult.Error(
                        e.message ?: "Could not reach RTMP server"
                    )
                )
            }
        }
    }

    fun clearConnectionTestResult() {
        _uiState.value = _uiState.value.copy(testConnectionResult = null)
    }

    private fun extractHost(url: String): String {
        return try {
            val clean = url.substringAfter("://").substringBefore("/")
            clean.substringBefore(":")
        } catch (_: Exception) {
            url
        }
    }

    private fun extractPort(url: String): Int {
        val isRtmps = url.startsWith("rtmps://", ignoreCase = true)
        return try {
            val clean = url.substringAfter("://").substringBefore("/")
            if (clean.contains(":")) {
                clean.substringAfter(":").toIntOrNull() ?: if (isRtmps) 443 else 1935
            } else {
                if (isRtmps) 443 else 1935
            }
        } catch (_: Exception) {
            if (isRtmps) 443 else 1935
        }
    }

    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun updatePlatform(platform: PlatformType) {
        val defaultUrl = when (platform) {
            PlatformType.FACEBOOK -> "rtmps://live-api-s.facebook.com:443/rtmp/"
            PlatformType.YOUTUBE -> "rtmp://a.rtmp.youtube.com/live2"
            PlatformType.CUSTOM_RTMP -> ""
            PlatformType.MULTI_DESTINATION -> "rtmps://live-api-s.facebook.com:443/rtmp/|rtmp://a.rtmp.youtube.com/live2"
        }
        _uiState.value = _uiState.value.copy(
            platform = platform,
            rtmpUrl = defaultUrl,
            errorMessage = null
        )
    }

    fun updateRtmpUrl(url: String) {
        _uiState.value = _uiState.value.copy(rtmpUrl = url)
    }

    fun updateStreamKey(key: String) {
        _uiState.value = _uiState.value.copy(streamKey = key)
    }

    fun updateYoutubeStreamKey(key: String) {
        _uiState.value = _uiState.value.copy(youtubeStreamKey = key)
    }

    fun updateQuality(resolution: String, bitrate: Int, fps: Int) {
        _uiState.value = _uiState.value.copy(
            resolution = resolution,
            bitrateKbps = bitrate,
            fps = fps
        )
    }

    fun createAndStartBroadcast(onSuccess: (broadcastId: Long) -> Unit) {
        val current = _uiState.value

        val finalUrl: String
        val finalKey: String

        if (current.platform == PlatformType.MULTI_DESTINATION) {
            val fbKey = current.streamKey.trim()
            val ytKey = current.youtubeStreamKey.trim()
            if (fbKey.isBlank() && ytKey.isBlank()) {
                _uiState.value = current.copy(errorMessage = "Please enter at least one Stream Key (Facebook or YouTube)")
                return
            }
            finalUrl = "rtmps://live-api-s.facebook.com:443/rtmp/|rtmp://a.rtmp.youtube.com/live2"
            finalKey = "$fbKey|$ytKey"
        } else {
            finalUrl = current.rtmpUrl.trim()
            finalKey = current.streamKey.trim()
            if (finalUrl.isBlank()) {
                _uiState.value = current.copy(errorMessage = "Please enter RTMP server endpoint")
                return
            }
            if (finalKey.isBlank()) {
                _uiState.value = current.copy(errorMessage = "Please enter your Live Stream Key")
                return
            }
        }

        val streamTitle = current.title.trim().ifBlank {
            if (current.platform == PlatformType.MULTI_DESTINATION) {
                "Simulcast (Facebook + YouTube Live)"
            } else {
                "Live Stream (${current.platform.name})"
            }
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)

            val broadcast = Broadcast(
                title = streamTitle,
                description = current.description.trim(),
                rtmpUrl = finalUrl,
                streamKey = finalKey,
                platform = current.platform,
                status = BroadcastStatus.DRAFT,
                resolution = current.resolution,
                bitrateKbps = current.bitrateKbps,
                fps = current.fps
            )

            when (val result = createBroadcastUseCase(broadcast)) {
                is Resource.Success -> {
                    _uiState.value = current.copy(isLoading = false, createdBroadcastId = result.data)
                    onSuccess(result.data)
                }
                is Resource.Error -> {
                    _uiState.value = current.copy(isLoading = false, errorMessage = result.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }
}
