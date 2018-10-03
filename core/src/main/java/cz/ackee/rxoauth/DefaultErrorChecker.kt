package cz.ackee.rxoauth

import retrofit2.HttpException
import java.net.HttpURLConnection

/**
 * Default error checker that checks for 401 for expired access token and 401 + 400 for bad refresh of token
 */
class DefaultErrorChecker : ErrorChecker {

    override fun isExpiredAccessToken(t: Throwable): Boolean {
        if (t is HttpException) {
            if (t.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return true
            }
        }
        return false
    }

    override fun isBadRefreshToken(t: Throwable): Boolean {
        if (t is HttpException) {
            if (t.code() == HttpURLConnection.HTTP_BAD_REQUEST || t.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return true
            }
        }
        return false
    }
}
