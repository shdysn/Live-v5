package pk.livecaster.app.facebook.domain.repository

import kotlinx.coroutines.flow.Flow
import pk.livecaster.app.core.common.Resource
import pk.livecaster.app.facebook.domain.model.FacebookLiveVideo
import pk.livecaster.app.facebook.domain.model.FacebookPage

interface FacebookRepository {
    fun getPages(): Flow<List<FacebookPage>>
    suspend fun refreshPages(): Resource<List<FacebookPage>>
    suspend fun createLiveStream(
        pageId: String,
        title: String,
        description: String
    ): Resource<FacebookLiveVideo>
    suspend fun createProfileLiveStream(
        title: String,
        description: String
    ): Resource<FacebookLiveVideo>
    suspend fun endLiveStream(liveVideoId: String): Resource<Unit>
    suspend fun linkPage(pageName: String, pageId: String, pageToken: String): Resource<FacebookPage>
    suspend fun unlinkPage(pageId: String): Resource<Unit>
    fun getSavedToken(): String?
    fun saveToken(token: String)
}
