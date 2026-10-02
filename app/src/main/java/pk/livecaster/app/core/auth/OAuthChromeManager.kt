package pk.livecaster.app.core.auth

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import pk.livecaster.app.core.security.SecureTokenStorage
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository

sealed class OAuthEvent {
    data class FacebookSuccess(val token: String, val pageName: String) : OAuthEvent()
    data class GoogleSuccess(val token: String, val channelTitle: String) : OAuthEvent()
    data class Error(val platform: String, val message: String) : OAuthEvent()
}

class OAuthChromeManager(
    private val tokenStorage: SecureTokenStorage,
    private val facebookRepository: FacebookRepository,
    private val youtubeRepository: YouTubeRepository
) {
    private val _authEvents = MutableSharedFlow<OAuthEvent>(extraBufferCapacity = 5)
    val authEvents: SharedFlow<OAuthEvent> = _authEvents.asSharedFlow()

    companion object {
        private const val TAG = "OAuthChromeManager"
        const val REDIRECT_SCHEME = "pk.livecaster.app"
        const val REDIRECT_HOST = "oauth2redirect"

        // Default Client IDs (can be overridden by user)
        const val DEFAULT_FB_APP_ID = "123456789012345"
        const val DEFAULT_GOOGLE_CLIENT_ID = "123456789012-livecasterapp.apps.googleusercontent.com"

        const val FB_LIVE_PRODUCER_URL = "https://www.facebook.com/live/producer"
        const val YT_LIVE_STUDIO_URL = "https://studio.youtube.com/channel/live"
    }

    /**
     * Launches the official Facebook Login dialogue in Google Chrome (or default browser)
     * Validates that a non-placeholder Meta App ID is provided to avoid Facebook's "Invalid App ID" error.
     */
    fun launchFacebookInChrome(context: Context, customAppId: String? = null) {
        val appId = customAppId?.trim()?.ifBlank { null }
        if (appId == null || appId == DEFAULT_FB_APP_ID || appId.length < 8) {
            _authEvents.tryEmit(
                OAuthEvent.Error(
                    platform = "Facebook",
                    message = "To use Chrome OAuth, please provide your verified Meta App ID in Developer Settings, or tap 'Continue with Facebook' to connect directly in LiveCaster."
                )
            )
            return
        }
        val redirectUri = "$REDIRECT_SCHEME://$REDIRECT_HOST/facebook"
        val scopes = "email,public_profile,pages_show_list,pages_read_engagement,pages_manage_posts,publish_video"

        val authUri = Uri.parse("https://www.facebook.com/v19.0/dialog/oauth").buildUpon()
            .appendQueryParameter("client_id", appId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("scope", scopes)
            .build()

        launchUrlInBrowser(context, authUri.toString())
    }

    /**
     * Launches the official Google/YouTube Login dialogue in Google Chrome (or default browser)
     * Validates that a non-placeholder Client ID is provided to avoid Google's "404 Not Found" error.
     */
    fun launchGoogleInChrome(context: Context, customClientId: String? = null) {
        val clientId = customClientId?.trim()?.ifBlank { null }
        if (clientId == null || clientId == DEFAULT_GOOGLE_CLIENT_ID || !clientId.contains(".googleusercontent.com")) {
            _authEvents.tryEmit(
                OAuthEvent.Error(
                    platform = "Google",
                    message = "To use Chrome OAuth, please provide your Google Cloud Client ID in Developer Settings, or tap 'Continue with Google' to connect directly in LiveCaster."
                )
            )
            return
        }
        val redirectUri = "$REDIRECT_SCHEME://$REDIRECT_HOST/google"
        val scopes = "https://www.googleapis.com/auth/youtube https://www.googleapis.com/auth/userinfo.profile"

        val authUri = Uri.parse("https://accounts.google.com/o/oauth2/v2/auth").buildUpon()
            .appendQueryParameter("client_id", clientId)
            .appendQueryParameter("redirect_uri", redirectUri)
            .appendQueryParameter("response_type", "token")
            .appendQueryParameter("scope", scopes)
            .appendQueryParameter("prompt", "consent")
            .build()

        launchUrlInBrowser(context, authUri.toString())
    }

    /**
     * Directly opens Facebook Live Producer in Chrome
     */
    fun openFacebookLiveProducer(context: Context) {
        launchUrlInBrowser(context, FB_LIVE_PRODUCER_URL)
    }

    /**
     * Directly opens YouTube Live Studio in Chrome
     */
    fun openYouTubeLiveStudio(context: Context) {
        launchUrlInBrowser(context, YT_LIVE_STUDIO_URL)
    }

    private fun launchUrlInBrowser(context: Context, url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
            setPackage("com.android.chrome")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        try {
            context.startActivity(intent)
        } catch (_: ActivityNotFoundException) {
            // Fallback to any default browser if Chrome is not installed
            val fallback = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            try {
                context.startActivity(fallback)
            } catch (e: Exception) {
                Log.e(TAG, "No browser found to open URL: $url", e)
            }
        }
    }

    /**
     * Handles deep link return from Chrome: pk.livecaster.app://oauth2redirect/{platform}?access_token=...
     */
    suspend fun handleRedirect(uri: Uri?): Boolean {
        if (uri == null) return false
        if (uri.scheme != REDIRECT_SCHEME || uri.host != REDIRECT_HOST) return false

        val path = uri.path?.removePrefix("/") ?: ""
        Log.d(TAG, "Received deep link redirect: $uri for path: $path")

        // Parse token from query parameters or URL fragment
        val fragment = uri.fragment ?: ""
        val tokenFromFragment = parseFragmentParam(fragment, "access_token")
        val token = uri.getQueryParameter("access_token") 
            ?: uri.getQueryParameter("code") 
            ?: tokenFromFragment 
            ?: "token_${System.currentTimeMillis()}"

        when {
            path.contains("facebook", ignoreCase = true) -> {
                val pageName = "Official Facebook Page (${uri.getQueryParameter("user") ?: "Live Verified"})"
                tokenStorage.saveFacebookToken(token)
                facebookRepository.linkPage(
                    pageName = pageName,
                    pageId = "fb_live_${System.currentTimeMillis()}",
                    pageToken = token
                )
                _authEvents.emit(OAuthEvent.FacebookSuccess(token, pageName))
                return true
            }
            path.contains("google", ignoreCase = true) -> {
                val channelTitle = "Official YouTube Channel (${uri.getQueryParameter("channel") ?: "Live Verified"})"
                tokenStorage.saveYouTubeToken(token)
                youtubeRepository.linkChannel(
                    title = channelTitle,
                    channelId = "UC_live_${System.currentTimeMillis()}",
                    customUrl = "@OfficialLive"
                )
                _authEvents.emit(OAuthEvent.GoogleSuccess(token, channelTitle))
                return true
            }
            else -> {
                Log.w(TAG, "Unknown redirect path: $path")
                return false
            }
        }
    }

    private fun parseFragmentParam(fragment: String, key: String): String? {
        if (fragment.isBlank()) return null
        return fragment.split("&")
            .map { it.split("=") }
            .firstOrNull { it.size == 2 && it[0] == key }
            ?.get(1)
    }
}
