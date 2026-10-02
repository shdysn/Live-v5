package pk.livecaster.app.accounts.presentation

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.core.auth.OAuthChromeManager
import pk.livecaster.app.core.auth.OAuthEvent
import pk.livecaster.app.core.security.SecureTokenStorage
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

data class ConnectAccountsUiState(
    // Facebook section state
    val isFacebookLoggedIn: Boolean = false,
    val selectedFacebookPage: String = "Official Live Stream PK",
    val customPageName: String = "",
    val customPageStreamKey: String = "",
    val availableFacebookPages: List<String> = listOf(
        "Official Live Stream PK",
        "Awais Studio Broadcast",
        "Daily News & Talk PK",
        "+ Enter Custom Page Name..."
    ),
    val isFacebookConnected: Boolean = false,
    val facebookConnectedName: String = "",
    val customFacebookAppId: String = "",
    val showFacebookDevOAuth: Boolean = false,
    val facebookPermissions: List<String> = listOf(
        "View managed Pages",
        "Create live broadcasts",
        "Read Page engagement"
    ),

    // YouTube section state
    val isGoogleLoggedIn: Boolean = false,
    val selectedYouTubeChannel: String = "Official YouTube Studio",
    val customChannelName: String = "",
    val customChannelStreamKey: String = "",
    val availableYouTubeChannels: List<String> = listOf(
        "Official YouTube Studio",
        "Awais Live PK",
        "Live Broadcast Pakistan",
        "+ Enter Custom Channel Name..."
    ),
    val isYouTubeConnected: Boolean = false,
    val youtubeConnectedName: String = "",
    val customGoogleClientId: String = "",
    val showYouTubeDevOAuth: Boolean = false,
    val youTubePermissions: List<String> = listOf(
        "View YouTube Channel",
        "Create and manage live broadcasts",
        "View Live status"
    ),

    val isLoading: Boolean = false,
    val message: String? = null
)

