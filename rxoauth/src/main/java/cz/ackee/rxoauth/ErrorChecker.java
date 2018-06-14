package cz.ackee.rxoauth;

/**
 * Interface for checking if given request error is expired token or invalid refresh token
 */
public interface ErrorChecker {
    boolean isExpiredAccessToken(Throwable t);

    boolean isBadRefreshToken(Throwable t);
}
