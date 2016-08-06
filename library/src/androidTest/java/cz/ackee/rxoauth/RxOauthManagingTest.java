package cz.ackee.rxoauth;

import org.junit.Before;
import org.junit.Test;

import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Func1;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RxOauthManaging} class
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class RxOauthManagingTest {

    private boolean firstRun;

    @Before
    public void beforeTest() {
        firstRun = true;
    }

    @Test
    public void testSuccessFullRequest() {
        IAuthService authService = new IAuthService() {
            @Override
            public Observable<ICredentialsModel> refreshAccessToken(String refreshToken) {
                return null;
            }
        };

        IOauthEventListener eventListener = new IOauthEventListener() {
            @Override
            public void onRefreshTokenFailed() {

            }
        };
        RxOauthManaging managing = new RxOauthManaging(getTargetContext(), authService, eventListener);
        assertEquals(Observable.just("ok").compose(managing.<String>wrapWithOAuthHandling()).toBlocking().first(), "ok");
    }

    @Test
    public void testExpiredAccessToken() {
        final ICredentialsModel mockedCredentials = mock(ICredentialsModel.class);
        final HttpException unauthorizedException = new HttpException(retrofit2.Response.error(401, new ResponseBody() {
            @Override
            public MediaType contentType() {
                return null;
            }

            @Override
            public long contentLength() {
                return 0;
            }

            @Override
            public BufferedSource source() {
                return null;
            }
        }));

        when(mockedCredentials.getAccessToken()).thenReturn("abc");
        when(mockedCredentials.getRefreshToken()).thenReturn("def");
        IAuthService authService = new IAuthService() {
            @Override
            public Observable<ICredentialsModel> refreshAccessToken(String refreshToken) {
                return Observable.just(mockedCredentials);
            }
        };

        IOauthEventListener eventListener = new IOauthEventListener() {
            @Override
            public void onRefreshTokenFailed() {

            }
        };
        RxOauthManaging managing = new RxOauthManaging(getTargetContext(), authService, eventListener);
        Observable<Object> badObservable = Observable.just("ok")
                .flatMap(new Func1<String, Observable<?>>() {
                    @Override
                    public Observable<String> call(String s) {
                        if (firstRun) {
                            firstRun = false;
                            return Observable.error(unauthorizedException);
                        }
                        return Observable.just(s);
                    }
                })
                .compose(managing.wrapWithOAuthHandling());
        assertEquals(badObservable.toBlocking().first(), "ok");
    }

    @Test
    public void testExpiredRefreshToken() {
        final ICredentialsModel mockedCredentials = mock(ICredentialsModel.class);
        final HttpException unauthorizedException = new HttpException(retrofit2.Response.error(401, mock(ResponseBody.class)));
        final HttpException badRequestException = new HttpException(retrofit2.Response.error(400, mock(ResponseBody.class)));
        final IOauthEventListener eventListener = mock(IOauthEventListener.class);
        when(mockedCredentials.getAccessToken()).thenReturn("abc");
        when(mockedCredentials.getRefreshToken()).thenReturn("def");

        IAuthService authService = new IAuthService() {
            @Override
            public Observable<ICredentialsModel> refreshAccessToken(String refreshToken) {
                return Observable.error(badRequestException);
            }
        };

        RxOauthManaging managing = new RxOauthManaging(getTargetContext(), authService, eventListener);
        Observable<Object> badObservable = Observable.just("ok")
                .flatMap(new Func1<String, Observable<String>>() {
                    @Override
                    public Observable<String> call(String s) {
                        if (firstRun) {
                            firstRun = false;
                            return Observable.error(unauthorizedException);
                        }
                        return Observable.just(s);
                    }
                })
                .compose(managing.wrapWithOAuthHandling());
        try {
            badObservable.toBlocking().first();
            assertTrue("Couldnt be here, should failed", false);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            assertTrue(ex.getCause() instanceof HttpException);
            assertEquals(((HttpException) ex.getCause()).code(), 400);
        }
        verify(eventListener).onRefreshTokenFailed();
    }
}