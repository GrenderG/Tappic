package es.dmoral.tooltipper;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import es.dmoral.tooltipper.services.TooltipperService;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent launchServiceIntent = new Intent(this, TooltipperService.class);
        if (getIntent().getDataString() != null)
            launchServiceIntent.setData(Uri.parse(getIntent().getDataString()));
        startService(launchServiceIntent);
        finish();
    }
}
