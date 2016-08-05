package cz.ackee.sample;

import android.app.Application;

/**
 * Application class
 * Created by David Bilik[david.bilik@ackee.cz] on {05/08/16}
 **/
public class App extends Application {
    public static final String TAG = App.class.getName();
    public static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static App getInstance() {
        return sInstance;
    }
}
