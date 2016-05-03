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

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;

import es.dmoral.tappic.R;
import es.dmoral.tappic.TooltipperApp;
import es.dmoral.tappic.services.TooltipperService;
import es.dmoral.tappic.utils.Constants;
import es.dmoral.tappic.utils.InternetUtils;
import es.dmoral.tappic.utils.StorageUtils;
import es.dmoral.tappic.utils.TooltipperUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DetailActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private Toolbar toolbar;
    private SimpleDraweeView fullTooltipGif;
    private ImageView fullTooltipImage;
    private RelativeLayout fullTooltipContainer;

    private String imageUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        this.imageUrl = getIntent().getStringExtra(Constants.CURRENT_URL_EXTRA);

        initViews();
        setupViews();

    }

    private void initViews() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.fullTooltipGif = (SimpleDraweeView) findViewById(R.id.full_tooltip_gif);
        this.fullTooltipImage = (ImageView) findViewById(R.id.full_tooltip_image);
        this.fullTooltipContainer = (RelativeLayout) findViewById(R.id.full_tooltip_container);
    }

    private void setupViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (this.imageUrl.contains(".gif")) {
            this.fullTooltipGif.setVisibility(View.VISIBLE);
            this.fullTooltipGif.setImageURI(Uri.parse(imageUrl));
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setUri(Uri.parse(imageUrl))
                    .setAutoPlayAnimations(true)
                    .build();
            this.fullTooltipGif.setController(controller);
        } else {
            this.fullTooltipImage.setVisibility(View.VISIBLE);
            this.fullTooltipImage.setImageDrawable(new BitmapDrawable(getResources(), TooltipperApp.getCurrentBitmap()));
            final PhotoViewAttacher photoViewAttacher = new PhotoViewAttacher(this.fullTooltipImage);
        }

        this.toolbar.setTitleTextColor(Color.BLACK);
        this.fullTooltipContainer.setBackgroundColor(Color.BLACK);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.details_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
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
        launchServiceIntent.setData(Uri.parse(this.imageUrl));
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
        share.putExtra(Intent.EXTRA_TEXT, imageUrl);
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
            final String fileName = TooltipperUtils.getStringFromRegex(imageUrl, Constants.TEXT_AFTER_SLASH_REGEX) +
                    (this.imageUrl.contains(".gif") ? ".gif" : ".png");
            new Thread(new Runnable() {
                Toast downloadingImageToast = Toast.makeText(DetailActivity.this, R.string.saving_image_text, Toast.LENGTH_SHORT);

                @Override
                public void run() {
                    this.downloadingImageToast.show();
                    final boolean isImageSaved;
                    if (fileName.endsWith(".png"))
                        isImageSaved = StorageUtils.storeImage(DetailActivity.this, InternetUtils.getBitmapFromURL(imageUrl), fileName);
                    else
                        isImageSaved = StorageUtils.storeGif(DetailActivity.this, InternetUtils.getBytesFromUrl(imageUrl), fileName);
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
