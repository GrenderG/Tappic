package es.dmoral.tappic;

import android.app.Application;
import android.content.Context;

/**
 * Created by grender on 27/04/16.
 */
public class TappicApp extends Application {

    private static Application tappicApp;

    @Override
    public void onCreate() {
        super.onCreate();
        TappicApp.tappicApp = this;
    }

    public static Context getAppContext() {
        return TappicApp.tappicApp.getApplicationContext();
    }

}
