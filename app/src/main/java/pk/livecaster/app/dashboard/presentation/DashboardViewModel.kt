package pk.livecaster.app.dashboard.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.broadcast.domain.model.Broadcast
import pk.livecaster.app.broadcast.domain.model.BroadcastStatus
import pk.livecaster.app.broadcast.domain.model.PlatformType
import pk.livecaster.app.broadcast.domain.repository.BroadcastRepository
import pk.livecaster.app.broadcast.domain.usecase.GetBroadcastsUseCase
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

data class DashboardStats(
    val totalStreams: Int = 0,
    val totalAirtimeSeconds: Long = 0,
    val peakAudience: Long = 0,
    val activeDestinationsCount: Int = 3
)

data class DashboardUiState(
    val broadcasts: List<Broadcast> = emptyList(),
    val activeBroadcast: Broadcast? = null,
    val stats: DashboardStats = DashboardStats(),
    val fbPagesCount: Int = 0,
    val ytChannelsCount: Int = 0,
    val isLoading: Boolean = false
)

class DashboardViewModel(
    private val getBroadcastsUseCase: GetBroadcastsUseCase,
    private val broadcastRepository: BroadcastRepository,
    private val facebookRepository: FacebookRepository,
    private val youtubeRepository: YouTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            getBroadcastsUseCase().collect { list ->
                val active = list.find { it.status == BroadcastStatus.LIVE }
                val totalAirtime = list.sumOf { it.durationSeconds }
                val peak = list.maxOfOrNull { it.peakViewers } ?: 0L

                _uiState.value = _uiState.value.copy(
                    broadcasts = list,
                    activeBroadcast = active,
                    stats = DashboardStats(
                        totalStreams = list.size,
                        totalAirtimeSeconds = totalAirtime,
                        peakAudience = peak,
                        activeDestinationsCount = if (list.isNotEmpty()) 2 else 0
                    )
                )
            }
        }

        viewModelScope.launch {
            facebookRepository.getPages().collect { pages ->
                _uiState.value = _uiState.value.copy(fbPagesCount = pages.size)
            }
        }

        viewModelScope.launch {
            youtubeRepository.getChannels().collect { channels ->
                _uiState.value = _uiState.value.copy(ytChannelsCount = channels.size)
            }
        }
    }

    fun deleteBroadcast(id: Long) {
        viewModelScope.launch {
            broadcastRepository.deleteBroadcast(id)
        }
    }
}
