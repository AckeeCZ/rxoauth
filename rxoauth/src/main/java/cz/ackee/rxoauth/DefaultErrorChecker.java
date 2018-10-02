package cz.ackee.rxoauth;

import java.net.HttpURLConnection;

import retrofit2.HttpException;

/**
 * Default error checker that checks for 401 for expired access token and 401 + 400 for bad refresh of token
 */
public class DefaultErrorChecker implements ErrorChecker {
    @Override
    public boolean isExpiredAccessToken(Throwable t) {
        if (t instanceof HttpException) {
            if (((HttpException) t).code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isBadRefreshToken(Throwable t) {
        if (t instanceof HttpException) {
            return ((HttpException) t).code() == HttpURLConnection.HTTP_BAD_REQUEST || ((HttpException) t).code() == HttpURLConnection.HTTP_UNAUTHORIZED;
        }
        return false;
    }
}
