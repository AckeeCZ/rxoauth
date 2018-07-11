package cz.ackee.rxoauth

import android.support.test.InstrumentationRegistry.getTargetContext
import okhttp3.*
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Test for auth okhttp3 interceptor
 */
@RunWith(JUnit4::class)
class AuthInterceptorTest {

    lateinit var store: OAuthStore
    lateinit var samplerequest: Request
    lateinit var responseBuilder: Response.Builder

    @Before
    fun clean() {
        store = OAuthStore(getTargetContext())
        //to make sure its new
        store.onLogout()
        samplerequest = Request.Builder()
                .url("https://example.com")
                .build()
        responseBuilder = Response.Builder()
                .message("")
                .protocol(Protocol.HTTP_1_0)
                .code(200)
        store.onLogout()
    }

    @Test
    @Throws(IOException::class)
    fun testEmptyAuthToken() {
        val interceptor = AuthInterceptor(store)

        val resp = interceptor.intercept(object : Interceptor.Chain {
            override fun writeTimeoutMillis(): Int {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun call(): Call {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun withWriteTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun connectTimeoutMillis(): Int {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun withConnectTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun withReadTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun readTimeoutMillis(): Int {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun request(): Request? {
                return samplerequest
            }

            @Throws(IOException::class)
            override fun proceed(request: Request): Response {
                return responseBuilder!!
                        .request(request)
                        .build()
            }

            override fun connection(): Connection? {
                return null
            }
        })
        assertNull(resp.request()
                .header("Authorization"))
    }

    @Test
    @Throws(IOException::class)
    fun testNonEmptyAuthToken() {
        val interceptor = AuthInterceptor(store!!)
        store!!.saveOauthCredentials("abc", "def")
        val resp = interceptor.intercept(object : Interceptor.Chain {
            override fun request(): Request? {
                return samplerequest
            }

            @Throws(IOException::class)
            override fun proceed(request: Request): Response {
                return responseBuilder
                        .request(request)
                        .build()
            }

            override fun connection(): Connection? {
                return null
            }

            override fun writeTimeoutMillis(): Int = 0

            override fun call(): Call {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun withWriteTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain = this

            override fun connectTimeoutMillis(): Int = 0

            override fun withConnectTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain = this

            override fun withReadTimeout(timeout: Int, unit: TimeUnit?): Interceptor.Chain = this

            override fun readTimeoutMillis(): Int = 0
        })
        assertTrue(resp.request()
                .header("Authorization")!!.contains("abc"))
    }
}