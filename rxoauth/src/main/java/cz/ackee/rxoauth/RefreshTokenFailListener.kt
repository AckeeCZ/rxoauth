package cz.ackee.rxoauth

/**
 * Listener that is notified when refresh of token fails.
 */
interface RefreshTokenFailListener {

    fun onRefreshTokenFailed()
}
