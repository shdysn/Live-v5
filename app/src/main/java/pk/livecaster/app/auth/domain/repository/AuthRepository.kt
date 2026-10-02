package pk.livecaster.app.auth.domain.repository

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.auth.domain.model.AuthSession
import pk.livecaster.app.auth.domain.model.User
import pk.livecaster.app.core.common.Resource

interface AuthRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun login(emailOrUsername: String, accessKey: String): Resource<AuthSession>
    suspend fun logout(): Resource<Unit>
    suspend fun isLoggedIn(): Boolean
}
