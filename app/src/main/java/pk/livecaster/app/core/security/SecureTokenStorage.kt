package pk.livecaster.app.core.security

import android.content.Context
import android.content.SharedPreferences
import pk.livecaster.app.core.constants.AppConstants

class SecureTokenStorage(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        AppConstants.PREFS_NAME,
        Context.MODE_PRIVATE
    )

    fun saveAuthToken(token: String) {
        prefs.edit().putString(KEY_AUTH_TOKEN, token).apply()
    }

    fun getAuthToken(): String? {
        return prefs.getString(KEY_AUTH_TOKEN, null)
    }

    fun saveFacebookToken(token: String) {
        prefs.edit().putString(KEY_FACEBOOK_TOKEN, token).apply()
    }

    fun getFacebookToken(): String? {
        return prefs.getString(KEY_FACEBOOK_TOKEN, null)
    }

    fun saveYouTubeToken(token: String) {
        prefs.edit().putString(KEY_YOUTUBE_TOKEN, token).apply()
    }

    fun getYouTubeToken(): String? {
        return prefs.getString(KEY_YOUTUBE_TOKEN, null)
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_AUTH_TOKEN = "sec_auth_token"
        private const val KEY_FACEBOOK_TOKEN = "sec_fb_token"
        private const val KEY_YOUTUBE_TOKEN = "sec_yt_token"
    }
}
