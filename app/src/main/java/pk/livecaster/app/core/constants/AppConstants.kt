package pk.livecaster.app.core.constants

object AppConstants {
    const val APP_NAME = "LiveCaster"
    const val PREFS_NAME = "livecaster_prefs"
    const val DATABASE_NAME = "livecaster_studio.db"
    
    // Auth & API
    const val BASE_API_URL = "https://api.livecaster.pk/v1/"
    const val FACEBOOK_GRAPH_API = "https://graph.facebook.com/v19.0/"
    const val YOUTUBE_API_V3 = "https://www.googleapis.com/youtube/v3/"

    // Service Notification
    const val NOTIFICATION_CHANNEL_ID = "livecaster_streaming_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Live Broadcast Service"
    const val NOTIFICATION_ID = 1001

    // Defaults
    const val DEFAULT_RTMP_INGEST = "rtmp://live.livecaster.pk/live"
}

object StreamConstants {
    const val DEFAULT_VIDEO_WIDTH = 1280
    const val DEFAULT_VIDEO_HEIGHT = 720
    const val DEFAULT_FPS = 30
    const val DEFAULT_BITRATE_KBPS = 3500
    const val DEFAULT_AUDIO_BITRATE_KBPS = 128
    const val DEFAULT_KEYFRAME_INTERVAL_SEC = 2

    const val MAX_RECONNECT_ATTEMPTS = 5
    const val RECONNECT_DELAY_MS = 3000L
    const val STATS_UPDATE_INTERVAL_MS = 1000L
}
