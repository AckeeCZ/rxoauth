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
                     private val refreshTokenFailedListener: RefreshTokenFailedListener,
                     private val errorChecker: ErrorChecker = DefaultErrorChecker()) {

    constructor(sp: SharedPreferences, refreshTokenService: RefreshTokenService, refreshTokenFailedListener: RefreshTokenFailedListener, errorChecker: ErrorChecker = DefaultErrorChecker()) :
            this(OAuthStore(sp), refreshTokenService, refreshTokenFailedListener, errorChecker)

    constructor(ctx: Context, refreshTokenService: RefreshTokenService, refreshTokenFailedListener: RefreshTokenFailedListener, errorChecker: ErrorChecker = DefaultErrorChecker()) :
            this(OAuthStore(ctx), refreshTokenService, refreshTokenFailedListener, errorChecker)

    init {
        initRefreshTokenObservable()
    }

    private var refreshTokenObservable: Observable<OauthCredentials>? = null

    private fun initRefreshTokenObservable() {
        refreshTokenObservable = Single.defer { refreshAccessToken() }
                .toObservable()
                .share()
                .doOnComplete { initRefreshTokenObservable() }
    }

    /**
     * Wrap upstream observable with oauth refresh access token handling
     */
    fun <T> wrapWithOAuthHandlingObservable(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            if (oAuthStore.tokenExpired()) {
                refreshTokenObservable!!.flatMap { upstream }
            } else {
                upstream.onErrorResumeNext(Function<Throwable, Observable<out T>> { throwable ->
                    if (errorChecker.isExpiredAccessToken(throwable)) {
                        refreshTokenObservable!!.flatMap { upstream }
                    } else Observable.error(throwable)
                })
            }
        }
    }

    /**
     * Wrap upstream Single with oauth refresh access token handling
     */
    fun <T> wrapWithOAuthHandlingSingle(): SingleTransformer<T, T> {
        return SingleTransformer { upstream ->
            if (oAuthStore.tokenExpired()) {
                refreshTokenObservable!!.flatMapSingle { upstream }.firstOrError()
            } else {
                upstream.onErrorResumeNext { throwable ->
                    if (errorChecker.isExpiredAccessToken(throwable)) {
                        refreshTokenObservable!!.flatMapSingle { upstream }.firstOrError()
                    } else Single.error(throwable)
                }
            }
        }
    }

    /**
     * Wrap upstream Completable with oauth refresh access token handling
     */
    fun wrapWithOAuthHandlingCompletable(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            if (oAuthStore.tokenExpired()) {
                refreshTokenObservable!!.flatMapCompletable { upstream }
            } else {
                upstream.onErrorResumeNext { throwable ->
                    if (errorChecker.isExpiredAccessToken(throwable)) {
                        refreshTokenObservable!!.flatMapCompletable { upstream }
                    } else Completable.error(throwable)
                }
            }
        }
    }

    fun storeCredentials(credentialsModel: OauthCredentials) {
        this.oAuthStore.saveOauthCredentials(credentialsModel)
    }

    private fun refreshAccessToken(): Single<OauthCredentials> {
        val refreshToken = oAuthStore.refreshToken
        return refreshTokenService.refreshAccessToken(refreshToken)
                .doOnSuccess { credentials -> storeCredentials(credentials) }
                .doOnError { throwable ->
                    if (errorChecker.isBadRefreshToken(throwable)) {
                        oAuthStore.onLogout()
                        refreshTokenFailedListener.onRefreshTokenFailed(throwable)
                    }
                }
    }
}
