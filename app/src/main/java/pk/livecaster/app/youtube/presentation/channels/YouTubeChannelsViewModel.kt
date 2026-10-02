package pk.livecaster.app.youtube.presentation.channels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.youtube.domain.model.YouTubeChannel
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository
import pk.livecaster.app.youtube.domain.usecase.GetYouTubeChannelsUseCase

data class YouTubeChannelsUiState(
    val channels: List<YouTubeChannel> = emptyList(),
    val isLoading: Boolean = false,
    val selectedChannelId: String? = null,
    val message: String? = null
)

class YouTubeChannelsViewModel(
    private val getChannelsUseCase: GetYouTubeChannelsUseCase,
    private val repository: YouTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(YouTubeChannelsUiState())
    val uiState: StateFlow<YouTubeChannelsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getChannelsUseCase().collect { channels ->
                _uiState.value = _uiState.value.copy(
                    channels = channels,
                    selectedChannelId = _uiState.value.selectedChannelId ?: channels.firstOrNull()?.id
                )
            }
        }
    }

    fun selectChannel(channelId: String) {
        _uiState.value = _uiState.value.copy(selectedChannelId = channelId)
    }

    fun linkNewChannel(title: String, channelId: String, handle: String?) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = repository.linkChannel(title, channelId, handle)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "YouTube Channel '${res.data.title}' connected!"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, message = res.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun unlinkChannel(channelId: String) {
        viewModelScope.launch {
            repository.unlinkChannel(channelId)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
