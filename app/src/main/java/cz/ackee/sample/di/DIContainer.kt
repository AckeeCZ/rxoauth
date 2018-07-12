package cz.ackee.sample.di

import cz.ackee.rxoauth.AuthInterceptor
import cz.ackee.rxoauth.OAuthStore
import cz.ackee.rxoauth.RefreshTokenFailListener
import cz.ackee.rxoauth.RefreshTokenService
import cz.ackee.rxoauth.adapter.RxOauthCallAdapterFactory
import cz.ackee.sample.App
import cz.ackee.sample.interactor.ApiInteractorImpl
import cz.ackee.sample.interactor.IApiInteractor
import cz.ackee.sample.model.Logouter
import cz.ackee.sample.model.rest.ApiDescription
import cz.ackee.sample.model.rest.AuthApiDescription
import cz.ackee.sample.model.rest.AuthService
import io.appflate.restmock.RESTMockServer
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.moshi.MoshiConverterFactory

/**
 * Simple DI container that provides dependencies
 */
class DIContainer(val app: App) {

    val oauthStore = OAuthStore(app)

    val logouter = Logouter(app)

    val refreshTokenFailListener: RefreshTokenFailListener = logouter

    val retrofitBuilder: Retrofit.Builder
        get() = Retrofit.Builder()
                .baseUrl(RESTMockServer.getUrl())
                .addConverterFactory(MoshiConverterFactory.create())

    val authApiDescription = retrofitBuilder
            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            .build()
            .create(AuthApiDescription::class.java)

    val refreshTokenService: RefreshTokenService = AuthService(authApiDescription)

    val apiDescription: ApiDescription = retrofitBuilder
            .client(OkHttpClient.Builder()
                    .addNetworkInterceptor(AuthInterceptor(oauthStore))
                    .build())
            .addCallAdapterFactory(RxOauthCallAdapterFactory.create(
                    oAuthStore = oauthStore,
                    authService = refreshTokenService,
                    logoutEvent = refreshTokenFailListener
            ))
            .build()
            .create(ApiDescription::class.java)

    val apiInteractor: IApiInteractor = ApiInteractorImpl(oauthStore, apiDescription, authApiDescription)
}
