package es.dmoral.tappic.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
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
import android.widget.LinearLayout;
import android.widget.Toast;

import es.dmoral.tappic.R;
import es.dmoral.tappic.TooltipperApp;
import es.dmoral.tappic.activities.DetailActivity;
import es.dmoral.tappic.utils.Constants;
import es.dmoral.tappic.utils.InternetUtils;

public class TooltipperService extends Service {

    private WindowManager windowManager;
    private ImageView tooltipImage;
    private LinearLayout tooltipContainer;
    private WindowManager.LayoutParams params;

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

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        ContextThemeWrapper ctx = new ContextThemeWrapper(this, R.style.TranslucentAppTheme);
        tooltipContainer = (LinearLayout) LayoutInflater.from(ctx)
                .inflate(R.layout.tooltip_layout, null);

        tooltipImage = (ImageView) tooltipContainer.findViewById(R.id.tooltip_image);
        deleteTooltipAnim = AnimationUtils.loadAnimation(this, R.anim.delete_tooltip_anim);

        detector = new GestureDetector(this, new GestureTap());

        //fabDeleteImage = (FloatingActionButton) tooltipContainer.findViewById(R.id.fab_delete_image);

        this.params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        windowManager.addView(tooltipContainer, params);

        setListeners();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        dataString = intent.getDataString();
        if (dataString != null)
            new GetBitmapFromURLAsync().execute(dataString);
        else if (TooltipperApp.getCurrentBitmap() != null) {
            tooltipImage.setImageDrawable(new BitmapDrawable(getResources(), TooltipperApp.getCurrentBitmap()));
            tooltipContainer.setVisibility(View.VISIBLE);
        }

        return START_NOT_STICKY;
    }

    private void setListeners() {
        tooltipImage.setOnTouchListener(new View.OnTouchListener() {

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

        deleteTooltipAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
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
        if (tooltipImage != null) windowManager.removeView(tooltipContainer);
    }

    private class GestureTap extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onDoubleTap(MotionEvent e) {
            tooltipImage.startAnimation(deleteTooltipAnim);
            return true;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            tooltipImage.setVisibility(View.INVISIBLE);
            Intent toDetailActivity = new Intent(TooltipperService.this, DetailActivity.class);
            toDetailActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            toDetailActivity.putExtra(Constants.CURRENT_URL_EXTRA, dataString);
            startActivity(toDetailActivity);
            TooltipperService.this.stopSelf();
            return true;
        }
    }

    private class GetBitmapFromURLAsync extends AsyncTask<String, Void, Bitmap> {

        Toast loadingImageToast = Toast.makeText(TooltipperService.this, R.string.loading_image, Toast.LENGTH_SHORT);

        @Override
        protected void onPreExecute() {
            loadingImageToast.show();
        }

        @Override
        protected Bitmap doInBackground(String... src) {
            try {
                return InternetUtils.getBitmapFromURL(src[0]);
            } catch (java.lang.OutOfMemoryError ome) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                TooltipperApp.setCurrentBitmap(bitmap);
                tooltipImage.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                tooltipContainer.setVisibility(View.VISIBLE);
                if (loadingImageToast != null)
                    loadingImageToast.cancel();
            } else {
                Toast.makeText(getApplicationContext(), R.string.out_of_memory_text, Toast.LENGTH_LONG).show();
                TooltipperService.this.stopSelf();
            }
        }
    }

}
