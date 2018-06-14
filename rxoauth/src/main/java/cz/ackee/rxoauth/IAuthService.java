package cz.ackee.rxoauth;

import rx.Observable;

/**
 * Service that performs requests to server regarding oauth
 */
public interface IAuthService {
    Observable<ICredentialsModel> refreshAccessToken(String refreshToken);
}
