package pk.livecaster.app.core.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pk.livecaster.app.core.constants.AppConstants
import pk.livecaster.app.core.network.interceptor.AuthInterceptor
import pk.livecaster.app.core.security.SecureTokenStorage
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

class NetworkClient(private val tokenStorage: SecureTokenStorage) {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val authInterceptor = AuthInterceptor(tokenStorage)

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(AppConstants.BASE_API_URL)
        .client(okHttpClient)
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    inline fun <reified T> createService(): T = retrofit.create(T::class.java)
}
