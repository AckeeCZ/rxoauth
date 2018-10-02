package cz.ackee.rxoauth;

/**
 * Listener that refresh of token failed
 */
public interface RefreshTokenFailedListener {
    void onRefreshTokenFailed(Throwable error);
}
