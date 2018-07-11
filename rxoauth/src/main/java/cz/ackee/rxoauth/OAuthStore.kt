package cz.ackee.rxoauth

import android.content.Context
import android.content.SharedPreferences

/**
 * Persistence store of OAuth credentials.
 */
class OAuthStore {

    companion object {
        private const val SP_NAME = "oauth2"
        private const val ACCESS_TOKEN_KEY = "oath2_access_token"
        private const val REFRESH_TOKEN_KEY = "oath2_refresh_token"
    }

    private val sp: SharedPreferences

    val accessToken: String?
        get() = sp.getString(ACCESS_TOKEN_KEY, null)

    val refreshToken: String?
        get() = sp.getString(REFRESH_TOKEN_KEY, null)

    constructor(ctx: Context) {
        sp = ctx.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE)
    }

    constructor(sp: SharedPreferences) {
        this.sp = sp
    }

    fun saveOauthCredentials(accessToken: String, refreshToken: String) {
        sp.edit()
                .putString(ACCESS_TOKEN_KEY, accessToken)
                .putString(REFRESH_TOKEN_KEY, refreshToken)
                .apply()
    }

    fun onLogout() {
        sp.edit().clear().apply()
    }

    fun saveOauthCredentials(credentials: OauthCredentials) {
        saveOauthCredentials(credentials.accessToken, credentials.refreshToken)
    }
}
