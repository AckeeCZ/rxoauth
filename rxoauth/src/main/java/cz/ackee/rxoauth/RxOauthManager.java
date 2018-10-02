package cz.ackee.rxoauth;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.concurrent.Callable;

import io.reactivex.Completable;
import io.reactivex.CompletableSource;
import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.SingleTransformer;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import okhttp3.Interceptor;

/**
 * Rx managing of Oauth2 logic
 */
public class RxOauthManager {
    public static final String TAG = RxOauthManager.class.getName();
    private final OAuthStore oAuthStore;
    private final IAuthService authService;
    private final IOauthEventListener eventListener;
    private final RefreshTokenFailedListener refreshTokenFailedListener;
    private Observable<ICredentialsModel> refreshTokenObservable;
    private ErrorChecker errorChecker = new DefaultErrorChecker();

    public RxOauthManager(SharedPreferences sp, IAuthService apiInteractor, IOauthEventListener eventListener) {
        this.oAuthStore = new OAuthStore(sp);
        this.authService = apiInteractor;
        this.eventListener = eventListener;
        this.refreshTokenFailedListener = null;
        initRefreshTokenObservable();
    }

    public RxOauthManager(Context ctx, IAuthService apiInteractor, IOauthEventListener eventListener) {
        this.oAuthStore = new OAuthStore(ctx);
        this.authService = apiInteractor;
        this.eventListener = eventListener;
        this.refreshTokenFailedListener = null;
        initRefreshTokenObservable();
    }

    public RxOauthManager(OAuthStore store, IAuthService apiInteractor, IOauthEventListener eventListener) {
        this.oAuthStore = store;
        this.authService = apiInteractor;
        this.eventListener = eventListener;
        this.refreshTokenFailedListener = null;
        initRefreshTokenObservable();
    }

    public RxOauthManager(SharedPreferences sp, IAuthService apiInteractor, RefreshTokenFailedListener eventListener) {
        this.oAuthStore = new OAuthStore(sp);
        this.authService = apiInteractor;
        this.eventListener = null;
        this.refreshTokenFailedListener = eventListener;
        initRefreshTokenObservable();
    }

    public RxOauthManager(Context ctx, IAuthService apiInteractor, RefreshTokenFailedListener eventListener) {
        this.oAuthStore = new OAuthStore(ctx);
        this.authService = apiInteractor;
        this.eventListener = null;
        this.refreshTokenFailedListener = eventListener;
        initRefreshTokenObservable();
    }

    public RxOauthManager(OAuthStore store, IAuthService apiInteractor, RefreshTokenFailedListener eventListener) {
        this.oAuthStore = store;
        this.authService = apiInteractor;
        this.eventListener = null;
        this.refreshTokenFailedListener = eventListener;
        initRefreshTokenObservable();
    }

    public void setErrorChecker(ErrorChecker errorChecker) {
        this.errorChecker = errorChecker;
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
    public <T> ObservableTransformer<T, T> wrapWithOAuthHandlingObservable() {
        return new ObservableTransformer<T, T>() {
            @Override
            public ObservableSource<T> apply(final Observable<T> upstream) {
                return upstream.onErrorResumeNext(new Function<Throwable, Observable<? extends T>>() {
                    @Override
                    public Observable<? extends T> apply(final Throwable throwable) throws Exception {
                        if (errorChecker.isExpiredAccessToken(throwable)) {
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

    /**
     * Wrap upstream Single with oauth refresh access token handling
     *
     * @param <T>
     * @return
     */
    public <T> SingleTransformer<T, T> wrapWithOAuthHandlingSingle() {
        return new SingleTransformer<T, T>() {
            @Override
            public SingleSource<T> apply(final Single<T> upstream) {
                return upstream
                        .onErrorResumeNext(new Function<Throwable, SingleSource<? extends T>>() {
                            @Override
                            public SingleSource<? extends T> apply(final Throwable throwable) throws Exception {
                                if (errorChecker.isExpiredAccessToken(throwable)) {
                                    return refreshTokenObservable
                                            .flatMapSingle(new Function<ICredentialsModel, Single<T>>() {
                                                @Override
                                                public Single<T> apply(ICredentialsModel iCredentialsModel) throws Exception {
                                                    return upstream;
                                                }
                                            }).firstOrError();
                                }
                                return Single.error(throwable);
                            }
                        });
            }
        };
    }

    /**
     * Wrap upstream Completable with oauth refresh access token handling
     *
     * @return
     */
    public CompletableTransformer wrapWithOAuthHandlingCompletable() {
        return new CompletableTransformer() {
            @Override
            public CompletableSource apply(final Completable upstream) {
                return upstream
                        .onErrorResumeNext(new Function<Throwable, CompletableSource>() {
                            @Override
                            public CompletableSource apply(final Throwable throwable) throws Exception {
                                if (errorChecker.isExpiredAccessToken(throwable)) {
                                    return refreshTokenObservable
                                            .flatMapCompletable(new Function<ICredentialsModel, Completable>() {
                                                @Override
                                                public Completable apply(ICredentialsModel iCredentialsModel) throws Exception {
                                                    return upstream;
                                                }
                                            });
                                }
                                return Completable.error(throwable);
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
                        if (errorChecker.isBadRefreshToken(throwable)) {
                            oAuthStore.onLogout();
                            if (eventListener != null) {
                                eventListener.onRefreshTokenFailed();
                            }
                            if (refreshTokenFailedListener != null) {
                                refreshTokenFailedListener.onRefreshTokenFailed(throwable);
                            }
                        }
                    }
                });
    }

    public void storeCredentials(ICredentialsModel credentialsModel) {
        this.oAuthStore.saveOauthCredentials(credentialsModel);
    }
}
