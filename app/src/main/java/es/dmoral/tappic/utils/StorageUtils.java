package es.dmoral.tappic.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by grender on 3/05/16.
 */
public class StorageUtils {

    public static boolean storeImage(Context context, Bitmap imageData, String filename) {
        //get path to external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory().toString();
        File sdIconStorageDir = new File(iconsStoragePath + "/Pictures");

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();

        try {
            File fileDir = new File(sdIconStorageDir.toString(), filename);
            FileOutputStream fileOutputStream = new FileOutputStream(fileDir);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            //choose another format if PNG doesn't suit you
            imageData.compress(Bitmap.CompressFormat.PNG, 100, bos);

            bos.flush();
            bos.close();

            MediaScannerConnection.scanFile(context, new String[]{fileDir.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

    public static boolean storeGif(Context context, byte[] gifBytes, String filename) {
        //get path to external storage (SD card)
        String iconsStoragePath = Environment.getExternalStorageDirectory().toString();
        File sdIconStorageDir = new File(iconsStoragePath + "/Pictures");

        //create storage directories, if they don't exist
        sdIconStorageDir.mkdirs();

        try {
            File fileDir = new File(sdIconStorageDir.toString(), filename);
            FileOutputStream fileOutputStream = new FileOutputStream(fileDir);

            BufferedOutputStream bos = new BufferedOutputStream(fileOutputStream);

            bos.write(gifBytes);

            bos.flush();
            bos.close();

            MediaScannerConnection.scanFile(context, new String[]{fileDir.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        public void onScanCompleted(String path, Uri uri) {
                        }
                    });

        } catch (FileNotFoundException e) {
            return false;
        } catch (IOException e) {
            return false;
        }

        return true;
    }

}
