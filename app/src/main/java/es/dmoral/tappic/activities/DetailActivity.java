package es.dmoral.tappic.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import es.dmoral.tappic.R;
import es.dmoral.tappic.TooltipperApp;
import es.dmoral.tappic.services.TooltipperService;
import es.dmoral.tappic.utils.Constants;
import es.dmoral.tappic.utils.InternetUtils;
import es.dmoral.tappic.utils.TooltipperUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DetailActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private Toolbar toolbar;
    private ImageView fullTooltipImage;
    private RelativeLayout fullTooltipContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        initViews();
        setupViews();
    }

    private void initViews() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.fullTooltipImage = (ImageView) findViewById(R.id.full_tooltip_image);
        this.fullTooltipContainer = (RelativeLayout) findViewById(R.id.full_tooltip_container);
    }

    private void setupViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        fullTooltipImage.setImageDrawable(new BitmapDrawable(getResources(), TooltipperApp.getCurrentBitmap()));
        new PhotoViewAttacher(fullTooltipImage);

        toolbar.setTitleTextColor(Color.BLACK);
        fullTooltipContainer.setBackgroundColor(Color.BLACK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch(item.getItemId()) {
            case R.id.action_download:
                downloadImage();
                return true;
            case R.id.action_share:
                shareImage();
                return true;
            case R.id.action_flip_back:
                backToPreview();
                break;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return false;
    }

    private void backToPreview() {
        onBackPressed();
        final Intent launchServiceIntent = new Intent(this, TooltipperService.class);
        TooltipperApp.setCurrentBitmap(((BitmapDrawable)fullTooltipImage.getDrawable()).getBitmap());
        launchServiceIntent.setData(Uri.parse(getIntent()
                .getStringExtra(Constants.CURRENT_URL_EXTRA)));
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startService(launchServiceIntent);
            }
        }, 500);
        finish();
    }

    private void shareImage() {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.putExtra(Intent.EXTRA_TEXT, getIntent().getStringExtra(Constants.CURRENT_URL_EXTRA));
        startActivity(Intent.createChooser(share, getString(R.string.share_image_text)));
    }

    private void downloadImage() {
        if (!(ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    REQUEST_WRITE_STORAGE);
        } else {
            Toast.makeText(DetailActivity.this, R.string.saving_image_text, Toast.LENGTH_SHORT).show();
            final String fileName = TooltipperUtils.getStringFromRegex(getIntent()
                    .getStringExtra(Constants.CURRENT_URL_EXTRA), Constants.TEXT_AFTER_SLASH_REGEX) + ".png";
            new Thread(new Runnable() {
                Toast downloadingImageToast = Toast.makeText(DetailActivity.this, R.string.saving_image_text, Toast.LENGTH_SHORT);

                @Override
                public void run() {
                    downloadingImageToast.show();
                    final boolean isImageSaved = InternetUtils.storeImage(DetailActivity.this, TooltipperApp.getCurrentBitmap(), fileName);
                    DetailActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (downloadingImageToast != null)
                                downloadingImageToast.setText(isImageSaved ?
                                        R.string.image_saved_text : R.string.error_saving_image_text);
                        }
                    });
                }
            }).start();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    downloadImage();
                else
                    Toast.makeText(this, "You need to give write permissions in order to save the image", Toast.LENGTH_LONG).show();
                break;
        }
    }
}
