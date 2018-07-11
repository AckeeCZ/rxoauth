package cz.ackee.sample

import android.app.Application

/**
 * Application class
 */
class App : Application() {

    companion object {
        lateinit var instance: App
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
