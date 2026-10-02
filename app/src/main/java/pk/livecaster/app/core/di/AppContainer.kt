package pk.livecaster.app.core.di

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import pk.livecaster.app.auth.data.datasource.AuthLocalDataSource
import pk.livecaster.app.auth.data.datasource.AuthRemoteDataSource
import pk.livecaster.app.auth.data.repository.AuthRepositoryImpl
import pk.livecaster.app.auth.domain.repository.AuthRepository
import pk.livecaster.app.auth.domain.usecase.LoginUseCase
import pk.livecaster.app.auth.domain.usecase.LogoutUseCase
import pk.livecaster.app.broadcast.data.repository.BroadcastRepositoryImpl
import pk.livecaster.app.broadcast.domain.repository.BroadcastRepository
import pk.livecaster.app.broadcast.domain.usecase.CreateBroadcastUseCase
import pk.livecaster.app.broadcast.domain.usecase.GetBroadcastsUseCase
import pk.livecaster.app.broadcast.domain.usecase.UpdateBroadcastStatusUseCase
import pk.livecaster.app.core.network.NetworkClient
import pk.livecaster.app.core.security.SecureTokenStorage
import pk.livecaster.app.core.storage.AppDatabase
import pk.livecaster.app.facebook.data.api.FacebookApiService
import pk.livecaster.app.facebook.data.repository.FacebookRepositoryImpl
import pk.livecaster.app.facebook.domain.repository.FacebookRepository
import pk.livecaster.app.facebook.domain.usecase.CreateFacebookLiveStreamUseCase
import pk.livecaster.app.facebook.domain.usecase.GetFacebookPagesUseCase
import pk.livecaster.app.settings.data.SettingsRepositoryImpl
import pk.livecaster.app.streaming.publisher.RtmpPublisher
import pk.livecaster.app.youtube.data.api.YouTubeApiService
import pk.livecaster.app.youtube.data.repository.YouTubeRepositoryImpl
import pk.livecaster.app.youtube.domain.repository.YouTubeRepository
import pk.livecaster.app.youtube.domain.usecase.CreateYouTubeLiveStreamUseCase
import pk.livecaster.app.youtube.domain.usecase.GetYouTubeChannelsUseCase

class AppContainer(val context: Context) {

    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    // Security & Storage
    val tokenStorage by lazy { SecureTokenStorage(context) }
    val networkClient by lazy { NetworkClient(tokenStorage) }
    val database by lazy { AppDatabase.getInstance(context) }

    // Repositories
    val authLocalDataSource by lazy { AuthLocalDataSource(tokenStorage) }
    val authRemoteDataSource by lazy { AuthRemoteDataSource() }
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(authLocalDataSource, authRemoteDataSource)
    }

    val facebookApiService by lazy { networkClient.createService<FacebookApiService>() }
    val facebookRepository: FacebookRepository by lazy {
        FacebookRepositoryImpl(facebookApiService, tokenStorage)
    }

    val youtubeApiService by lazy { networkClient.createService<YouTubeApiService>() }
    val youtubeRepository: YouTubeRepository by lazy {
        YouTubeRepositoryImpl(youtubeApiService, tokenStorage)
    }

    val broadcastRepository: BroadcastRepository by lazy {
        BroadcastRepositoryImpl(database.broadcastDao(), database.destinationDao())
    }

    val settingsRepository by lazy { SettingsRepositoryImpl(context) }

    // Chrome OAuth Manager
    val oAuthChromeManager by lazy {
        pk.livecaster.app.core.auth.OAuthChromeManager(
            tokenStorage = tokenStorage,
            facebookRepository = facebookRepository,
            youtubeRepository = youtubeRepository
        )
    }

    // Streaming Publisher
    val rtmpPublisher by lazy { RtmpPublisher(appScope) }

    // Use Cases
    val loginUseCase by lazy { LoginUseCase(authRepository) }
    val logoutUseCase by lazy { LogoutUseCase(authRepository) }

    val getFacebookPagesUseCase by lazy { GetFacebookPagesUseCase(facebookRepository) }
    val createFacebookLiveStreamUseCase by lazy { CreateFacebookLiveStreamUseCase(facebookRepository) }

    val getYouTubeChannelsUseCase by lazy { GetYouTubeChannelsUseCase(youtubeRepository) }
    val createYouTubeLiveStreamUseCase by lazy { CreateYouTubeLiveStreamUseCase(youtubeRepository) }

    val getBroadcastsUseCase by lazy { GetBroadcastsUseCase(broadcastRepository) }
    val createBroadcastUseCase by lazy { CreateBroadcastUseCase(broadcastRepository) }
    val updateBroadcastStatusUseCase by lazy { UpdateBroadcastStatusUseCase(broadcastRepository) }
}
