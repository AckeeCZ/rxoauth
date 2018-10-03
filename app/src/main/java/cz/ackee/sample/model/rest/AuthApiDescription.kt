package cz.ackee.sample.model.rest

import cz.ackee.rxoauth.DefaultOauthCredentials
import cz.ackee.rxoauth.OauthCredentials
import cz.ackee.rxoauth.RefreshTokenService
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api Description of auth endpoints
 */
interface AuthApiDescription {

    @POST("login")
    fun login(@Query("username") name: String, @Query("password") passwd: String): Single<DefaultOauthCredentials>

    @POST("refresh_token")
    fun refreshAccessToken(@Query("refresh_token") refreshToken: String?): Single<DefaultOauthCredentials>

    @POST("logout")
    fun logout(): Completable
}

class AuthService(private val apiDescription: AuthApiDescription) : RefreshTokenService {

    override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
        return apiDescription.refreshAccessToken(refreshToken).map { it }
    }
}