package wael.ouni.apps.androidcodingbestpractices;

import android.app.Application;

import timber.log.Timber;

/**
 * Created by wael on 10/03/18.
 */

public class ACBPApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
