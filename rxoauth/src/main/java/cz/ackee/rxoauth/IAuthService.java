package cz.ackee.rxoauth;


import io.reactivex.Observable;

/**
 * Service that performs requests to server regarding oauth
 */
public interface IAuthService {
    Observable<ICredentialsModel> refreshAccessToken(String refreshToken);
}
