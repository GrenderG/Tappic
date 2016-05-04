package es.dmoral.tappic;

import android.app.Application;
import android.content.Context;

import es.dmoral.tappic.helpers.PreferenceHelper;

/**
 * Created by grender on 27/04/16.
 */
public class TappicApp extends Application {

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceHelper.initializePrefs(getApplicationContext());
        TappicApp.appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return TappicApp.appContext;
    }

}
