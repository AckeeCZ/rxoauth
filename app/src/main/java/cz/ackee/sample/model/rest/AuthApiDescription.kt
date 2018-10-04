package cz.ackee.sample.model.rest

import cz.ackee.rxoauth.DefaultOAuthCredentials
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api Description of auth endpoints
 */
interface AuthApiDescription {

    @POST("login")
    fun login(@Query("username") name: String, @Query("password") passwd: String): Single<DefaultOAuthCredentials>

    @POST("refresh_token")
    fun refreshAccessToken(@Query("refresh_token") refreshToken: String?): Single<DefaultOAuthCredentials>

    @POST("logout")
    fun logout(): Completable
}