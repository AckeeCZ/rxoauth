package cz.ackee.sample.interactor;


import java.util.List;

import cz.ackee.wrapper.IAuthService;
import cz.ackee.sample.model.LoginResponse;
import cz.ackee.sample.model.SampleItem;
import rx.Observable;

/**
 * Interactor for communicating with API
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public interface IApiInteractor extends IAuthService {
    public Observable<LoginResponse> login(String name, String password);

    public Observable<List<SampleItem>> getData();
}
