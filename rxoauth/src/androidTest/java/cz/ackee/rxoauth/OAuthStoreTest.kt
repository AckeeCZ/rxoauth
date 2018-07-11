package cz.ackee.rxoauth

import android.content.Context
import android.support.test.InstrumentationRegistry.getTargetContext
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Test for oauth store
 */
@RunWith(JUnit4::class)
class OAuthStoreTest {

    @Before
    fun clean() {
        var store = OAuthStore(getTargetContext())
        store.onLogout()
        store = OAuthStore(getTargetContext().getSharedPreferences("sp", Context.MODE_PRIVATE))
        store.onLogout()
    }

    @Throws(Exception::class)
    fun testSaveOauthCredentialsContextConstructor() {
        val store = OAuthStore(getTargetContext())
        store.saveOauthCredentials("a", "b")
        assertEquals(store.accessToken, "a")
    }

    @Test
    @Throws(Exception::class)
    fun testGetAccessToken() {
        val store = OAuthStore(getTargetContext())
        store.saveOauthCredentials("abc", "def")
        assertEquals(store.accessToken, "abc")
    }

    @Test
    @Throws(Exception::class)
    fun testGetRefreshToken() {
        val store = OAuthStore(getTargetContext())
        store.saveOauthCredentials("abc", "def")
        assertEquals(store.refreshToken, "def")
    }

    @Test
    @Throws(Exception::class)
    fun testOnLogout() {
        val store = OAuthStore(getTargetContext())
        store.saveOauthCredentials("abc", "def")
        assertEquals(store.accessToken, "abc")
        store.onLogout()
        assertEquals(store.accessToken, null)
    }

    @Test
    @Throws(Exception::class)
    fun testSaveOauthCredentials1() {
        val sp = getTargetContext().getSharedPreferences("sp", Context.MODE_PRIVATE)
        assertNull(sp.getString("oath2_access_token", null))
        val store = OAuthStore(sp)
        store.saveOauthCredentials("a", "b")
        assertEquals(store.accessToken, sp.getString("oath2_access_token", null))
        // clean
    }
}