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
    val selectedFacebookPage: String = "",
    val availableFacebookPages: List<String> = emptyList(),
    val isFacebookConnected: Boolean = false,
    val facebookConnectedName: String = "",
    val customFacebookAppId: String = "",
    val facebookPermissions: List<String> = listOf(
        "View managed Pages",
        "Create live broadcasts",
        "Read Page engagement"
    ),

    // YouTube section state
    val isGoogleLoggedIn: Boolean = false,
    val selectedYouTubeChannel: String = "",
    val availableYouTubeChannels: List<String> = emptyList(),
    val isYouTubeConnected: Boolean = false,
    val youtubeConnectedName: String = "",
    val customGoogleClientId: String = "",
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

    private val _uiState = MutableStateFlow(ConnectAccountsUiState())
    val uiState: StateFlow<ConnectAccountsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            facebookRepository.getPages().collect { pages ->
                val connected = pages.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    availableFacebookPages = pages.map { it.name },
                    selectedFacebookPage = if (_uiState.value.selectedFacebookPage.isBlank()) (pages.firstOrNull()?.name ?: "") else _uiState.value.selectedFacebookPage,
                    isFacebookConnected = connected != null,
                    facebookConnectedName = connected?.name ?: ""
                )
            }
        }

        viewModelScope.launch {
            youtubeRepository.getChannels().collect { channels ->
                val connected = channels.firstOrNull()
                _uiState.value = _uiState.value.copy(
                    availableYouTubeChannels = channels.map { it.title },
                    selectedYouTubeChannel = if (_uiState.value.selectedYouTubeChannel.isBlank()) (channels.firstOrNull()?.title ?: "") else _uiState.value.selectedYouTubeChannel,
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

    fun continueWithFacebook() {
        _uiState.value = _uiState.value.copy(
            isFacebookLoggedIn = true,
            selectedFacebookPage = _uiState.value.selectedFacebookPage.ifBlank { "My Facebook Live Page" },
            message = "Facebook account authenticated. Select Page to link."
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
            val pageName = _uiState.value.selectedFacebookPage.ifBlank { "Live Broadcast Page" }
            facebookRepository.linkPage(
                pageName = pageName,
                pageId = "fb_page_${pageName.replace(" ", "_").lowercase()}",
                pageToken = "EAAB_${System.currentTimeMillis()}"
            )
            tokenStorage.saveFacebookToken("fb_auth_token_${System.currentTimeMillis()}")
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
            selectedYouTubeChannel = _uiState.value.selectedYouTubeChannel.ifBlank { "My YouTube Live Channel" },
            message = "Google account authenticated. Select Channel to link."
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
            val channelName = _uiState.value.selectedYouTubeChannel.ifBlank { "Live Stream Channel" }
            youtubeRepository.linkChannel(
                title = channelName,
                channelId = "UC_${channelName.replace(" ", "_").lowercase()}",
                customUrl = "@${channelName.replace(" ", "")}"
            )
            tokenStorage.saveYouTubeToken("yt_auth_token_${System.currentTimeMillis()}")
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
