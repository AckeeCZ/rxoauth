package cz.ackee.sample.model.rest;

import java.util.List;

import cz.ackee.rxoauth.ICredentialsModel;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import cz.ackee.wrapper.annotations.NoCompose;
import cz.ackee.wrapper.annotations.WrappedService;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Api description simulating Retrofit description
 * <p>
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
@WrappedService
public interface ApiDescription {

    @NoCompose
    Observable<LoginResponse> login(String name, String passwd);

    Observable<List<SampleItem>> getData();

    @NoCompose
    Observable<ICredentialsModel> refreshAccessToken(String refreshToken);

    Completable logout();

    Single<String> something();
}
