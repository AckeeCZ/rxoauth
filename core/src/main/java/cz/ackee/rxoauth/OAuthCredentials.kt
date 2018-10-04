package cz.ackee.rxoauth

/**
 * Model for OAuth credentials.
 */
interface OAuthCredentials {

    val accessToken: String
    val refreshToken: String
    val expiresIn: Long?
}
