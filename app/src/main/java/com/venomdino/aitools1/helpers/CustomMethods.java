package com.venomdino.aitools1.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.venomdino.aitools1.R;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class CustomMethods {

    private static final String TAG = "MADARA";

    public static void showErrorAlert(Activity activity, String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setCancelable(true);
        builder.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());
        builder.setIcon(R.drawable.error_24);
        builder.create().show();
    }

//    ----------------------------------------------------------------------------------------------

    public static void storeErrorLog(Activity activity, String error) {

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String formattedDateTime = now.format(formatter);

        String logFileName = "log_" + formattedDateTime;

        File logDir = activity.getExternalFilesDir("Logs");

        if (logDir != null) {

            if (!logDir.exists()){
                if (logDir.mkdirs()){
                    activity.runOnUiThread(() -> Toast.makeText(activity, "Log directory created at " + logDir.getAbsolutePath(), Toast.LENGTH_SHORT).show());
                }
            }

            File textFile = new File(logDir.getAbsolutePath() + "/" + logFileName);

            try {
                if (textFile.createNewFile()) {
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(textFile))) {
                        writer.write(error);
                    } catch (IOException e) {
                        Log.e(TAG, "storeErrorLog: ", e);
                    }
                }
            } catch (IOException e) {
                Log.e(TAG, "storeErrorLog: ", e);
            }
        }
    }

//    ----------------------------------------------------------------------------------------------


    public static boolean isValidImage(Activity activity, Uri uri) {
        try {
            InputStream inputStream = activity.getContentResolver().openInputStream(uri);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            if (inputStream != null) {
                inputStream.close();
            }

            return (options.outWidth > 0 && options.outHeight > 0);

        } catch (Exception e) {
            Log.e(TAG, "isValidImage: ", e);
            return false;
        }
    }

//    ----------------------------------------------------------------------------------------------

    @SuppressWarnings("deprecation")
    public static boolean isNetworkConnected(Activity activity) {
        ConnectivityManager connectivityManager = (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnectedOrConnecting();
    }

}
