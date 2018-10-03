package cz.ackee.rxoauth

import io.reactivex.Single

/**
 * Service that should provide credentials when refresh token request is required.
 */
interface RefreshTokenService {

    fun refreshAccessToken(refreshToken: String?): Single<OauthCredentials>
}
