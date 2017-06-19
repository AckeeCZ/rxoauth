package cz.ackee.sample.interactor;

import android.content.Intent;
import android.util.Log;

import java.util.List;

import cz.ackee.rxoauth.ICredentialsModel;
import cz.ackee.rxoauth.RxOauthManager;
import cz.ackee.wrapper.annotations.IComposeWrapper;
import cz.ackee.sample.App;
import cz.ackee.sample.login.MainActivity;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import cz.ackee.sample.model.rest.ApiDescription;
import cz.ackee.sample.model.rest.ApiDescriptionImpl;
import cz.ackee.sample.model.rest.ApiDescriptionWrapped;
import io.reactivex.Completable;
import io.reactivex.CompletableTransformer;
import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;

/**
 * Implementation of api
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class ApiInteractorImpl implements IApiInteractor {
    public static final String TAG = ApiInteractorImpl.class.getName();
    private final RxOauthManager rxOauth;
    private final ApiDescription apiDescription;
    private final ApiDescriptionWrapped apiWrapper;

    public ApiInteractorImpl() {
        this.rxOauth = new RxOauthManager(App.getInstance(), this, () -> {
            Log.d(TAG, "ApiInteractorImpl: should logout");
            App.getInstance().startActivity(new Intent(App.getInstance(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
        IComposeWrapper wrapper = new IComposeWrapper() {
            @Override
            public <T> ObservableTransformer<T, T> wrapObservable() {
                return rxOauth.wrapWithOAuthHandlingObservable();
            }

            @Override
            public <T> SingleTransformer<T, T> wrapSingle() {
                return rxOauth.wrapWithOAuthHandlingSingle();
            }

            @Override
            public CompletableTransformer wrapCompletable() {
                return rxOauth.wrapWithOAuthHandlingCompletable();
            }
        };
        apiDescription = new ApiDescriptionImpl();
        this.apiWrapper = new ApiDescriptionWrapped(apiDescription, wrapper);
    }

    @Override
    public Observable<LoginResponse> login(String name, String password) {
        return apiWrapper.login(name, password)
                .doOnNext(this.rxOauth::storeCredentials);
    }

    @Override
    public Observable<List<SampleItem>> getData() {
        return apiWrapper.getData();
    }

    @Override
    public Observable<ICredentialsModel> refreshAccessToken(String refreshToken) {
        return apiWrapper.refreshAccessToken(refreshToken);
    }

    @Override
    public Single<String> something() {
        return apiWrapper.something();
    }

    @Override
    public Completable logout() {
        return apiWrapper.logout();
    }
}
