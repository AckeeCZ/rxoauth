package cz.ackee.sample.interactor

import cz.ackee.rxoauth.RefreshTokenService
import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Interactor for communicating with API
 */
interface IApiInteractor : RefreshTokenService {

    fun getData(): Single<List<SampleItem>>

    fun login(name: String, password: String): Single<LoginResponse>

    fun logout(): Completable
}
