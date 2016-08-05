package cz.ackee.sample.interactor;

import android.content.Intent;
import android.util.Log;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cz.ackee.rxoauth.ICredentialsModel;
import cz.ackee.rxoauth.RxOauthManaging;
import cz.ackee.sample.App;
import cz.ackee.sample.login.MainActivity;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.Response;
import retrofit2.adapter.rxjava.HttpException;
import rx.Observable;

/**
 * Implementation of api
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class ApiInteractorImpl implements IApiInteractor {
    public static final String TAG = ApiInteractorImpl.class.getName();
    private final RxOauthManaging rxOauth;

    public ApiInteractorImpl() {
        this.rxOauth = new RxOauthManaging(App.getInstance(), this, () -> {
            Log.d(TAG, "ApiInteractorImpl: should logout");
            App.getInstance().startActivity(new Intent(App.getInstance(), MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        });
    }

    @Override
    public Observable<LoginResponse> login(String name, String password) {
        return Observable.just(new LoginResponse("David", "123", "456"))
                .doOnNext(this.rxOauth::storeCredentials);
    }

    @Override
    public Observable<List<SampleItem>> getData() {
        final boolean[] first = {true};
        return Observable.just(Arrays.asList(new SampleItem("a"), new SampleItem("b"), new SampleItem("c")))

                .flatMap(list -> {
                    if (first[0] && new Random().nextInt() % 2 == 0) {
                        first[0] = false;
                        //simulate 401 response
                        return Observable.error(new HttpException(Response.error(401, new ResponseBody() {
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
                        })));
                    } else {
                        return Observable.just(list);
                    }
                })
                .compose(rxOauth.wrapWithOAuthHandling());
    }

    @Override
    public Observable<ICredentialsModel> refreshAccessToken(String refreshToken) {
        final boolean[] first = {true};
        return Observable.just(new LoginResponse("David", "123", "456"))
                .flatMap(data -> {
                    if (first[0] && new Random().nextInt() % 2 == 0) {
                        first[0] = false;
                        //simulate 400 response
                        return Observable.error(new HttpException(Response.error(400, new ResponseBody() {
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
                        })));
                    } else {
                        return Observable.just(data);
                    }
                })
                .doOnNext(rxOauth::storeCredentials)
                .map(resp -> resp);
    }
}
