package cz.ackee.sample.model.rest

import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import io.reactivex.Completable
import io.reactivex.Single
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * Api description simulating Retrofit description
 */
interface ApiDescription {

    @GET("items")
    fun getData(): Single<List<SampleItem>>

    @POST("login")
    fun login(@Query("username") name: String, @Query("password") passwd: String): Single<LoginResponse>

    @POST("refresh_token")
    fun refreshAccessToken(@Query("refresh_token")refreshToken: String?): Single<LoginResponse>

    @POST("logout")
    fun logout(): Completable
}
