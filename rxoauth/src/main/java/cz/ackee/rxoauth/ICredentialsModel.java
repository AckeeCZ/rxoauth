package cz.ackee.rxoauth;

/**
 * Model for credentials
 */
public interface ICredentialsModel {

    String getAccessToken();

    String getRefreshToken();
}
