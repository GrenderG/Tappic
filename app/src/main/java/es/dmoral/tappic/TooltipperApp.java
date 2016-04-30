package es.dmoral.tappic;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;

import es.dmoral.tappic.helpers.PreferenceHelper;

/**
 * Created by grender on 27/04/16.
 */
public class TooltipperApp extends Application {

    private static Context appContext;
    private static Bitmap currentBitmap;

    @Override
    public void onCreate() {
        super.onCreate();
        PreferenceHelper.initializePrefs(getApplicationContext());
        TooltipperApp.appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return TooltipperApp.appContext;
    }

    public static Bitmap getCurrentBitmap() {
        return currentBitmap;
    }

    public static void setCurrentBitmap(Bitmap currentBitmap) {
        TooltipperApp.currentBitmap = currentBitmap;
    }

}
