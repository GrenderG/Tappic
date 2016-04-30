package es.dmoral.tappic.activities;

import android.Manifest;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;

import com.chyrta.onboarder.OnboarderActivity;
import com.chyrta.onboarder.OnboarderPage;

import java.util.ArrayList;
import java.util.List;

import es.dmoral.tappic.R;
import es.dmoral.tappic.helpers.PreferenceHelper;

/**
 * Created by grender on 30/04/16.
 */
public class OnboardingActivity extends OnboarderActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setOnboardPagesReady(populateOnboardingList());
        shouldUseFloatingActionButton(true);
        //setTitleTextSize(12);
        //setDescriptionTextSize(12);
    }

    private List<OnboarderPage> populateOnboardingList() {
        List<OnboarderPage> onboardingList = new ArrayList<>();

        onboardingList.add(new OnboarderPage(getString(R.string.title_onboard_0), getString(R.string.msg_onboard_0), R.drawable.onboarder_0));
        onboardingList.get(0).setBackgroundColor(R.color.onboarding_0);
        onboardingList.add(new OnboarderPage(getString(R.string.title_onboard_1), getString(R.string.msg_onboard_1), R.drawable.onboarder_1));
        onboardingList.get(1).setBackgroundColor(R.color.onboarding_1);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            onboardingList.add(new OnboarderPage(getString(R.string.title_onboard_2), getString(R.string.msg_onboard_2), R.drawable.onboarder_2));
            onboardingList.get(2).setBackgroundColor(R.color.onboarding_2);
        }

        return  onboardingList;
    }

    @Override
    public void onFinishButtonPressed() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (!Settings.canDrawOverlays(this)) {
                Intent myIntent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
                startActivity(myIntent);
            }
        }
        if (PreferenceHelper.isPrefsInitialized())
            PreferenceHelper.writeBoolean(PreferenceHelper.PREF_FIRST_BOOT, false);

        finish();
    }
}
