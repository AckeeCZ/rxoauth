package cz.ackee.sample.model;

import cz.ackee.rxoauth.ICredentialsModel;

/**
 * Response for login
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class LoginResponse implements ICredentialsModel{
    public static final String TAG = LoginResponse.class.getName();

    String name;

    String accessToken;
    String refreshToken;

    public LoginResponse(String name, String accessToken, String refreshToken) {
        this.name = name;
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
    }

    @Override
    public String getAccessToken() {
        return null;
    }

    @Override
    public String getRefreshToken() {
        return null;
    }
}
