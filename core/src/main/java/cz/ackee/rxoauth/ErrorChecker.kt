package cz.ackee.rxoauth

/**
 * Interface for checking if given request error is expired token or invalid refresh token
 */
interface ErrorChecker {

    fun isExpiredAccessToken(t: Throwable): Boolean

    fun isBadRefreshToken(t: Throwable): Boolean
}
