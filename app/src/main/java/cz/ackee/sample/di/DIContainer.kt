package cz.ackee.sample.di

import cz.ackee.rxoauth.RxOAuthManager
import cz.ackee.rxoauth.adapter.RxOauthCallAdapterFactory
import cz.ackee.sample.App
import cz.ackee.sample.interactor.ApiInteractorImpl
import cz.ackee.sample.interactor.IApiInteractor
import cz.ackee.sample.model.Logouter
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription
import io.appflate.restmock.RESTMockServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Simple DI container that provides dependencies
 */
class DIContainer(val app: App) {

    val rxOAuthManager = RxOAuthManager(
            context = app,
            refreshTokenAction = { authApiDescription.refreshAccessToken(it).map { it } },
            onRefreshTokenFailed = { logouter.logout() }
    )

    val logouter = Logouter(app)

    val retrofitBuilder: Retrofit.Builder
        get() = Retrofit.Builder()
                .baseUrl(RESTMockServer.getUrl())
                .addConverterFactory(MoshiConverterFactory.create())

    val authApiDescription = retrofitBuilder
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(AuthApiDescription::class.java)

    val apiDescription: ApiDescription = retrofitBuilder
            .client(OkHttpClient.Builder()
                    .addNetworkInterceptor(rxOAuthManager.provideAuthInterceptor())
                    .build())
            .addCallAdapterFactory(RxOauthCallAdapterFactory(rxOAuthManager))
            .build()
            .create(ApiDescription::class.java)

    val apiInteractor: IApiInteractor = ApiInteractorImpl(rxOAuthManager, apiDescription, authApiDescription)
}