class ConnectAccountsViewModel(
    private val facebookRepository: FacebookRepository,
    private val youtubeRepository: YouTubeRepository,
    private val tokenStorage: SecureTokenStorage,
    private val oAuthChromeManager: OAuthChromeManager? = null
) : ViewModel() {

    private val defaultFbPages = listOf(
        "Official Live Stream PK",
        "Awais Studio Broadcast",
        "Daily News & Talk PK",
        "+ Enter Custom Page Name..."
    )

    private val defaultYtChannels = listOf(
        "Official YouTube Studio",
        "Awais Live PK",
        "Live Broadcast Pakistan",
        "+ Enter Custom Channel Name..."
    )

    private val _uiState = MutableStateFlow(ConnectAccountsUiState())
    val uiState: StateFlow<ConnectAccountsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            facebookRepository.getPages().collect { pages ->
                val connected = pages.firstOrNull()
                val pageList = if (pages.isEmpty()) defaultFbPages else (pages.map { it.name } + "+ Enter Custom Page Name...").distinct()
                _uiState.value = _uiState.value.copy(
                    availableFacebookPages = pageList,
                    selectedFacebookPage = if (_uiState.value.selectedFacebookPage.isBlank()) (pageList.firstOrNull() ?: "") else _uiState.value.selectedFacebookPage,
                    isFacebookConnected = connected != null,
                    facebookConnectedName = connected?.name ?: ""
                )
            }
        }

        viewModelScope.launch {
            youtubeRepository.getChannels().collect { channels ->
                val connected = channels.firstOrNull()
                val channelList = if (channels.isEmpty()) defaultYtChannels else (channels.map { it.title } + "+ Enter Custom Channel Name...").distinct()
                _uiState.value = _uiState.value.copy(
                    availableYouTubeChannels = channelList,
                    selectedYouTubeChannel = if (_uiState.value.selectedYouTubeChannel.isBlank()) (channelList.firstOrNull() ?: "") else _uiState.value.selectedYouTubeChannel,
                    isYouTubeConnected = connected != null,
                    youtubeConnectedName = connected?.title ?: ""
                )
            }
        }

        // Listen for Chrome OAuth deep link events
        oAuthChromeManager?.let { manager ->
            viewModelScope.launch {
                manager.authEvents.collect { event ->
                    when (event) {
                        is OAuthEvent.FacebookSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isFacebookConnected = true,
                                facebookConnectedName = event.pageName,
                                isFacebookLoggedIn = false,
                                message = "Facebook account linked successfully via Chrome!"
                            )
                        }
                        is OAuthEvent.GoogleSuccess -> {
                            _uiState.value = _uiState.value.copy(
                                isYouTubeConnected = true,
                                youtubeConnectedName = event.channelTitle,
                                isGoogleLoggedIn = false,
                                message = "Google/YouTube account linked successfully via Chrome!"
                            )
                        }
                        is OAuthEvent.Error -> {
                            _uiState.value = _uiState.value.copy(
                                message = "${event.platform} login error: ${event.message}"
                            )
                        }
                    }
                }
            }
        }
    }

    fun openFacebookInChrome(context: Context) {
        val appId = _uiState.value.customFacebookAppId.trim().ifBlank { null }
        oAuthChromeManager?.launchFacebookInChrome(context, appId)
    }

    fun openGoogleInChrome(context: Context) {
        val clientId = _uiState.value.customGoogleClientId.trim().ifBlank { null }
        oAuthChromeManager?.launchGoogleInChrome(context, clientId)
    }

    fun openFacebookLiveProducer(context: Context) {
        oAuthChromeManager?.openFacebookLiveProducer(context)
    }

    fun openYouTubeLiveStudio(context: Context) {
        oAuthChromeManager?.openYouTubeLiveStudio(context)
    }

    fun updateFacebookAppId(id: String) {
        _uiState.value = _uiState.value.copy(customFacebookAppId = id)
    }

    fun updateGoogleClientId(id: String) {
        _uiState.value = _uiState.value.copy(customGoogleClientId = id)
    }

    fun updateCustomPageName(name: String) {
        _uiState.value = _uiState.value.copy(customPageName = name)
    }

    fun updateCustomPageStreamKey(key: String) {
        _uiState.value = _uiState.value.copy(customPageStreamKey = key)
    }

    fun updateCustomChannelName(name: String) {
        _uiState.value = _uiState.value.copy(customChannelName = name)
    }

    fun updateCustomChannelStreamKey(key: String) {
        _uiState.value = _uiState.value.copy(customChannelStreamKey = key)
    }

    fun toggleFacebookDevOAuth() {
        _uiState.value = _uiState.value.copy(showFacebookDevOAuth = !_uiState.value.showFacebookDevOAuth)
    }

    fun toggleYouTubeDevOAuth() {
        _uiState.value = _uiState.value.copy(showYouTubeDevOAuth = !_uiState.value.showYouTubeDevOAuth)
    }

    fun continueWithFacebook() {
        _uiState.value = _uiState.value.copy(
            isFacebookLoggedIn = true,
            selectedFacebookPage = _uiState.value.selectedFacebookPage.ifBlank { "Official Live Stream PK" },
            message = "Facebook account authenticated. Select or enter Page to link."
        )
    }

    fun selectFacebookPage(page: String) {
        _uiState.value = _uiState.value.copy(selectedFacebookPage = page)
    }

    fun cancelFacebook() {
        _uiState.value = _uiState.value.copy(
            isFacebookLoggedIn = false
        )
    }

    fun connectFacebookPage() {
        viewModelScope.launch {
            val isCustom = _uiState.value.selectedFacebookPage.startsWith("+")
            val rawName = if (isCustom && _uiState.value.customPageName.isNotBlank()) {
                _uiState.value.customPageName.trim()
            } else if (!isCustom) {
                _uiState.value.selectedFacebookPage
            } else {
                "Official Live Stream PK"
            }
            val pageName = rawName.ifBlank { "Official Live Stream PK" }
            val pageToken = _uiState.value.customPageStreamKey.ifBlank { "fb_live_${System.currentTimeMillis()}" }

            facebookRepository.linkPage(
                pageName = pageName,
                pageId = "fb_page_${pageName.replace(" ", "_").lowercase()}",
                pageToken = pageToken
            )
            tokenStorage.saveFacebookToken(pageToken)
            _uiState.value = _uiState.value.copy(
                isFacebookConnected = true,
                facebookConnectedName = pageName,
                isFacebookLoggedIn = false,
                message = "Facebook: Connected to $pageName"
            )
        }
    }

    fun disconnectFacebook() {
        viewModelScope.launch {
            val pageName = _uiState.value.facebookConnectedName
            facebookRepository.unlinkPage(pageName)
            tokenStorage.saveFacebookToken("")
            _uiState.value = _uiState.value.copy(
                isFacebookConnected = false,
                facebookConnectedName = "",
                isFacebookLoggedIn = false,
                message = "Facebook Page disconnected"
            )
        }
    }

    fun continueWithGoogle() {
        _uiState.value = _uiState.value.copy(
            isGoogleLoggedIn = true,
            selectedYouTubeChannel = _uiState.value.selectedYouTubeChannel.ifBlank { "Official YouTube Studio" },
            message = "Google account authenticated. Select or enter Channel to link."
        )
    }

    fun selectYouTubeChannel(channel: String) {
        _uiState.value = _uiState.value.copy(selectedYouTubeChannel = channel)
    }

    fun cancelGoogle() {
        _uiState.value = _uiState.value.copy(
            isGoogleLoggedIn = false
        )
    }

    fun connectYouTubeChannel() {
        viewModelScope.launch {
            val isCustom = _uiState.value.selectedYouTubeChannel.startsWith("+")
            val rawName = if (isCustom && _uiState.value.customChannelName.isNotBlank()) {
                _uiState.value.customChannelName.trim()
            } else if (!isCustom) {
                _uiState.value.selectedYouTubeChannel
            } else {
                "Official YouTube Studio"
            }
            val channelName = rawName.ifBlank { "Official YouTube Studio" }
            val channelHandle = "@${channelName.replace(" ", "")}"

            youtubeRepository.linkChannel(
                title = channelName,
                channelId = "UC_${channelName.replace(" ", "_").lowercase()}",
                customUrl = channelHandle
            )
            val token = _uiState.value.customChannelStreamKey.ifBlank { "yt_live_${System.currentTimeMillis()}" }
            tokenStorage.saveYouTubeToken(token)
            _uiState.value = _uiState.value.copy(
                isYouTubeConnected = true,
                youtubeConnectedName = channelName,
                isGoogleLoggedIn = false,
                message = "YouTube: Connected to $channelName"
            )
        }
    }

    fun disconnectYouTube() {
        viewModelScope.launch {
            val channelName = _uiState.value.youtubeConnectedName
            youtubeRepository.unlinkChannel(channelName)
            tokenStorage.saveYouTubeToken("")
            _uiState.value = _uiState.value.copy(
                isYouTubeConnected = false,
                youtubeConnectedName = "",
                isGoogleLoggedIn = false,
                message = "YouTube Channel disconnected"
            )
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
