package cz.ackee.sample.model.rest;

import java.util.List;

import cz.ackee.rxoauth.ICredentialsModel;
import cz.ackee.rxoauth.annotations.NoOauth;
import cz.ackee.rxoauth.annotations.OauthService;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import rx.Observable;

/**
 * Api description simulating Retrofit description
 * <p>
 * Created by David Bilik[david.bilik@ackee.cz] on {06/08/16}
 **/
@OauthService
public interface ApiDescription {

    @NoOauth
    public Observable<LoginResponse> login(String name, String passwd);

    public Observable<List<SampleItem>> getData();

    @NoOauth
    Observable<ICredentialsModel> refreshAccessToken(String refreshToken);
}
