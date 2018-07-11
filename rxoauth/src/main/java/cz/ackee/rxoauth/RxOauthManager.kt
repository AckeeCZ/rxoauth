package cz.ackee.rxoauth

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.*
import io.reactivex.functions.Function

/**
 * Manager containing logic of
 */
class RxOauthManager(private val oAuthStore: OAuthStore,
                     private val refreshTokenService: RefreshTokenService,
                     private val refreshTokenFailListener: RefreshTokenFailListener,
                     private val errorChecker: ErrorChecker = DefaultErrorChecker()) {

    constructor(sp: SharedPreferences, refreshTokenService: RefreshTokenService, refreshTokenFailListener: RefreshTokenFailListener, errorChecker: ErrorChecker = DefaultErrorChecker()) :
            this(OAuthStore(sp), refreshTokenService, refreshTokenFailListener, errorChecker)

    constructor(ctx: Context, refreshTokenService: RefreshTokenService, refreshTokenFailListener: RefreshTokenFailListener, errorChecker: ErrorChecker = DefaultErrorChecker()) :
            this(OAuthStore(ctx), refreshTokenService, refreshTokenFailListener, errorChecker)

    init {
        initRefreshTokenObservable()
    }

    private var refreshTokenObservable: Observable<OauthCredentials>? = null

    private fun initRefreshTokenObservable() {
        refreshTokenObservable = Single.defer { refreshAccessToken() }
                .toObservable()
                .publish()
                .refCount()
                .doOnComplete { initRefreshTokenObservable() }
    }

    /**
     * Wrap upstream observable with oauth refresh access token handling
     */
    fun <T> wrapWithOAuthHandlingObservable(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            upstream.onErrorResumeNext(Function<Throwable, Observable<out T>> { throwable ->
                if (errorChecker.isExpiredAccessToken(throwable)) {
                    refreshTokenObservable!!
                            .flatMap { upstream }
                } else Observable.error(throwable)
            })
        }
    }

    /**
     * Wrap upstream Single with oauth refresh access token handling
     */
    fun <T> wrapWithOAuthHandlingSingle(): SingleTransformer<T, T> {
        return SingleTransformer { upstream ->
            upstream
                    .onErrorResumeNext { throwable ->
                        if (errorChecker.isExpiredAccessToken(throwable)) {
                            refreshTokenObservable!!
                                    .flatMapSingle { upstream }.firstOrError()
                        } else Single.error(throwable)
                    }
        }
    }

    /**
     * Wrap upstream Completable with oauth refresh access token handling
     */
    fun wrapWithOAuthHandlingCompletable(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            upstream
                    .onErrorResumeNext { throwable ->
                        if (errorChecker.isExpiredAccessToken(throwable)) {
                            refreshTokenObservable!!
                                    .flatMapCompletable { upstream }
                        } else Completable.error(throwable)
                    }
        }
    }

    private fun refreshAccessToken(): Single<OauthCredentials> {
        val refreshToken = oAuthStore.refreshToken
        return refreshTokenService.refreshAccessToken(refreshToken)
                .doOnSuccess { iCredentialsModel -> oAuthStore.saveOauthCredentials(iCredentialsModel) }
                .doOnError { throwable ->
                    if (errorChecker.isBadRefreshToken(throwable)) {
                        oAuthStore.onLogout()
                        refreshTokenFailListener.onRefreshTokenFailed()
                    }
                }
    }

    fun storeCredentials(credentialsModel: OauthCredentials) {
        this.oAuthStore.saveOauthCredentials(credentialsModel)
    }
}
