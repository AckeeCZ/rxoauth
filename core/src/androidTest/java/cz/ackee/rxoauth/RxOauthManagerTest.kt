package cz.ackee.rxoauth

import androidx.test.InstrumentationRegistry.getTargetContext
import io.reactivex.*
import io.reactivex.functions.Function
import okhttp3.ResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import retrofit2.HttpException

/**
 * Tests for [RxOauthManager] class
 * = */
class RxOauthManagerTest {

    private var firstRun: Boolean = false

    private val accessToken = "abc"
    private val refreshToken = "def"
    private val expiresIn = 3600L
    private val successResult = "ok"
    private val credentials = DefaultOauthCredentials(accessToken, refreshToken, expiresIn)

    private val eventListener = mock(RefreshTokenFailListener::class.java)
    private val unauthorizedException = HttpException(retrofit2.Response.error<Any>(401, mock(ResponseBody::class.java)))
    private val badRequestException = HttpException(retrofit2.Response.error<Any>(400, mock(ResponseBody::class.java)))

    private val authServiceSuccess = object : RefreshTokenService {
        override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
            return Single.just(credentials)
        }
    }
    private val authServiceError = object : RefreshTokenService {
        override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
            return Single.error(badRequestException)
        }
    }

    @Before
    fun setup() {
        cleanStore()
        firstRun = true
    }

    @After
    fun clean() {
        cleanStore()
    }

    @Test
    fun testSuccessFullRequest() {
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        val result = Observable.just(successResult).compose(oauthManager.wrapWithOAuthHandlingObservable()).blockingFirst()
        assertEquals(successResult, result)
    }

    @Test
    fun testExpiredAccessTokenLocal() {
        OAuthStore(getTargetContext()).saveOauthCredentials(credentials.copy(expiresIn = 0))
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        val result = Observable.just(successResult).compose(oauthManager.wrapWithOAuthHandlingObservable()).blockingFirst()
        assertEquals(successResult, result)
    }

    @Test
    fun testExpiredAccessTokenSingleLocal() {
        OAuthStore(getTargetContext()).saveOauthCredentials(credentials.copy(expiresIn = 0))
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        val result = Single.just(successResult).compose(oauthManager.wrapWithOAuthHandlingSingle()).blockingGet()
        assertEquals(successResult, result)
    }

    @Test
    fun testExpiredAccessTokenCompletableLocal() {
        OAuthStore(getTargetContext()).saveOauthCredentials(credentials.copy(expiresIn = 0))
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        Completable.complete()
                .compose(oauthManager.wrapWithOAuthHandlingCompletable())
                .test()
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun testExpiredAccessTokenServer() {
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        val badObservable = Observable.just(successResult)
                .flatMap(Function<String, ObservableSource<*>> { result ->
                    if (firstRun) {
                        firstRun = false
                        return@Function Observable.error<String>(unauthorizedException)
                    }
                    Observable.just(result)
                })
                .compose(oauthManager.wrapWithOAuthHandlingObservable())
        assertEquals(successResult, badObservable.blockingFirst())
    }

    @Test
    fun testExpiredAccessTokenSingleServer() {
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        val badObservable = Single.just(successResult)
                .flatMap(Function<String, SingleSource<*>> { result ->
                    if (firstRun) {
                        firstRun = false
                        return@Function Single.error<String>(unauthorizedException)
                    }
                    Single.just(result)
                })
                .compose(oauthManager.wrapWithOAuthHandlingSingle())
        assertEquals(successResult, badObservable.blockingGet())
    }

    @Test
    fun testExpiredAccessTokenCompletableServer() {
        val oauthManager = RxOauthManager(getTargetContext(), authServiceSuccess, eventListener)
        Observable.just(successResult)
                .flatMapCompletable(Function {
                    if (firstRun) {
                        firstRun = false
                        return@Function Completable.error(unauthorizedException)
                    }
                    Completable.complete()
                })
                .compose(oauthManager.wrapWithOAuthHandlingCompletable())
                .test()
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun testExpiredRefreshToken() {
        val oauthManager = RxOauthManager(getTargetContext(), authServiceError, eventListener)
        val badObservable = Observable.just(successResult)
                .flatMap(Function<String, ObservableSource<*>> { s ->
                    if (firstRun) {
                        firstRun = false
                        return@Function Observable.error<String>(unauthorizedException)
                    }
                    Observable.just(s)
                })
                .compose(oauthManager.wrapWithOAuthHandlingObservable())
        try {
            badObservable.blockingFirst()
            assertTrue("Couldnt be here, should failed", false)
        } catch (ex: RuntimeException) {
            ex.printStackTrace()
            assertTrue(ex is HttpException)
            assertEquals((ex as HttpException).code().toLong(), 400)
        }

        verify(eventListener).onRefreshTokenFailed()
    }

    private fun cleanStore() {
        OAuthStore(getTargetContext()).onLogout()
    }
}