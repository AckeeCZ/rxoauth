package cz.ackee.rxoauth;

import org.junit.Before;
import org.junit.Test;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.HttpException;

import static android.support.test.InstrumentationRegistry.getTargetContext;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RxOauthManager} class
=*/
public class RxOauthManagerTest {

    private boolean firstRun;

    @Before
    public void beforeTest() {
        firstRun = true;
    }

    @Test
    public void testSuccessFullRequest() {
        RefreshTokenService authService = new RefreshTokenService() {
            @Override
            public Observable<OauthCredentials> refreshAccessToken(String refreshToken) {
                return null;
            }
        };

        RefreshTokenFailListener eventListener = new RefreshTokenFailListener() {
            @Override
            public void onRefreshTokenFailed() {

            }
        };

        RxOauthManager managing = new RxOauthManager(getTargetContext(), authService, eventListener);
        assertEquals(Observable.just("ok").compose(managing.<String>wrapWithOAuthHandlingObservable()).blockingFirst(), "ok");
    }

    @Test
    public void testExpiredAccessToken() {
        final OauthCredentials mockedCredentials = mock(OauthCredentials.class);
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
        RefreshTokenService authService = new RefreshTokenService() {
            @Override
            public Observable<OauthCredentials> refreshAccessToken(String refreshToken) {
                return Observable.just(mockedCredentials);
            }
        };

        RefreshTokenFailListener eventListener = new RefreshTokenFailListener() {
            @Override
            public void onRefreshTokenFailed() {

            }
        };
        RxOauthManager managing = new RxOauthManager(getTargetContext(), authService, eventListener);
        Observable<Object> badObservable = Observable.just("ok")
                .flatMap(new Function<String, ObservableSource<?>>() {
                    @Override
                    public Observable<String> apply(String s) {
                        if (firstRun) {
                            firstRun = false;
                            return Observable.error(unauthorizedException);
                        }
                        return Observable.just(s);
                    }
                })
                .compose(managing.wrapWithOAuthHandlingObservable());
        assertEquals(badObservable.blockingFirst(), "ok");
    }


    @Test
    public void testExpiredAccessTokenSingle() {
        final OauthCredentials mockedCredentials = mock(OauthCredentials.class);
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
        RefreshTokenService authService = new RefreshTokenService() {
            @Override
            public Observable<OauthCredentials> refreshAccessToken(String refreshToken) {
                return Observable.just(mockedCredentials);
            }
        };

        RefreshTokenFailListener eventListener = new RefreshTokenFailListener() {
            @Override
            public void onRefreshTokenFailed() {

            }
        };
        RxOauthManager managing = new RxOauthManager(getTargetContext(), authService, eventListener);
        Single<Object> badObservable = Single.just("ok")
                .flatMap(new Function<String, SingleSource<?>>() {
                    @Override
                    public Single<String> apply(String s) {
                        if (firstRun) {
                            firstRun = false;
                            return Single.error(unauthorizedException);
                        }
                        return Single.just(s);
                    }
                })
                .compose(managing.wrapWithOAuthHandlingSingle());
        assertEquals(badObservable.blockingGet(), "ok");
    }


    @Test
    public void testExpiredAccessTokenCompletable() {
        final OauthCredentials mockedCredentials = mock(OauthCredentials.class);
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
        RefreshTokenService authService = new RefreshTokenService() {
            @Override
            public Observable<OauthCredentials> refreshAccessToken(String refreshToken) {
                return Observable.just(mockedCredentials);
            }
        };

        RefreshTokenFailListener eventListener = new RefreshTokenFailListener() {
            @Override
            public void onRefreshTokenFailed() {

            }
        };
        RxOauthManager managing = new RxOauthManager(getTargetContext(), authService, eventListener);

        Observable.just("ok")
                .flatMapCompletable(new Function<String, CompletableSource>() {
                    @Override
                    public CompletableSource apply(String s) {
                        if (firstRun) {
                            firstRun = false;
                            return Completable.error(unauthorizedException);
                        }
                        return Completable.complete();
                    }
                })
                .compose(managing.wrapWithOAuthHandlingCompletable())
                .test()
                .assertNoErrors()
                .assertComplete();
    }

    @Test
    public void testExpiredRefreshToken() {
        final OauthCredentials mockedCredentials = mock(OauthCredentials.class);
        final HttpException unauthorizedException = new HttpException(retrofit2.Response.error(401, mock(ResponseBody.class)));
        final HttpException badRequestException = new HttpException(retrofit2.Response.error(400, mock(ResponseBody.class)));
        final RefreshTokenFailListener eventListener = mock(RefreshTokenFailListener.class);
        when(mockedCredentials.getAccessToken()).thenReturn("abc");
        when(mockedCredentials.getRefreshToken()).thenReturn("def");

        RefreshTokenService authService = new RefreshTokenService() {
            @Override
            public Observable<OauthCredentials> refreshAccessToken(String refreshToken) {
                return Observable.error(badRequestException);
            }
        };

        RxOauthManager managing = new RxOauthManager(getTargetContext(), authService, eventListener);
        Observable<Object> badObservable = Observable.just("ok")
                .flatMap(new Function<String, ObservableSource<?>>() {
                    @Override
                    public Observable<String> apply(String s) {
                        if (firstRun) {
                            firstRun = false;
                            return Observable.error(unauthorizedException);
                        }
                        return Observable.just(s);
                    }
                })
                .compose(managing.wrapWithOAuthHandlingObservable());
        try {
            badObservable.blockingFirst();
            assertTrue("Couldnt be here, should failed", false);
        } catch (RuntimeException ex) {
            ex.printStackTrace();
            assertTrue(ex.getCause() instanceof HttpException);
            assertEquals(((HttpException) ex.getCause()).code(), 400);
        }
        verify(eventListener).onRefreshTokenFailed();
    }
}