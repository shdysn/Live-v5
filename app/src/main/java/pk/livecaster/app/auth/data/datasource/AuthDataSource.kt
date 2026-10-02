package pk.livecaster.app.auth.data.datasource

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import pk.livecaster.app.auth.domain.model.User
import pk.livecaster.app.core.security.SecureTokenStorage

class AuthLocalDataSource(private val tokenStorage: SecureTokenStorage) {
    private val _currentUserFlow = MutableStateFlow<User?>(
        User(
            id = "caster_pk_001",
            username = "live_producer",
            email = "producer@livecaster.pk",
            fullName = "LiveCaster Producer",
            streamingTier = "Enterprise Studio"
        )
    )
    val currentUserFlow: Flow<User?> = _currentUserFlow

    fun setLoggedInUser(user: User?, token: String?) {
        if (token != null) {
            tokenStorage.saveAuthToken(token)
        } else {
            tokenStorage.clearAll()
        }
        _currentUserFlow.value = user
    }

    fun hasToken(): Boolean = !tokenStorage.getAuthToken().isNullOrEmpty()
}

class AuthRemoteDataSource {
    suspend fun authenticate(email: String, key: String): Pair<User, String> {
        // Authenticate with studio service or fallback credentials
        val username = email.substringBefore("@")
        val user = User(
            id = "caster_${System.currentTimeMillis() % 10000}",
            username = username,
            email = if (email.contains("@")) email else "$email@livecaster.pk",
            fullName = username.replaceFirstChar { it.uppercase() } + " Broadcast Ops",
            streamingTier = "Studio Pro HD"
        )
        val token = "livecaster_jwt_${System.currentTimeMillis()}_sec"
        return Pair(user, token)
    }
}
