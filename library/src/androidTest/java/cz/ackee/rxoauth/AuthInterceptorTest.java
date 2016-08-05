package cz.ackee.rxoauth;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import okhttp3.Connection;
import okhttp3.Interceptor;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.Response;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;

/**
 * Test for auth okhttp3 interceptor
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
@RunWith(JUnit4.class)
public class AuthInterceptorTest {

    private static OAuthStore store;
    private static Request samplerequest;
    private static Response.Builder responseBuilder;

    @BeforeClass
    public static void init() {
        store = new OAuthStore(getTargetContext());
        //to make sure its new
        store.onLogout();
        samplerequest = new Request.Builder()
                .url("https://example.com")
                .build();
        responseBuilder = new Response.Builder()
                .protocol(Protocol.HTTP_1_0)
                .code(200);
    }

    @Before
    public  void clean() {
        store.onLogout();
    }

    @Test
    public void testEmptyAuthToken() throws IOException {
        AuthInterceptor interceptor = new AuthInterceptor(store);
        Response resp = interceptor.intercept(new Interceptor.Chain() {
            @Override
            public Request request() {
                return samplerequest;
            }

            @Override
            public Response proceed(Request request) throws IOException {
                return responseBuilder
                        .request(request)
                        .build();
            }

            @Override
            public Connection connection() {
                return null;
            }
        });
        assertNull(resp.request()
                .header("Authorization"));

    }

    @Test
    public void testNonEmptyAuthToken() throws IOException {
        AuthInterceptor interceptor = new AuthInterceptor(store);
        store.saveOauthCredentials("abc", "def");
        Response resp = interceptor.intercept(new Interceptor.Chain() {
            @Override
            public Request request() {
                return samplerequest;
            }

            @Override
            public Response proceed(Request request) throws IOException {
                return responseBuilder
                        .request(request)
                        .build();
            }

            @Override
            public Connection connection() {
                return null;
            }
        });
        assertTrue(resp.request()
                .header("Authorization").contains("abc"));
    }
}