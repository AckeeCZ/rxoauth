package cz.ackee.rxoauth

import android.content.Context
import android.content.SharedPreferences
import io.reactivex.*
import io.reactivex.functions.Function

/**
 * RxOAuthManager provides wrapping for RxJava2 streams, which automatically handles access token
 * expiration and performs refresh token logic defined with [refreshTokenAction], provided by user.
 * In case of success, new credentials are stored in [OAuthStore].
 *
 * The user may provide fallback for refresh token expiration in [onRefreshTokenFailed].
 *
 * The user may provide custom [ErrorChecker] containing access and refresh token expiration
 * checking logic. Otherwise, [DefaultErrorChecker] is applied.
 */
class RxOAuthManager internal constructor(private val oAuthStore: OAuthStore,
                                          private val refreshTokenAction: (String) -> Single<OAuthCredentials>,
                                          private val onRefreshTokenFailed: (Throwable) -> Unit = {},
                                          private val errorChecker: ErrorChecker = DefaultErrorChecker()) {

    constructor(sp: SharedPreferences, refreshTokenAction: (String) -> Single<OAuthCredentials>,
                onRefreshTokenFailed: (Throwable) -> Unit = {}, errorChecker: ErrorChecker = DefaultErrorChecker()) :
            this(OAuthStore(sp), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    constructor(context: Context, refreshTokenAction: (String) -> Single<OAuthCredentials>,
                onRefreshTokenFailed: (Throwable) -> Unit = {}, errorChecker: ErrorChecker = DefaultErrorChecker()) :
            this(OAuthStore(context), refreshTokenAction, onRefreshTokenFailed, errorChecker)

    init {
        initRefreshTokenObservable()
    }

    val accessToken get() = oAuthStore.accessToken

    val refreshToken get() = oAuthStore.refreshToken

    private var refreshTokenObservable: Observable<OAuthCredentials>? = null

    private fun initRefreshTokenObservable() {
        refreshTokenObservable = Single.defer { refreshAccessToken() }
                .toObservable()
                .share()
                .doOnComplete { initRefreshTokenObservable() }
    }

    internal fun <T> transformObservable(): ObservableTransformer<T, T> {
        return ObservableTransformer { upstream ->
            if (oAuthStore.tokenExpired()) {
                refreshTokenObservable!!.flatMap { upstream }
            } else {
                upstream.onErrorResumeNext(Function<Throwable, Observable<out T>> { throwable ->
                    if (errorChecker.invalidAccessToken(throwable)) {
                        refreshTokenObservable!!.flatMap { upstream }
                    } else Observable.error(throwable)
                })
            }
        }
    }

    internal fun <T> transformSingle(): SingleTransformer<T, T> {
        return SingleTransformer { upstream ->
            if (oAuthStore.tokenExpired()) {
                refreshTokenObservable!!.flatMapSingle { upstream }.firstOrError()
            } else {
                upstream.onErrorResumeNext { throwable ->
                    if (errorChecker.invalidAccessToken(throwable)) {
                        refreshTokenObservable!!.flatMapSingle { upstream }.firstOrError()
                    } else Single.error(throwable)
                }
            }
        }
    }

    internal fun transformCompletable(): CompletableTransformer {
        return CompletableTransformer { upstream ->
            if (oAuthStore.tokenExpired()) {
                refreshTokenObservable!!.flatMapCompletable { upstream }
            } else {
                upstream.onErrorResumeNext { throwable ->
                    if (errorChecker.invalidAccessToken(throwable)) {
                        refreshTokenObservable!!.flatMapCompletable { upstream }
                    } else Completable.error(throwable)
                }
            }
        }
    }

    fun saveCredentials(credentials: OAuthCredentials) {
        oAuthStore.saveCredentials(credentials)
    }

    fun clearCredentials() {
        oAuthStore.clearCredentials()
    }

    fun provideAuthInterceptor() = OAuthInterceptor(oAuthStore)

    private fun refreshAccessToken(): Single<OAuthCredentials> {
        val refreshToken = oAuthStore.refreshToken ?: ""
        return refreshTokenAction(refreshToken)
                .doOnSuccess { credentials -> saveCredentials(credentials) }
                .doOnError { throwable ->
                    if (errorChecker.invalidRefreshToken(throwable)) {
                        clearCredentials()
                        onRefreshTokenFailed(throwable)
                    }
                }
    }
}

/**
 * Wrap upstream observable with oauth refresh access token handling
 */
fun <T> Observable<T>.wrapWithOAuthHandlingObservable(rxOAuthManager: RxOAuthManager): Observable<T> = compose(rxOAuthManager.transformObservable())

/**
 * Wrap upstream Single with oauth refresh access token handling
 */
fun <T> Single<T>.wrapWithOAuthHandlingSingle(rxOAuthManager: RxOAuthManager): Single<T> = compose(rxOAuthManager.transformSingle())

/**
 * Wrap upstream Completable with oauth refresh access token handling
 */
fun Completable.wrapWithOAuthHandlingCompletable(rxOAuthManager: RxOAuthManager): Completable = compose(rxOAuthManager.transformCompletable())