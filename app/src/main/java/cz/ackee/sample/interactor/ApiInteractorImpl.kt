package cz.ackee.sample.interactor

import android.content.Intent
import android.util.Log
import cz.ackee.rxoauth.*
import cz.ackee.sample.App
import cz.ackee.sample.login.MainActivity
import cz.ackee.sample.model.LoginResponse
import cz.ackee.sample.model.SampleItem
import cz.ackee.sample.model.rest.ApiDescription
import io.appflate.restmock.RESTMockServer
import io.reactivex.Completable
import io.reactivex.Single
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Implementation of api
 */
class ApiInteractorImpl : IApiInteractor {

    val store = OAuthStore(App.instance)

    private val rxOauth: RxOauthManager = RxOauthManager(store, this, object : RefreshTokenFailListener {
        override fun onRefreshTokenFailed() {
            Log.d(ApiInteractorImpl::class.java.name, "ApiInteractorImpl: should logout")
            App.instance.startActivity(Intent(App.instance, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        }
    })

    private val apiDescription: ApiDescription

    init {
        apiDescription = Retrofit.Builder()
                .baseUrl(RESTMockServer.getUrl())
                .client(OkHttpClient.Builder()
                        .addNetworkInterceptor(AuthInterceptor(store))
                        .build())
                .addConverterFactory(MoshiConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(ApiDescription::class.java)
    }

    override fun login(name: String, password: String): Single<LoginResponse> {
        return apiDescription.login(name, password)
                .doOnSuccess { this.store.saveOauthCredentials(it) }
                .compose(rxOauth.wrapWithOAuthHandlingSingle())
    }

    override fun getData(): Single<List<SampleItem>> {
        return apiDescription.getData()
                .compose(rxOauth.wrapWithOAuthHandlingSingle())
    }

    override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
        return apiDescription.refreshAccessToken(refreshToken).map { it }
    }

    override fun logout(): Completable {
        return apiDescription.logout()
    }
}
