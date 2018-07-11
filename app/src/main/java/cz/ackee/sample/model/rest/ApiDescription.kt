package cz.ackee.sample.model.rest

import cz.ackee.rxoauth.OauthCredentials
import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Api description simulating Retrofit description
 */
interface ApiDescription {

    fun getData(): Single<List<SampleItem>>

    fun login(name: String, passwd: String): Single<LoginResponse>

    fun refreshAccessToken(refreshToken: String): Single<OauthCredentials>

    fun logout(): Completable

    fun something(): Single<String>
}
