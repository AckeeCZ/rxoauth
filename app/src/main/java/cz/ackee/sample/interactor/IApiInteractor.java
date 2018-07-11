package cz.ackee.sample.interactor;


import java.util.List;

import cz.ackee.rxoauth.RefreshTokenService;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import io.reactivex.Completable;
import io.reactivex.Single;

/**
 * Interactor for communicating with API
 */
public interface IApiInteractor extends RefreshTokenService {
    Single<LoginResponse> login(String name, String password);

    Single<List<SampleItem>> getData();

    Completable logout();
}
