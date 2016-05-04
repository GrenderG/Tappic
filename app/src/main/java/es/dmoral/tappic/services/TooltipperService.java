package es.dmoral.tappic.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.IBinder;
import android.support.v7.view.ContextThemeWrapper;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.FutureTarget;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.facebook.drawee.backends.pipeline.Fresco;

import java.io.File;

import es.dmoral.tappic.R;
import es.dmoral.tappic.activities.DetailActivity;
import es.dmoral.tappic.custom.RoundedCornerLayout;
import es.dmoral.tappic.utils.Constants;
import es.dmoral.tappic.utils.StorageUtils;

public class TooltipperService extends Service {

    private WindowManager windowManager;
    private ImageView tooltipImage;
    private RoundedCornerLayout tooltipContainer;
    private WindowManager.LayoutParams params;
    private TextView gifInfo;

    private Animation deleteTooltipAnim;
    private GestureDetector detector;
    private String dataString;

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        Fresco.initialize(this);

        this.windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        ContextThemeWrapper ctx = new ContextThemeWrapper(this, R.style.TranslucentAppTheme);
        this.tooltipContainer = (RoundedCornerLayout) LayoutInflater.from(ctx)
                .inflate(R.layout.tooltip_layout, null);

        this.gifInfo = (TextView) tooltipContainer.findViewById(R.id.gif_info);
        this.tooltipImage = (ImageView) tooltipContainer.findViewById(R.id.tooltip_image);
        this.deleteTooltipAnim = AnimationUtils.loadAnimation(this, R.anim.delete_tooltip_anim);

        this.detector = new GestureDetector(this, new GestureTap());

        this.params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        this.params.gravity = Gravity.CENTER;

        this.windowManager.addView(tooltipContainer, params);

        setListeners();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.dataString = intent.getDataString();
        if (this.dataString != null) {
            //new GetCacheTask(this).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this.dataString);
            final Toast loadingImage = Toast.makeText(TooltipperService.this, R.string.loading_image, Toast.LENGTH_SHORT);
            loadingImage.show();
            Glide.with(TooltipperService.this)
                    .load(dataString)
                    .asBitmap()
                    .into(new SimpleTarget<Bitmap>() {
                        @Override
                        public void onLoadFailed(Exception e, Drawable errorDrawable) {
                            loadingImage.cancel();
                            Toast.makeText(getApplicationContext(), R.string.out_of_memory_text, Toast.LENGTH_LONG).show();
                            TooltipperService.this.stopSelf();
                        }

                        @Override
                        public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {
                            tooltipImage.setImageBitmap(resource);
                            if (dataString.endsWith(".gif")) {
                                gifInfo.setVisibility(View.VISIBLE);
                            }
                            loadingImage.cancel();

                        }
                    });
        }

        return START_NOT_STICKY;
    }

    private void setListeners() {
        this.tooltipImage.setOnTouchListener(new View.OnTouchListener() {

            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                detector.onTouchEvent(motionEvent);
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (motionEvent.getRawX() - initialTouchX);
                        params.y = initialY + (int) (motionEvent.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(tooltipContainer, params);
                        return true;
                }
                return false;
            }
        });

        this.deleteTooltipAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                gifInfo.setVisibility(View.GONE);
                tooltipImage.setEnabled(false);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                TooltipperService.this.stopSelf();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (this.tooltipContainer != null) this.windowManager.removeView(this.tooltipContainer);
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            tooltipImage.startAnimation(deleteTooltipAnim);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            tooltipContainer.setVisibility(View.INVISIBLE);
            Intent toDetailActivity = new Intent(TooltipperService.this, DetailActivity.class);
            toDetailActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            toDetailActivity.putExtra(Constants.CURRENT_URL_EXTRA, dataString);
            startActivity(toDetailActivity);
            TooltipperService.this.stopSelf();
            return true;
        }
    }

}
