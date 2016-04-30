package es.dmoral.tappic.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import es.dmoral.tappic.R;
import es.dmoral.tappic.helpers.PreferenceHelper;
import es.dmoral.tappic.services.TooltipperService;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!PreferenceHelper.isPrefsInitialized())
            PreferenceHelper.initializePrefs(this);

        if (PreferenceHelper.readBoolean(PreferenceHelper.PREF_FIRST_BOOT, true) ||
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
