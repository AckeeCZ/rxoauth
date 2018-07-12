package cz.ackee.sample.interactor

import cz.ackee.rxoauth.OAuthStore
import cz.ackee.rxoauth.OauthCredentials
import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Implementation of api
 */
class ApiInteractorImpl(private val oauthStore: OAuthStore,
                        private val apiDescription: ApiDescription,
                        private val authApiDescription: AuthApiDescription) : IApiInteractor {

    override fun login(name: String, password: String): Single<LoginResponse> {
        return authApiDescription.login(name, password)
                .doOnSuccess { this.oauthStore.saveOauthCredentials(it) }
    }

    override fun getData(): Single<List<SampleItem>> {
        return apiDescription.getData()
    }

    override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
        return authApiDescription.refreshAccessToken(refreshToken).map { it }
    }

    override fun logout(): Completable {
        return authApiDescription.logout()
    }
}
