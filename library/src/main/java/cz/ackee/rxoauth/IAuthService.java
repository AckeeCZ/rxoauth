package cz.ackee.rxoauth;

import rx.Observable;

/**
 * Service that performs requests to server regarding oauth
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public interface IAuthService {
    Observable<ICredentialsModel> refreshAccessToken(String refreshToken);
}
