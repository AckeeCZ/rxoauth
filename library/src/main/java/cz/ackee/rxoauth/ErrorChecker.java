package cz.ackee.rxoauth;

/**
 * Interface for checking if given request error is expired token or invalid refresh token
 *
 * @author David Bilik [david.bilik@ackee.cz]
 * @since 02/12/2017
 **/
public interface ErrorChecker {
    boolean isExpiredAccessToken(Throwable t);

    boolean isBadRefreshToken(Throwable t);
}
