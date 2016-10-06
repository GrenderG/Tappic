package es.dmoral.tappic.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;

import es.dmoral.prefs.Prefs;
import es.dmoral.tappic.services.TooltipperService;
import es.dmoral.tappic.utils.Constants;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Prefs.with(this).readBoolean(Constants.PREF_FIRST_BOOT, true) ||
                (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)))
            launchOnBoardingScene();
        else {
            Intent launchServiceIntent = new Intent(this, TooltipperService.class);
            if (getIntent().getDataString() != null) {
                launchServiceIntent.setData(Uri.parse(getIntent().getDataString()));
                startService(launchServiceIntent);
            } else {
                launchOnBoardingScene();
            }
        }

        finish();
    }

    private void launchOnBoardingScene() {
        startActivity(new Intent(this, OnboardingActivity.class));
    }

}
