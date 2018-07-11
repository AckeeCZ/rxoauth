package cz.ackee.rxoauth

import android.text.TextUtils
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds `Authorization: Bearer {access_token}` to all requests, if access_token is set.
 */
class AuthInterceptor(internal var oAuthStore: OAuthStore) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val builder = originalRequest.newBuilder()

        if (!TextUtils.isEmpty(oAuthStore.accessToken)) {
            val accToken = oAuthStore.accessToken
            builder.addHeader("Authorization", "Bearer $accToken")
        }
        return chain.proceed(builder.build())
    }
}
