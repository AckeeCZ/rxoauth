package cz.ackee.rxoauth

import android.support.test.InstrumentationRegistry.getTargetContext
import io.reactivex.*
import io.reactivex.functions.Function
import okhttp3.MediaType
import okhttp3.ResponseBody
import okio.BufferedSource
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*
import retrofit2.HttpException

/**
 * Tests for [RxOauthManager] class
 * = */
class RxOauthManagerTest {

    private var firstRun: Boolean = false

    @Before
    fun beforeTest() {
        firstRun = true
    }

    @Test
    fun testSuccessFullRequest() {
        val authService = object : RefreshTokenService {
            override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
                return Single.just(mock(OauthCredentials::class.java))
            }
        }

        val eventListener = object : RefreshTokenFailListener {
            override fun onRefreshTokenFailed() {
            }
        }

        val managing = RxOauthManager(getTargetContext(), authService, eventListener)
        assertEquals(Observable.just("ok").compose(managing.wrapWithOAuthHandlingObservable()).blockingFirst(), "ok")
    }

    @Test
    fun testExpiredAccessToken() {
        val mockedCredentials = mock(OauthCredentials::class.java)
        val unauthorizedException = HttpException(retrofit2.Response.error<Any>(401, object : ResponseBody() {
            override fun contentType(): MediaType? {
                return null
            }

            override fun contentLength(): Long {
                return 0
            }

            override fun source(): BufferedSource? {
                return null
            }
        }))

        `when`(mockedCredentials.accessToken).thenReturn("abc")
        `when`(mockedCredentials.refreshToken).thenReturn("def")
        val authService = object : RefreshTokenService {
            override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
                return Single.just(mockedCredentials)
            }
        }

        val eventListener = object : RefreshTokenFailListener {
            override fun onRefreshTokenFailed() {
            }
        }
        val managing = RxOauthManager(getTargetContext(), authService, eventListener)
        val badObservable = Observable.just("ok")
                .flatMap(Function<String, ObservableSource<*>> { s ->
                    if (firstRun) {
                        firstRun = false
                        return@Function Observable.error<String>(unauthorizedException)
                    }
                    Observable.just(s)
                })
                .compose(managing.wrapWithOAuthHandlingObservable())
        assertEquals(badObservable.blockingFirst(), "ok")
    }

    @Test
    fun testExpiredAccessTokenSingle() {
        val mockedCredentials = mock(OauthCredentials::class.java)
        val unauthorizedException = HttpException(retrofit2.Response.error<Any>(401, object : ResponseBody() {
            override fun contentType(): MediaType? {
                return null
            }

            override fun contentLength(): Long {
                return 0
            }

            override fun source(): BufferedSource? {
                return null
            }
        }))

        `when`(mockedCredentials.accessToken).thenReturn("abc")
        `when`(mockedCredentials.refreshToken).thenReturn("def")
        val authService = object : RefreshTokenService {
            override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
                return Single.just(mockedCredentials)
            }
        }

        val eventListener = object : RefreshTokenFailListener {
            override fun onRefreshTokenFailed() {
            }
        }
        val managing = RxOauthManager(getTargetContext(), authService, eventListener)
        val badObservable = Single.just("ok")
                .flatMap(Function<String, SingleSource<*>> { s ->
                    if (firstRun) {
                        firstRun = false
                        return@Function Single.error<String>(unauthorizedException)
                    }
                    Single.just(s)
                })
                .compose(managing.wrapWithOAuthHandlingSingle())
        assertEquals(badObservable.blockingGet(), "ok")
    }

    @Test
    fun testExpiredAccessTokenCompletable() {
        val mockedCredentials = mock(OauthCredentials::class.java)
        val unauthorizedException = HttpException(retrofit2.Response.error<Any>(401, object : ResponseBody() {
            override fun contentType(): MediaType? {
                return null
            }

            override fun contentLength(): Long {
                return 0
            }

            override fun source(): BufferedSource? {
                return null
            }
        }))

        `when`(mockedCredentials.accessToken).thenReturn("abc")
        `when`(mockedCredentials.refreshToken).thenReturn("def")
        val authService = object : RefreshTokenService {
            override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
                return Single.just(mockedCredentials)
            }
        }

        val eventListener = object : RefreshTokenFailListener {
            override fun onRefreshTokenFailed() {
            }
        }
        val managing = RxOauthManager(getTargetContext(), authService, eventListener)

        Observable.just("ok")
                .flatMapCompletable(Function {
                    if (firstRun) {
                        firstRun = false
                        return@Function Completable.error(unauthorizedException)
                    }
                    Completable.complete()
                })
                .compose(managing.wrapWithOAuthHandlingCompletable())
                .test()
                .assertNoErrors()
                .assertComplete()
    }

    @Test
    fun testExpiredRefreshToken() {
        val mockedCredentials = mock(OauthCredentials::class.java)
        val unauthorizedException = HttpException(retrofit2.Response.error<Any>(401, mock(ResponseBody::class.java)))
        val badRequestException = HttpException(retrofit2.Response.error<Any>(400, mock(ResponseBody::class.java)))
        val eventListener = mock(RefreshTokenFailListener::class.java)
        `when`(mockedCredentials.accessToken).thenReturn("abc")
        `when`(mockedCredentials.refreshToken).thenReturn("def")

        val authService = object : RefreshTokenService {
            override fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials> {
                return Single.error(badRequestException)
            }
        }

        val managing = RxOauthManager(getTargetContext(), authService, eventListener)
        val badObservable = Observable.just("ok")
                .flatMap(Function<String, ObservableSource<*>> { s ->
                    if (firstRun) {
                        firstRun = false
                        return@Function Observable.error<String>(unauthorizedException)
                    }
                    Observable.just(s)
                })
                .compose(managing.wrapWithOAuthHandlingObservable())
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
}