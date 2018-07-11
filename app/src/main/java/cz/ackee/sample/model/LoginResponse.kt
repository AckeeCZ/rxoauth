package cz.ackee.sample.model

import cz.ackee.rxoauth.OauthCredentials

/**
 * Response for login
 */
data class LoginResponse(var name: String, override var accessToken: String, override var refreshToken: String) : OauthCredentials