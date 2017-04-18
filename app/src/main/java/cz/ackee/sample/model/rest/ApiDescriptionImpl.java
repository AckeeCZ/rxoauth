package cz.ackee.sample.model.rest;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import cz.ackee.rxoauth.ICredentialsModel;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import io.reactivex.Observable;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import retrofit2.HttpException;
import retrofit2.Response;

/**
 * TODO add class description
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
public class ApiDescriptionImpl implements ApiDescription {
    public static final String TAG = ApiDescriptionImpl.class.getName();

    @Override
    public Observable<LoginResponse> login(String name, String passwd) {
        return Observable.just(new LoginResponse("David", "123", "456"));
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
                });
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
                .map(resp -> resp);
    }
}
