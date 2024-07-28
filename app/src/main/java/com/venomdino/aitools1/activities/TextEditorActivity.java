package com.venomdino.aitools1.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;

import com.google.android.material.appbar.MaterialToolbar;
import com.venomdino.aitools1.R;
import com.venomdino.aitools1.databinding.ActivityTextEditorBinding;

public class TextEditorActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityTextEditorBinding binding = ActivityTextEditorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        MaterialToolbar toolbar = binding.toolbar.topAppBar;
        setSupportActionBar(toolbar);

        toolbar.setNavigationIcon(AppCompatResources.getDrawable(this, R.drawable.arrow_back_24));
        toolbar.setTitle("Generated Texts");
        toolbar.setNavigationOnClickListener(v -> onBackPressed());


        Intent intent = getIntent();

        if (intent.hasExtra("generatedText")){

            String generatedText = intent.getStringExtra("generatedText");
            binding.editText.setText(generatedText);

        } else {
            Toast.makeText(this, "Invalid access.", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}