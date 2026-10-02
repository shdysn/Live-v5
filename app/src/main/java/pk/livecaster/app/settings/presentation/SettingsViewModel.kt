package pk.livecaster.app.settings.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.settings.data.SettingsRepositoryImpl
import pk.livecaster.app.settings.data.StudioSettings

class SettingsViewModel(
    private val repository: SettingsRepositoryImpl
) : ViewModel() {

    private val _settings = MutableStateFlow(repository.getSettings())
    val settings: StateFlow<StudioSettings> = _settings.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage: StateFlow<String?> = _saveMessage.asStateFlow()

    init {
        viewModelScope.launch {
            repository.settingsFlow.collect {
                _settings.value = it
            }
        }
    }

    fun updateResolution(res: String) {
        _settings.value = _settings.value.copy(resolution = res)
    }

    fun updateFps(fps: Int) {
        _settings.value = _settings.value.copy(fps = fps)
    }

    fun updateVideoBitrate(bitrate: Int) {
        _settings.value = _settings.value.copy(videoBitrateKbps = bitrate)
    }

    fun updateAudioBitrate(bitrate: Int) {
        _settings.value = _settings.value.copy(audioBitrateKbps = bitrate)
    }

    fun toggleAdaptiveBitrate(enabled: Boolean) {
        _settings.value = _settings.value.copy(isAdaptiveBitrateEnabled = enabled)
    }

    fun toggleHardwareEncoder(enabled: Boolean) {
        _settings.value = _settings.value.copy(isHardwareEncoderEnabled = enabled)
    }

    fun updateLanguage(lang: String) {
        _settings.value = _settings.value.copy(language = lang)
    }

    fun updateRtmpServer(url: String) {
        _settings.value = _settings.value.copy(defaultRtmpServer = url)
    }

    fun saveSettings() {
        repository.updateSettings(_settings.value)
        _saveMessage.value = "Studio configuration saved successfully!"
    }

    fun clearMessage() {
        _saveMessage.value = null
    }
}
