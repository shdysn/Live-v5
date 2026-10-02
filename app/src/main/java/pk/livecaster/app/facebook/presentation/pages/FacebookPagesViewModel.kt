package pk.livecaster.app.facebook.presentation.pages

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.facebook.domain.model.FacebookPage
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.facebook.domain.usecase.GetFacebookPagesUseCase

data class FacebookPagesUiState(
    val pages: List<FacebookPage> = emptyList(),
    val isLoading: Boolean = false,
    val selectedPageId: String? = null,
    val message: String? = null
)

class FacebookPagesViewModel(
    private val getPagesUseCase: GetFacebookPagesUseCase,
    private val repository: FacebookRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FacebookPagesUiState())
    val uiState: StateFlow<FacebookPagesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            getPagesUseCase().collect { pages ->
                _uiState.value = _uiState.value.copy(
                    pages = pages,
                    selectedPageId = _uiState.value.selectedPageId ?: pages.firstOrNull()?.id
                )
            }
        }
    }

    fun selectPage(pageId: String) {
        _uiState.value = _uiState.value.copy(selectedPageId = pageId)
    }

    fun linkNewPage(pageName: String, pageId: String, pageToken: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            when (val res = repository.linkPage(pageName, pageId, pageToken)) {
                is Resource.Success -> {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        message = "Facebook Page '${res.data.name}' linked successfully"
                    )
                }
                is Resource.Error -> {
                    _uiState.value = _uiState.value.copy(isLoading = false, message = res.message)
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun unlinkPage(pageId: String) {
        viewModelScope.launch {
            repository.unlinkPage(pageId)
        }
    }

    fun clearMessage() {
        _uiState.value = _uiState.value.copy(message = null)
    }
}
