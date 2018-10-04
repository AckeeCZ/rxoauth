package cz.ackee.rxoauth

import androidx.test.InstrumentationRegistry.getTargetContext
import okhttp3.*
import org.junit.After
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
class OAuthInterceptorTest {

    private val store: OAuthStore = OAuthStore(getTargetContext())

    private val samplerequest = Request.Builder()
            .url("https://example.com")
            .build()
    private val responseBuilder = Response.Builder()
            .message("")
            .protocol(Protocol.HTTP_1_0)
            .code(200)

    @Before
    fun setup() {
        cleanStore()
    }

    @After
    fun clean() {
        cleanStore()
    }

    @Test
    @Throws(IOException::class)
    fun testEmptyAuthToken() {
        assertNull(interceptAuth(store, samplerequest, responseBuilder).request().header("Authorization"))
    }

    @Test
    @Throws(IOException::class)
    fun testNonEmptyAuthToken() {
        store.saveCredentials(DefaultOAuthCredentials("abc", "def"))
        assertTrue(interceptAuth(store, samplerequest, responseBuilder).request().header("Authorization")!!.contains("abc"))
    }

    private fun cleanStore() {
        OAuthStore(getTargetContext()).clearCredentials()
    }

    private fun interceptAuth(store: OAuthStore, request: Request, responseBuilder: Response.Builder): Response {
        return OAuthInterceptor(store).intercept(object : Interceptor.Chain {
            override fun request(): Request? {
                return request
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
    }
}