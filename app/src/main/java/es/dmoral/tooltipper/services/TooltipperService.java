package es.dmoral.tooltipper.services;

import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import es.dmoral.tooltipper.R;
import es.dmoral.tooltipper.custom.RoundedCornerLayout;
import es.dmoral.tooltipper.utils.InternetUtils;

public class TooltipperService extends Service {

    private WindowManager windowManager;
    private ImageView tooltipImage;
    private RoundedCornerLayout tooltipContainer;
    private Button deleteTooltipButton;
    private WindowManager.LayoutParams params;

    @Override
    public IBinder onBind(Intent intent) {
        // Not used
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        tooltipContainer = new RoundedCornerLayout(this);
        tooltipContainer.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        tooltipContainer.setOrientation(LinearLayout.VERTICAL);
        ((LinearLayout.LayoutParams)tooltipContainer.getLayoutParams()).setMargins(16, 16, 16, 16);
        tooltipContainer.setBackgroundColor(Color.LTGRAY);
        
        tooltipImage = new ImageView(this);
        tooltipImage.setAdjustViewBounds(true);

        deleteTooltipButton = new Button(this);
        deleteTooltipButton.setText(R.string.delete_image);
        deleteTooltipButton.setBackgroundColor(Color.RED);
        deleteTooltipButton.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT));
        ((LinearLayout.LayoutParams)deleteTooltipButton.getLayoutParams()).height = 80;

        tooltipContainer.addView(tooltipImage);
        tooltipContainer.addView(deleteTooltipButton);

        tooltipContainer.setVisibility(View.GONE);

        this.params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.TYPE_PHONE,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);

        params.gravity = Gravity.CENTER;

        windowManager.addView(tooltipContainer, params);

        setListeners();
        System.out.println("ASDASDASD SERVICE");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String dataString = intent.getDataString();
        if (dataString != null)
            new GetBitmapFromURLAsync().execute(dataString);
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
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = motionEvent.getRawX();
                        initialTouchY = motionEvent.getRawY();
                        System.out.println("ACTION DOWN");
                        return true;
                    case MotionEvent.ACTION_UP:
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

        deleteTooltipButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TooltipperService.this.stopSelf();
            }
        });

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tooltipImage != null) windowManager.removeView(tooltipContainer);
    }

    private class GetBitmapFromURLAsync extends AsyncTask<String, Void, Bitmap> {

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
                tooltipImage.setImageDrawable(new BitmapDrawable(getResources(), bitmap));
                tooltipContainer.setVisibility(View.VISIBLE);
            } else {
                Toast.makeText(getApplicationContext(), "La imagen es demasiado grande para previsualizar.", Toast.LENGTH_LONG).show();
                TooltipperService.this.stopSelf();
            }
        }
    }

}
