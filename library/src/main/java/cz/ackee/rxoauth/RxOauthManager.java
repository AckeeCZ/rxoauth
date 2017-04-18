package cz.ackee.rxoauth;

import android.content.Context;
import android.content.SharedPreferences;

import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.Interceptor;
import retrofit2.HttpException;

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
        refreshTokenObservable = Observable.defer(new Callable<Observable<ICredentialsModel>>() {
            @Override
            public Observable<ICredentialsModel> call() {
                return refreshAccessToken();
            }
        })
                .publish()
                .refCount()
                .doOnComplete(new Action() {
                    @Override
                    public void run() throws Exception {
                        initRefreshTokenObservable();
                    }

                });
    }

    /**
     * Wrap upstream observable with oauth refresh access token handling
     *
     * @param <T>
     * @return
     */
    public <T> ObservableTransformer<T, T> wrapWithOAuthHandling() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(final Observable<T> upstream) {
                return upstream.onErrorResumeNext(new Function<Throwable, Observable<? extends T>>() {
                    @Override
                    public Observable<? extends T> apply(final Throwable throwable) throws Exception {
                        if (isUnAuthorizedError(throwable)) {
                            Logger.d("Access token expired");
                            return refreshTokenObservable
                                    .flatMap(new Function<ICredentialsModel, Observable<T>>() {
                                        @Override
                                        public Observable<T> apply(ICredentialsModel iCredentialsModel) throws Exception {
                                            return upstream;
                                        }
                                    });
                        }
                        return Observable.error(throwable);
                    }
                });
            }
        };
    }

    private Observable<ICredentialsModel> refreshAccessToken() {
        String refreshToken = oAuthStore.getRefreshToken();
        return authService.refreshAccessToken(refreshToken)
                .doOnNext(new Consumer<ICredentialsModel>() {
                    @Override
                    public void accept(ICredentialsModel iCredentialsModel) {
                        oAuthStore.saveOauthCredentials(iCredentialsModel);
                    }
                })
                .doOnError(new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (isBadRequestError(throwable)) {
                            Logger.d("Refresh token expired");
                            oAuthStore.onLogout();
                            eventListener.onRefreshTokenFailed();
                        }
                    }
                });
    }

    private boolean isUnAuthorizedError(Throwable error) {
        if (error instanceof HttpException) {
            if (((HttpException) error).code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return true;
            }
        }
        return false;
    }

    private boolean isBadRequestError(Throwable error) {
        if (error instanceof HttpException) {
            if (((HttpException) error).code() == HttpURLConnection.HTTP_BAD_REQUEST || ((HttpException) error).code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return true;
            }
        }
        return false;
    }

    public void storeCredentials(ICredentialsModel credentialsModel) {
        this.oAuthStore.saveOauthCredentials(credentialsModel);
    }
}
