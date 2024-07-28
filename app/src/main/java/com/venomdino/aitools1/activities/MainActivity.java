package com.venomdino.aitools1.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.android.material.navigation.NavigationView;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.venomdino.aitools1.BuildConfig;
import com.venomdino.aitools1.R;
import com.venomdino.aitools1.databinding.ActivityMainBinding;
import com.venomdino.aitools1.helpers.CustomMethods;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final int PICK_IMAGE_REQUEST = 500;
    private static final int PERMISSION_CODE = 101;
    private static final int TAKE_PHOTO_REQUEST = 600;
    private final String TAG = "MADARA";
    private Uri photoUri;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!CustomMethods.isNetworkConnected(this)){
            Toast.makeText(this, "No internet connection.", Toast.LENGTH_SHORT).show();
            new Handler().postDelayed(this::finish, 1500);
        }

//        ------------------------------------------------------------------------------------------
        Toolbar toolbar = binding.toolbar.topAppBar;
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(true);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, binding.getRoot(), toolbar, 0, 0);
        toggle.syncState();

        binding.getRoot().addDrawerListener(toggle);

        View headView = binding.navigationView.getHeaderView(0);

        ((TextView) headView.findViewById(R.id.header_layout_version_tv)).setText("Version: " + BuildConfig.VERSION_NAME);

        navigationViewItemClickedActions(binding.navigationView);


//        ------------------------------------------------------------------------------------------

        binding.openGalleryBtn.setOnClickListener(view -> {
            if (isPermissionGranted()) {
                openGallery();
            }
        });

        binding.openCamBtn.setOnClickListener(v -> {
            if (isPermissionGranted()) {
                openCamera();
            }
        });

        Intent receivedIntent = getIntent();

        if (receivedIntent.hasExtra("croppedImageUri")) {

            Uri croppedImageUri = receivedIntent.getParcelableExtra("croppedImageUri");

            if (croppedImageUri != null) {

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), croppedImageUri);
                    readTextFromImage(this, bitmap);
                } catch (IOException e) {
                    CustomMethods.showErrorAlert(this, "Error", "Failed to extract texts. Something went wrong.");
                }
            } else {
                CustomMethods.showErrorAlert(this, "Error", "Oops! something went wrong.");
            }
        }
    }

//    ----------------------------------------------------------------------------------------------

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {

            Uri imageUri = data.getData();

            if (CustomMethods.isValidImage(this, imageUri)) {

                Intent sendIntent = new Intent(this, CropPhotoActivity.class);
                sendIntent.putExtra("imageUri", imageUri);
                startActivity(sendIntent);

            } else {
                Toast.makeText(this, "Invalid image.", Toast.LENGTH_SHORT).show();
            }
        }

        if (requestCode == TAKE_PHOTO_REQUEST && resultCode == RESULT_OK) {
            
            if (photoUri != null) {
                if (CustomMethods.isValidImage(this, photoUri)){
                    Intent sendIntent = new Intent(this, CropPhotoActivity.class);
                    sendIntent.putExtra("imageUri", photoUri);
                    startActivity(sendIntent);
                } else {
                    Toast.makeText(this, "Corrupted image.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Photo capture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }


//    ----------------------------------------------------------------------------------------------

    private void readTextFromImage(Activity activity, Bitmap bitmap) {

        ProgressDialog pd = new ProgressDialog(activity);
        pd.setCancelable(false);
        pd.setMessage("Generating texts...");
        pd.show();

        // The Gemini 1.5 models are versatile and work with both text-only and multimodal prompts
        GenerativeModel gm = new GenerativeModel("gemini-1.5-flash", BuildConfig.apiKey);
        GenerativeModelFutures model = GenerativeModelFutures.from(gm);

        Content content = new Content.Builder()
                .addText("What is written in this image. Tell me only written texts not other texts.")
                .addImage(bitmap)
                .build();

        Executor executor = Executors.newSingleThreadExecutor();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);
        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {

                String resultText = result.getText();

                new Handler(Looper.getMainLooper()).post(() -> {

                    pd.dismiss();

                    Intent intent = new Intent(activity, TextEditorActivity.class);
                    intent.putExtra("generatedText", resultText);
                    startActivity(intent);
                });
            }

            @Override
            public void onFailure(@NonNull Throwable t) {
                Log.e(TAG, "onFailure: ", t);

                new Handler(Looper.getMainLooper()).post(() -> {
                    pd.dismiss();
                    Toast.makeText(activity, "Failed to generate text. See logs.", Toast.LENGTH_SHORT).show();
                    CustomMethods.storeErrorLog(MainActivity.this, t.getMessage());
                });
            }
        }, executor);

    }
//    ----------------------------------------------------------------------------------------------

    private void openGallery() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

//    ----------------------------------------------------------------------------------------------

    private boolean isPermissionGranted() {
        String[] permissions;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions = new String[]{
                    Manifest.permission.READ_MEDIA_IMAGES,
                    Manifest.permission.CAMERA
            };
        } else {
            permissions = new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.CAMERA
            };
        }

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, PERMISSION_CODE);
                return false;
            }
        }
        return true;
    }

    //  ------------------------------------------------------------------------------------------------

    private void openCamera() {

        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (cameraIntent.resolveActivity(getPackageManager()) != null) {

            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (Exception ex) {
                Log.e(TAG, "openCamera: ", ex);
                Toast.makeText(this, "Error creating file.", Toast.LENGTH_SHORT).show();
            }

            // Continue only if the File was successfully created
            if (photoFile != null) {
                photoUri = FileProvider.getUriForFile(this, getPackageName() + ".nurAlamProvider", photoFile);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                startActivityForResult(cameraIntent, TAKE_PHOTO_REQUEST);
            }
        }
    }

    //  ------------------------------------------------------------------------------------------------
    private File createImageFile() {
        // Create an image file name
        @SuppressLint("SimpleDateFormat")
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        return new File(storageDir, imageFileName + ".jpg");
    }

//  ------------------------------------------------------------------------------------------------

    private void navigationViewItemClickedActions(NavigationView navigationView) {

        navigationView.setNavigationItemSelectedListener(item -> {

            if (item.getItemId() == R.id.report_bug_action) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_group))));

            } else if (item.getItemId() == R.id.rate_action) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + getPackageName())));

            } else if (item.getItemId() == R.id.share_action) {

                Intent intent1 = new Intent(Intent.ACTION_SEND);
                intent1.setType("text/plain");
                intent1.putExtra(Intent.EXTRA_TEXT, getResources().getString(R.string.app_sharing_message) + getPackageName());
                startActivity(Intent.createChooser(intent1, "Share via"));

            } else if (item.getItemId() == R.id.more_apps_action) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.more_apps))));

            } else if (item.getItemId() == R.id.visit_telegram) {

                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.official_telegram_channel))));
            }
            return false;
        });
    }

//  ------------------------------------------------------------------------------------------------


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSION_CODE) {

            if (grantResults.length < 1 || grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "onRequestPermissionsResult: Permission not granted " + grantResults.length);
            }
        }
    }
}