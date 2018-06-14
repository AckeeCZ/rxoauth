package cz.ackee.rxoauth;

/**
 * Model for credentials
 */
public interface ICredentialsModel {

    public String getAccessToken();

    public String getRefreshToken();
}
