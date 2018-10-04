package cz.ackee.sample.interactor

import cz.ackee.rxoauth.OAuthCredentials
import cz.ackee.rxoauth.RxOAuthManager
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Implementation of api
 */
class ApiInteractorImpl(private val rxOAuthManager: RxOAuthManager,
                        private val apiDescription: ApiDescription,
                        private val authApiDescription: AuthApiDescription) : IApiInteractor {

    override fun login(name: String, password: String): Single<OAuthCredentials> {
        return authApiDescription.login(name, password)
                .doOnSuccess { rxOAuthManager.saveCredentials(it) }
                .map { it }
    }

    override fun getData(): Single<List<SampleItem>> {
        return apiDescription.getData()
    }

    override fun logout(): Completable {
        return authApiDescription.logout()
    }
}
