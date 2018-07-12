package cz.ackee.sample.model.rest

import cz.ackee.rxoauth.OauthCredentials
import cz.ackee.rxoauth.RefreshTokenService
import cz.ackee.sample.model.LoginResponse
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api Description of auth endpoints
 */
interface AuthApiDescription {

    @POST("login")
    fun login(@Query("username") name: String, @Query("password") passwd: String): Single<LoginResponse>

    @POST("refresh_token")
    fun refreshAccessToken(@Query("refresh_token") refreshToken: String?): Single<LoginResponse>

    @POST("logout")
    fun logout(): Completable
}

class AuthService(private val apiDescription: AuthApiDescription) : RefreshTokenService {

    override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
        return apiDescription.refreshAccessToken(refreshToken).map { it }
    }
}