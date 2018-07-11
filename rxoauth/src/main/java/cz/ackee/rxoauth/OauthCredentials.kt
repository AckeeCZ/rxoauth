package cz.ackee.rxoauth

/**
 * Model for credentials
 */
interface OauthCredentials {

    val accessToken: String
    val refreshToken: String
}
