package cz.ackee.rxoauth;


import android.content.Context;
import android.content.SharedPreferences;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

/**
 * Test for oauth store
 */
@RunWith(JUnit4.class)
public class OAuthStoreTest {

    @Before
    public void clean() {
        OAuthStore store = new OAuthStore(getTargetContext());
        store.onLogout();
        store = new OAuthStore(getTargetContext().getSharedPreferences("sp", Context.MODE_PRIVATE));
        store.onLogout();
    }

    public void testSaveOauthCredentialsContextConstructor() throws Exception {
        OAuthStore store = new OAuthStore(getTargetContext());
        store.saveOauthCredentials("a", "b");
        assertEquals(store.getAccessToken(), "a");
    }

    @Test
    public void testGetAccessToken() throws Exception {
        OAuthStore store = new OAuthStore(getTargetContext());
        store.saveOauthCredentials("abc", "def");
        assertEquals(store.getAccessToken(), "abc");
    }

    @Test
    public void testGetRefreshToken() throws Exception {
        OAuthStore store = new OAuthStore(getTargetContext());
        store.saveOauthCredentials("abc", "def");
        assertEquals(store.getRefreshToken(), "def");
    }

    @Test
    public void testOnLogout() throws Exception {
        OAuthStore store = new OAuthStore(getTargetContext());
        store.saveOauthCredentials("abc", "def");
        assertEquals(store.getAccessToken(), "abc");
        store.onLogout();
        assertEquals(store.getAccessToken(), null);
    }

    @Test
    public void testSaveOauthCredentials1() throws Exception {
        SharedPreferences sp = getTargetContext().getSharedPreferences("sp", Context.MODE_PRIVATE);
        assertNull(sp.getString("oath2_access_token", null));
        OAuthStore store = new OAuthStore(sp);
        store.saveOauthCredentials("a", "b");
        assertEquals(store.getAccessToken(), sp.getString("oath2_access_token", null));
        // clean
    }
}