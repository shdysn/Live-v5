package pk.livecaster.app.auth.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.auth.domain.model.User
import pk.livecaster.app.auth.domain.usecase.LoginUseCase
import pk.livecaster.app.auth.domain.usecase.LogoutUseCase
import pk.livecaster.app.auth.domain.repository.AuthRepository
import pk.livecaster.app.core.common.Resource

data class LoginUiState(
    val currentUser: User? = null,
    val email: String = "",
    val accessKey: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isLoginSuccess: Boolean = false
)

class LoginViewModel(
    private val loginUseCase: LoginUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepository.getCurrentUser().collect { user ->
                _uiState.value = _uiState.value.copy(currentUser = user)
            }
        }
    }

    fun onEmailChange(newEmail: String) {
        _uiState.value = _uiState.value.copy(email = newEmail, errorMessage = null)
    }

    fun onKeyChange(newKey: String) {
        _uiState.value = _uiState.value.copy(accessKey = newKey, errorMessage = null)
    }

    fun login() {
        val email = _uiState.value.email.trim()
        val key = _uiState.value.accessKey.trim()

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            when (val result = loginUseCase(email, key)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isLoginSuccess = true,
                        currentUser = result.data.user
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        errorMessage = result.message
                    )
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            logoutUseCase()
        }
    }
}
