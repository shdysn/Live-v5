package pk.livecaster.app.settings.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

data class StudioSettings(
    val resolution: String = "720p",
    val fps: Int = 30,
    val videoBitrateKbps: Int = 3500,
    val audioBitrateKbps: Int = 128,
    val isAdaptiveBitrateEnabled: Boolean = true,
    val isHardwareEncoderEnabled: Boolean = true,
    val keyframeIntervalSec: Int = 2,
    val language: String = "en", // "en" or "ur"
    val defaultRtmpServer: String = "rtmp://live.livecaster.pk/live"
)

class SettingsRepositoryImpl(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("livecaster_settings", Context.MODE_PRIVATE)

    private val _settingsFlow = MutableStateFlow(loadSettings())
    val settingsFlow: Flow<StudioSettings> = _settingsFlow

    fun getSettings(): StudioSettings = _settingsFlow.value

    fun updateSettings(newSettings: StudioSettings) {
        prefs.edit()
            .putString("resolution", newSettings.resolution)
            .putInt("fps", newSettings.fps)
            .putInt("videoBitrateKbps", newSettings.videoBitrateKbps)
            .putInt("audioBitrateKbps", newSettings.audioBitrateKbps)
            .putBoolean("adaptiveBitrate", newSettings.isAdaptiveBitrateEnabled)
            .putBoolean("hardwareEncoder", newSettings.isHardwareEncoderEnabled)
            .putInt("keyframeInterval", newSettings.keyframeIntervalSec)
            .putString("language", newSettings.language)
            .putString("defaultRtmp", newSettings.defaultRtmpServer)
            .apply()
        _settingsFlow.value = newSettings
    }

    private fun loadSettings(): StudioSettings {
        return StudioSettings(
            resolution = prefs.getString("resolution", "720p") ?: "720p",
            fps = prefs.getInt("fps", 30),
            videoBitrateKbps = prefs.getInt("videoBitrateKbps", 3500),
            audioBitrateKbps = prefs.getInt("audioBitrateKbps", 128),
            isAdaptiveBitrateEnabled = prefs.getBoolean("adaptiveBitrate", true),
            isHardwareEncoderEnabled = prefs.getBoolean("hardwareEncoder", true),
            keyframeIntervalSec = prefs.getInt("keyframeInterval", 2),
            language = prefs.getString("language", "en") ?: "en",
            defaultRtmpServer = prefs.getString("defaultRtmp", "rtmp://live.livecaster.pk/live") ?: "rtmp://live.livecaster.pk/live"
        )
    }
}
