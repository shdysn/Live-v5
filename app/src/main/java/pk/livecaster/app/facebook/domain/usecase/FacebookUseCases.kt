package pk.livecaster.app.facebook.domain.usecase

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.facebook.domain.model.FacebookLiveVideo
import pk.livecaster.app.facebook.domain.model.FacebookPage
import pk.livecaster.app.facebook.domain.repository.FacebookRepository

class GetFacebookPagesUseCase(private val repository: FacebookRepository) {
    operator fun invoke(): Flow<List<FacebookPage>> = repository.getPages()
    suspend fun refresh(): Resource<List<FacebookPage>> = repository.refreshPages()
}

class CreateFacebookLiveStreamUseCase(private val repository: FacebookRepository) {
    suspend operator fun invoke(pageId: String, title: String, description: String): Resource<FacebookLiveVideo> {
        if (title.isBlank()) return Resource.Error("Broadcast title is required for Facebook Live")
        return repository.createLiveStream(pageId, title, description)
    }
}
