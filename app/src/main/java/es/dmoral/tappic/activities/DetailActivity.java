package es.dmoral.tappic.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
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
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.controller.ControllerListener;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.image.ImageInfo;

import es.dmoral.tappic.R;
import es.dmoral.tappic.services.TooltipperService;
import es.dmoral.tappic.utils.Constants;
import es.dmoral.tappic.utils.InternetUtils;
import es.dmoral.tappic.utils.TooltipperUtils;
import uk.co.senab.photoview.PhotoViewAttacher;

public class DetailActivity extends AppCompatActivity {

    private static final int REQUEST_WRITE_STORAGE = 112;
    private Toolbar toolbar;
    private SimpleDraweeView fullTooltipGif;
    private ImageView fullTooltipImage;
    private RelativeLayout fullTooltipContainer;
    private ProgressBar progressBar;

    private String imageUrl;

    private DownloadManager downloadManager;
    private BroadcastReceiver onDownloadCompletedReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fresco.initialize(this);

        setContentView(R.layout.activity_detail);

        this.imageUrl = getIntent().getStringExtra(Constants.CURRENT_URL_EXTRA);
        this.downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);

        initViews();
        setupViews();

    }

    private void initViews() {
        this.toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.fullTooltipGif = (SimpleDraweeView) findViewById(R.id.full_tooltip_gif);
        this.fullTooltipImage = (ImageView) findViewById(R.id.full_tooltip_image);
        this.fullTooltipContainer = (RelativeLayout) findViewById(R.id.full_tooltip_container);
        this.progressBar = (ProgressBar) findViewById(R.id.progress_bar);
    }

    private void setupViews() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        if (this.imageUrl.contains(".gif")) {
            this.progressBar.setVisibility(View.VISIBLE);
            this.fullTooltipImage.setVisibility(View.GONE);
            this.fullTooltipGif.setImageURI(Uri.parse(imageUrl));
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setControllerListener(new ControllerListener<ImageInfo>() {
                        @Override
                        public void onSubmit(String id, Object callerContext) {

                        }

                        @Override
                        public void onFinalImageSet(String id, ImageInfo imageInfo, Animatable animatable) {
                            progressBar.setVisibility(View.INVISIBLE);
                        }

                        @Override
                        public void onIntermediateImageSet(String id, ImageInfo imageInfo) {

                        }

                        @Override
                        public void onIntermediateImageFailed(String id, Throwable throwable) {

                        }

                        @Override
                        public void onFailure(String id, Throwable throwable) {

                        }

                        @Override
                        public void onRelease(String id) {

                        }
                    })
                    .setUri(Uri.parse(imageUrl))
                    .setAutoPlayAnimations(true)
                    .build();
            this.fullTooltipGif.setController(controller);
        } else {
            this.progressBar.setVisibility(View.VISIBLE);
            this.fullTooltipGif.setVisibility(View.GONE);
            Glide.with(this)
                    .load(this.imageUrl)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            new PhotoViewAttacher(fullTooltipImage);
                            progressBar.setVisibility(View.INVISIBLE);
                            return false;
                        }
                    })
                    .into(this.fullTooltipImage);

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
            case R.id.clear_cache:
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        Fresco.getImagePipeline().clearCaches();
                        Glide.get(DetailActivity.this).clearDiskCache();
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Glide.get(DetailActivity.this).clearMemory();
                                Toast.makeText(DetailActivity.this, R.string.cache_cleared_text, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).start();
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
            this.progressBar.setVisibility(View.VISIBLE);
            final String fileName = TooltipperUtils.getStringFromRegex(imageUrl, Constants.TEXT_AFTER_SLASH_REGEX) +
                    (this.imageUrl.endsWith(".gif") ? ".gif" : ".png");
            if (this.onDownloadCompletedReceiver == null)
                this.onDownloadCompletedReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(DownloadManager.ACTION_DOWNLOAD_COMPLETE)) {
                            DownloadManager.Query query = new DownloadManager.Query();
                            Cursor cursor = downloadManager.query(query);
                            boolean downloadError = false;
                            if (cursor.moveToFirst()) {
                                int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
                                switch (status) {
                                    case DownloadManager.STATUS_SUCCESSFUL:
                                        progressBar.setVisibility(View.INVISIBLE);
                                        break;
                                    case DownloadManager.STATUS_PENDING:
                                    case DownloadManager.STATUS_RUNNING:
                                    case DownloadManager.STATUS_PAUSED:
                                        break;
                                    default:
                                        downloadError = true;
                                }
                            } else
                                downloadError = true;

                            if (downloadError) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(DetailActivity.this,
                                        R.string.error_saving_image_text, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                };
            registerReceiver(this.onDownloadCompletedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
            InternetUtils.downloadFileFromUrl(this.imageUrl, Constants.DOWNLOAD_FOLDER, fileName, this.downloadManager);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (onDownloadCompletedReceiver != null)
            registerReceiver(onDownloadCompletedReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (onDownloadCompletedReceiver != null)
            unregisterReceiver(onDownloadCompletedReceiver);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    downloadImage();
                else
                    Toast.makeText(this, R.string.write_permission_error, Toast.LENGTH_LONG).show();
                break;
        }
    }

}
