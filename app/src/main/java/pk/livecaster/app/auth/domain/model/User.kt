package pk.livecaster.app.auth.domain.model

data class User(
    val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String? = null,
    val isVerifiedBroadcaster: Boolean = true,
    val streamingTier: String = "Studio Pro"
)

data class AuthSession(
    val user: User,
    val token: String,
    val expiresAt: Long
)
