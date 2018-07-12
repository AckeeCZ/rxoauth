package cz.ackee.sample

import android.app.Application
import cz.ackee.sample.di.DIContainer
import cz.ackee.sample.model.rest.Server

/**
 * Application class
 */
class App : Application() {

    companion object {
        lateinit var diContainer: DIContainer
    }

    override fun onCreate() {
        super.onCreate()
        Server(this)
        diContainer = DIContainer(this)
    }
}
