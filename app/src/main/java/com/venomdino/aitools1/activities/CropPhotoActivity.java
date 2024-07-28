package com.venomdino.aitools1.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.canhub.cropper.CropImageView;
import com.venomdino.aitools1.databinding.ActivityCropPhotoBinding;
import com.venomdino.aitools1.helpers.CustomMethods;

public class CropPhotoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCropPhotoBinding binding = ActivityCropPhotoBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();

        if (intent.hasExtra("imageUri")){
            Uri imageUri = intent.getParcelableExtra("imageUri");

            if (CustomMethods.isValidImage(this, imageUri)) {
                binding.cropImageView.setImageUriAsync(imageUri);
            } else {
                Toast.makeText(this, "Image corrupted", Toast.LENGTH_SHORT).show();
            }

        } else {
            onBackPressed();
        }


        binding.cropImageView.setOnCropImageCompleteListener((cropImageView, cropResult) -> {

            Intent intent1 = new Intent(CropPhotoActivity.this, MainActivity.class);
            intent1.putExtra("croppedImageUri",  cropResult.getUriContent());
            startActivity(intent1);

            new Handler().postDelayed(this::finish, 500);
        });

        binding.cropBtn.setOnClickListener(v -> {

            int reqWidth = binding.cropImageView.getWidth();
            int reqHeight = binding.cropImageView.getHeight();

            binding.cropImageView.croppedImageAsync(Bitmap.CompressFormat.JPEG, 90, reqWidth, reqHeight, CropImageView.RequestSizeOptions.RESIZE_INSIDE, null);
        });
    }
}