package cz.ackee.rxoauth

/**
 * Default [OauthCredentials] implementation.
 */
data class DefaultOauthCredentials(
        override val accessToken: String,
        override val refreshToken: String,
        override val expiresIn: Long? = null
) : OauthCredentials