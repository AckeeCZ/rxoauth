package cz.ackee.sample.interactor;


import java.util.List;

import cz.ackee.rxoauth.IAuthService;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;

/**
 * Interactor for communicating with API
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public interface IApiInteractor extends IAuthService {
    Observable<LoginResponse> login(String name, String password);

    Observable<List<SampleItem>> getData();

    Completable logout();

    Single<String> something();
}
