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
import retrofit2.HttpException

/**
 * Tests for [RxOAuthManager] class
 * = */
class RxOAuthManagerTest {

    private var firstRun: Boolean = false

    private val accessToken = "abc"
    private val refreshToken = "def"
    private val expiresIn = 3600L
    private val successResult = "ok"
    private val credentials = DefaultOAuthCredentials(accessToken, refreshToken, expiresIn)

    private val unauthorizedException = HttpException(retrofit2.Response.error<Any>(401, mock(ResponseBody::class.java)))
    private val badRequestException = HttpException(retrofit2.Response.error<Any>(400, mock(ResponseBody::class.java)))

    private val refreshSuccess: (String) -> Single<OAuthCredentials> = { Single.just(credentials) }
    private val refreshError: (String) -> Single<OAuthCredentials> = { Single.error(badRequestException) }

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
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
        val result = Observable.just(successResult).compose(oauthManager.wrapWithOAuthHandlingObservable()).blockingFirst()
        assertEquals(successResult, result)
    }

    @Test
    fun testExpiredAccessTokenLocal() {
        OAuthStore(getTargetContext()).saveCredentials(credentials.copy(expiresIn = 0))
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
        val result = Observable.just(successResult).compose(oauthManager.wrapWithOAuthHandlingObservable()).blockingFirst()
        assertEquals(successResult, result)
    }

    @Test
    fun testExpiredAccessTokenSingleLocal() {
        OAuthStore(getTargetContext()).saveCredentials(credentials.copy(expiresIn = 0))
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
        val result = Single.just(successResult).compose(oauthManager.wrapWithOAuthHandlingSingle()).blockingGet()
        assertEquals(successResult, result)
    }

    @Test
    fun testExpiredAccessTokenCompletableLocal() {
        OAuthStore(getTargetContext()).saveCredentials(credentials.copy(expiresIn = 0))
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
        Completable.complete()
                .compose(oauthManager.wrapWithOAuthHandlingCompletable())
                .test()
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun testExpiredAccessTokenServer() {
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
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
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
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
        val oauthManager = RxOAuthManager(getTargetContext(), refreshSuccess)
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
        val oauthManager = RxOAuthManager(getTargetContext(), refreshError)
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
    }

    private fun cleanStore() {
        OAuthStore(getTargetContext()).clearCredentials()
    }
}