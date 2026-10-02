package pk.livecaster.app.core.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import pk.livecaster.app.core.security.SecureTokenStorage

class AuthInterceptor(private val tokenStorage: SecureTokenStorage) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val token = tokenStorage.getAuthToken()

        val newRequest = if (!token.isNullOrEmpty() && !request.headers.names().contains("Authorization")) {
            request.newBuilder()
                .header("Authorization", "Bearer $token")
                .header("Accept", "application/json")
                .header("X-App-Platform", "Android")
                .build()
        } else {
            request.newBuilder()
                .header("Accept", "application/json")
                .header("X-App-Platform", "Android")
                .build()
        }

        return chain.proceed(newRequest)
    }
}
