package cz.ackee.rxoauth

import android.content.Context
import androidx.test.InstrumentationRegistry.getTargetContext
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.JUnit4

/**
 * Test for oauth store
 */
@RunWith(JUnit4::class)
class OAuthStoreTest {

    private val accessToken = "abc"
    private val refreshToken = "def"
    private val expiresIn = 3600L
    private val toleranceInterval = 5L

    @Before
    fun setup() {
        cleanStore()
    }

    @After
    fun clean() {
        cleanStore()
    }

    @Test
    @Throws(Exception::class)
    fun testSaveOauthCredentialsContextConstructor() {
        val store = OAuthStore(getTargetContext())
        store.saveCredentials(DefaultOAuthCredentials(accessToken, refreshToken, expiresIn))
        assertEquals(accessToken, store.accessToken)
        assertEquals(refreshToken, store.refreshToken)
    }

    @Test
    @Throws(Exception::class)
    fun testSaveOauthCredentialsSpConstructor() {
        val sp = getTargetContext().getSharedPreferences(OAuthStore.DEFAULT_SP_NAME, Context.MODE_PRIVATE)
        val store = OAuthStore(sp)
        store.saveCredentials(DefaultOAuthCredentials(accessToken, refreshToken, expiresIn))
        assertEquals(accessToken, store.accessToken)
        assertEquals(refreshToken, store.refreshToken)
        val expiresAtApprox = System.currentTimeMillis() + expiresIn * 1000
        assertTrue(store.expiresAt in expiresAtApprox - toleranceInterval..expiresAtApprox + toleranceInterval)
    }

    @Test
    @Throws(Exception::class)
    fun testOnLogout() {
        val store = OAuthStore(getTargetContext())
        store.saveCredentials(DefaultOAuthCredentials(accessToken, refreshToken, expiresIn))
        store.clearCredentials()
        assertEquals(null, store.accessToken)
        assertEquals(null, store.refreshToken)
        assertEquals(null, store.expiresAt)
    }

    @Test
    @Throws(Exception::class)
    fun testKeysMigration() {
        val sp = getTargetContext().getSharedPreferences(OAuthStore.DEFAULT_SP_NAME, Context.MODE_PRIVATE)
        sp.edit().putString(OAuthStore.ACCESS_TOKEN_KEY_OLD, accessToken).putString(OAuthStore.REFRESH_TOKEN_KEY_OLD, refreshToken).apply()
        val store = OAuthStore(getTargetContext())
        assertEquals(accessToken, store.accessToken)
        assertEquals(refreshToken, store.refreshToken)
        assertEquals(null, sp.getString(OAuthStore.ACCESS_TOKEN_KEY_OLD, null))
        assertEquals(null, sp.getString(OAuthStore.REFRESH_TOKEN_KEY_OLD, null))
    }

    private fun cleanStore() {
        OAuthStore(getTargetContext()).clearCredentials()
    }
}