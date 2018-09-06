package cz.ackee.rxoauth;

/**
 * Listener with events regarding Oauth2 tokens lifecycle
 *
 * @deprecated Use {@link RefreshTokenFailedListener} instead
 */
public interface IOauthEventListener {

    public void onRefreshTokenFailed();
}
