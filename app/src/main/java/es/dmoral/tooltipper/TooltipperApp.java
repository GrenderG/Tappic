package es.dmoral.tooltipper;

import android.app.Application;
import android.content.Context;

/**
 * Created by grender on 27/04/16.
 */
public class TooltipperApp extends Application{

    private static Context appContext;

    @Override
    public void onCreate() {
        super.onCreate();
        TooltipperApp.appContext = getApplicationContext();
    }

    public static Context getAppContext() {
        return TooltipperApp.appContext;
    }

}
