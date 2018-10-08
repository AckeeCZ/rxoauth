package cz.ackee.sample.interactor

import cz.ackee.rxoauth.OAuthCredentials
import cz.ackee.sample.model.SampleItem
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Interactor for communicating with API
 */
interface IApiInteractor {

    fun getData(): Single<List<SampleItem>>

    fun login(name: String, password: String): Single<OAuthCredentials>

    fun logout(): Completable
}
