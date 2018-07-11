package cz.ackee.sample.interactor

import android.content.Intent
import android.util.Log
import cz.ackee.rxoauth.OauthCredentials
import cz.ackee.rxoauth.RefreshTokenFailListener
import cz.ackee.rxoauth.RxOauthManager
import cz.ackee.sample.App
import cz.ackee.sample.login.MainActivity
import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.ApiDescriptionImpl
import io.reactivex.Completable
import io.reactivex.Single

/**
 * Implementation of api
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 */
class ApiInteractorImpl : IApiInteractor {

    private val rxOauth: RxOauthManager = RxOauthManager(App.instance, this, object : RefreshTokenFailListener {
        override fun onRefreshTokenFailed() {
            Log.d(ApiInteractorImpl::class.java.name, "ApiInteractorImpl: should logout")
            App.instance.startActivity(Intent(App.instance, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }
    })

    private val apiDescription: ApiDescription

    init {
        apiDescription = ApiDescriptionImpl()
    }

    override fun login(name: String, password: String): Single<LoginResponse> {
        return apiDescription.login(name, password)
                .doOnSuccess { this.rxOauth.storeCredentials(it) }
                .compose(rxOauth.wrapWithOAuthHandlingSingle())
    }

    override fun getData(): Single<List<SampleItem>> {
        return apiDescription.getData()
                .compose(rxOauth.wrapWithOAuthHandlingSingle())
    }

    override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
        return apiDescription.refreshAccessToken(refreshToken)
    }

    override fun something(): Single<String> {
        return apiDescription.something()
                .compose(rxOauth.wrapWithOAuthHandlingSingle())
    }

    override fun logout(): Completable {
        return apiDescription.logout()
    }
}
