package pk.livecaster.app.broadcast.presentation.setup

import android.content.Context
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
import pk.livecaster.app.facebook.domain.model.FacebookPage
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

enum class FacebookDestination {
    PROFILE,
    PAGE
}

sealed class ConnectionTestResult {
    data class Success(val host: String, val port: Int, val latencyMs: Long) : ConnectionTestResult()
    data class Error(val message: String) : ConnectionTestResult()
}

data class BroadcastSetupUiState(
    val title: String = "",
    val description: String = "",
    val platform: PlatformType = PlatformType.MULTI_DESTINATION,
    val facebookDestination: FacebookDestination = FacebookDestination.PROFILE,
    val facebookToken: String = "",
    val isFacebookTokenSaved: Boolean = false,
    val availableFacebookPages: List<FacebookPage> = emptyList(),
    val selectedFacebookPageId: String = "",
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
    val createdBroadcastId: Long? = null,
    val showAutoStartGuideDialog: Boolean = false,
    val isPersistentKeySaved: Boolean = false
)

class BroadcastSetupViewModel(
    private val context: Context,
    private val createBroadcastUseCase: CreateBroadcastUseCase,
    private val facebookRepository: FacebookRepository,
    private val youtubeRepository: YouTubeRepository
) : ViewModel() {

    private val prefs = context.getSharedPreferences("livecaster_stream_prefs", Context.MODE_PRIVATE)

    private val _uiState = MutableStateFlow(
        BroadcastSetupUiState(
            streamKey = prefs.getString("fb_persistent_stream_key", "") ?: "",
            youtubeStreamKey = prefs.getString("yt_persistent_stream_key", "") ?: "",
            isPersistentKeySaved = !prefs.getString("fb_persistent_stream_key", "").isNullOrBlank()
        )
    )
    val uiState: StateFlow<BroadcastSetupUiState> = _uiState.asStateFlow()

    init {
        val savedFbToken = facebookRepository.getSavedToken() ?: ""
        _uiState.value = _uiState.value.copy(
            facebookToken = savedFbToken,
            isFacebookTokenSaved = savedFbToken.isNotBlank()
        )

        viewModelScope.launch {
            facebookRepository.getPages().collect { pages ->
                _uiState.value = _uiState.value.copy(
                    availableFacebookPages = pages,
                    selectedFacebookPageId = if (_uiState.value.selectedFacebookPageId.isBlank()) {
                        pages.firstOrNull()?.id ?: ""
                    } else {
                        _uiState.value.selectedFacebookPageId
                    }
                )
            }
        }

        viewModelScope.launch {
            if (savedFbToken.isNotBlank()) {
                facebookRepository.refreshPages()
            }
        }
    }

    fun updateFacebookDestination(dest: FacebookDestination) {
        _uiState.value = _uiState.value.copy(facebookDestination = dest)
    }

    fun updateFacebookToken(token: String) {
        val cleanToken = token.trim()
        _uiState.value = _uiState.value.copy(
            facebookToken = cleanToken,
            isFacebookTokenSaved = cleanToken.isNotBlank()
        )
        facebookRepository.saveToken(cleanToken)
        if (cleanToken.isNotBlank()) {
            viewModelScope.launch {
                facebookRepository.refreshPages()
            }
        }
    }

    fun selectFacebookPage(pageId: String) {
        _uiState.value = _uiState.value.copy(selectedFacebookPageId = pageId)
    }

    fun toggleAutoStartGuide(show: Boolean) {
        _uiState.value = _uiState.value.copy(showAutoStartGuideDialog = show)
    }

    fun updateTitle(title: String) {
        _uiState.value = _uiState.value.copy(title = title, errorMessage = null)
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
        _uiState.value = _uiState.value.copy(
            streamKey = key,
            isPersistentKeySaved = key.trim().isNotBlank()
        )
        prefs.edit().putString("fb_persistent_stream_key", key.trim()).apply()
    }

    fun updateYoutubeStreamKey(key: String) {
        _uiState.value = _uiState.value.copy(youtubeStreamKey = key)
        prefs.edit().putString("yt_persistent_stream_key", key.trim()).apply()
    }

    fun updateQuality(resolution: String, bitrate: Int, fps: Int) {
        _uiState.value = _uiState.value.copy(
            resolution = resolution,
            bitrateKbps = bitrate,
            fps = fps
        )
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

    fun createAndStartBroadcast(onSuccess: (broadcastId: Long) -> Unit) {
        val current = _uiState.value

        val streamTitle = current.title.trim().ifBlank {
            when (current.platform) {
                PlatformType.MULTI_DESTINATION -> "Simulcast (Facebook + YouTube Live)"
                PlatformType.FACEBOOK -> if (current.facebookDestination == FacebookDestination.PROFILE) "Facebook Profile Live" else "Facebook Page Live"
                PlatformType.YOUTUBE -> "YouTube Live Stream"
                PlatformType.CUSTOM_RTMP -> "Live Stream (RTMP)"
            }
        }

        viewModelScope.launch {
            _uiState.value = current.copy(isLoading = true, errorMessage = null)

            var fbStreamUrl = "rtmps://live-api-s.facebook.com:443/rtmp/"
            var fbStreamKey = current.streamKey.trim()

            // Native Facebook Graph API initialization (Zero Browser!)
            val isFbPlatform = current.platform == PlatformType.FACEBOOK || current.platform == PlatformType.MULTI_DESTINATION
            if (isFbPlatform && current.isFacebookTokenSaved) {
                val liveResult = if (current.facebookDestination == FacebookDestination.PROFILE) {
                    facebookRepository.createProfileLiveStream(
                        title = streamTitle,
                        description = current.description.trim()
                    )
                } else {
                    facebookRepository.createLiveStream(
                        pageId = current.selectedFacebookPageId,
                        title = streamTitle,
                        description = current.description.trim()
                    )
                }

                when (liveResult) {
                    is Resource.Success -> {
                        fbStreamUrl = liveResult.data.streamUrl
                        fbStreamKey = liveResult.data.streamKey
                    }
                    is Resource.Error -> {
                        if (fbStreamKey.isBlank()) {
                            _uiState.value = current.copy(isLoading = false, errorMessage = liveResult.message)
                            return@launch
                        }
                    }
                    is Resource.Loading -> Unit
                }
            }

            val finalUrl: String
            val finalKey: String

            if (current.platform == PlatformType.MULTI_DESTINATION) {
                val ytKey = current.youtubeStreamKey.trim()
                if (fbStreamKey.isBlank() && ytKey.isBlank()) {
                    _uiState.value = current.copy(
                        isLoading = false,
                        errorMessage = "Please enter Facebook Access Token or Stream Key to stream"
                    )
                    return@launch
                }
                finalUrl = "$fbStreamUrl|rtmp://a.rtmp.youtube.com/live2"
                finalKey = "$fbStreamKey|$ytKey"
            } else if (current.platform == PlatformType.FACEBOOK) {
                if (fbStreamKey.isBlank()) {
                    _uiState.value = current.copy(
                        isLoading = false,
                        errorMessage = "Please enter your Facebook Access Token or Stream Key"
                    )
                    return@launch
                }
                finalUrl = fbStreamUrl
                finalKey = fbStreamKey
            } else {
                finalUrl = current.rtmpUrl.trim()
                finalKey = current.streamKey.trim()
                if (finalUrl.isBlank() || finalKey.isBlank()) {
                    _uiState.value = current.copy(
                        isLoading = false,
                        errorMessage = "Please enter RTMP server endpoint and Stream Key"
                    )
                    return@launch
                }
            }

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
