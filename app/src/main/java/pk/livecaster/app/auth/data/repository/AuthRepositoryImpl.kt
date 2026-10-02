package pk.livecaster.app.auth.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.auth.data.datasource.AuthLocalDataSource
import pk.livecaster.app.auth.data.datasource.AuthRemoteDataSource
import pk.livecaster.app.auth.domain.model.AuthSession
import pk.livecaster.app.auth.domain.model.User
import pk.livecaster.app.auth.domain.repository.AuthRepository
import pk.livecaster.app.core.common.Resource

class AuthRepositoryImpl(
    private val localDataSource: AuthLocalDataSource,
    private val remoteDataSource: AuthRemoteDataSource
) : AuthRepository {

    override fun getCurrentUser(): Flow<User?> = localDataSource.currentUserFlow

    override suspend fun login(emailOrUsername: String, accessKey: String): Resource<AuthSession> {
        return try {
            delay(500) // Simulated auth network handshake
            val (user, token) = remoteDataSource.authenticate(emailOrUsername, accessKey)
            localDataSource.setLoggedInUser(user, token)
            val session = AuthSession(
                user = user,
                token = token,
                expiresAt = System.currentTimeMillis() + 86400000L * 7
            )
            Resource.Success(session)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Authentication failed", e)
        }
    }

    override suspend fun logout(): Resource<Unit> {
        return try {
            localDataSource.setLoggedInUser(null, null)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.message ?: "Logout failed", e)
        }
    }

    override suspend fun isLoggedIn(): Boolean = localDataSource.hasToken()
}
