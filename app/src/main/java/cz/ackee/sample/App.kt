package cz.ackee.sample

import android.app.Application
import cz.ackee.sample.model.rest.Server

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
        Server(this)
    }
}
