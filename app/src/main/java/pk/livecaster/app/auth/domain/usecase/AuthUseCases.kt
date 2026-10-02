package pk.livecaster.app.auth.domain.usecase

import pk.livecaster.app.auth.domain.model.AuthSession
import pk.livecaster.app.auth.domain.repository.AuthRepository
import pk.livecaster.app.core.common.Resource

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(email: String, key: String): Resource<AuthSession> {
        if (email.isBlank()) {
            return Resource.Error("Email or username cannot be empty")
        }
        if (key.length < 4) {
            return Resource.Error("Studio access key must be at least 4 characters")
        }
        return authRepository.login(email, key)
    }
}

class LogoutUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(): Resource<Unit> {
        return authRepository.logout()
    }
}
