package cz.ackee.rxoauth;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.net.HttpURLConnection;

import okhttp3.Interceptor;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Rx managing of Oauth2 logic
 * Created by David Bilik[david.bilik@ackee.cz] on {23/03/16}
 **/
public class RxOauthManager {
    public static final String TAG = RxOauthManager.class.getName();
    private final OAuthStore oAuthStore;
    private final IAuthService authService;
    private final IOauthEventListener eventListener;
    private Observable<ICredentialsModel> refreshTokenObservable;

    public RxOauthManager(SharedPreferences sp, IAuthService apiInteractor, IOauthEventListener eventListener) {
        this.oAuthStore = new OAuthStore(sp);
        this.authService = apiInteractor;
        this.eventListener = eventListener;
        initRefreshTokenObservable();
    }

    public RxOauthManager(Context ctx, IAuthService apiInteractor, IOauthEventListener eventListener) {
        this.oAuthStore = new OAuthStore(ctx);
        this.authService = apiInteractor;
        this.eventListener = eventListener;
        initRefreshTokenObservable();
    }
    public RxOauthManager(OAuthStore store, IAuthService apiInteractor, IOauthEventListener eventListener) {
        this.oAuthStore = store;
        this.authService = apiInteractor;
        this.eventListener = eventListener;
        initRefreshTokenObservable();
    }

    public Interceptor getAuthInterceptor() {
        return new AuthInterceptor(this.oAuthStore);
    }

    private void initRefreshTokenObservable() {
        refreshTokenObservable = Observable.defer(new Func0<Observable<ICredentialsModel>>() {
            @Override
            public Observable<ICredentialsModel> call() {
                return refreshAccessToken();
            }
        })
                .publish()
                .refCount()
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        initRefreshTokenObservable();
                    }
                });
    }

    public <T> Observable.Transformer<T, T> wrapWithOAuthHandling() {
        return new Observable.Transformer<T, T>() {
            @Override
            public Observable<T> call(final Observable<T> observable) {
                Log.d(TAG, "call: ");
                return observable.onErrorResumeNext(new Func1<Throwable, Observable<? extends T>>() {
                    @Override
                    public Observable<? extends T> call(Throwable error) {
                        if (isUnAuthorizedError(error)) {
                            Logger.d("Access token expired");
                            return refreshTokenObservable
                                    .flatMap(new Func1<ICredentialsModel, Observable<T>>() {
                                        @Override
                                        public Observable<T> call(ICredentialsModel iCredentialsModel) {
                                            return observable;
                                        }
                                    });
                        }
                        return Observable.error(error);
                    }
                });
            }
        };
    }

    private Observable<ICredentialsModel> refreshAccessToken() {
        String refreshToken = oAuthStore.getRefreshToken();
        return authService.refreshAccessToken(refreshToken)
                .doOnNext(new Action1<ICredentialsModel>() {
                    @Override
                    public void call(ICredentialsModel iCredentialsModel) {
                        oAuthStore.saveOauthCredentials(iCredentialsModel);
                    }
                })
                .doOnError(new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if (isBadRequestError(throwable)) {
                            Logger.d("Refresh token expired");
                            oAuthStore.onLogout();
                            eventListener.onRefreshTokenFailed();
                        }
                    }
                });
    }

    private boolean isUnAuthorizedError(Throwable error) {
        Log.e(TAG, "isUnAuthorizedError: ",error );
        if (error instanceof HttpException) {
            Log.d(TAG, "isUnAuthorizedError: in instance of");
            if (((HttpException) error).code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                Log.d(TAG, "isUnAuthorizedError: code 401");
                return true;
            }
        }
        return false;
    }

    private boolean isBadRequestError(Throwable error) {
        if (error instanceof HttpException) {
            if (((HttpException) error).code() == HttpURLConnection.HTTP_BAD_REQUEST || ((HttpException) error).code() == HttpURLConnection.HTTP_UNAUTHORIZED ) {
                return true;
            }
        }
        return false;
    }

    public void storeCredentials(ICredentialsModel credentialsModel) {
        this.oAuthStore.saveOauthCredentials(credentialsModel);
    }
}
