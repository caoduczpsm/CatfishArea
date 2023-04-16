package com.example.catfisharea.activities.personal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityPersonalUserHomeBinding;


public class PersonalUserHomeActivity extends AppCompatActivity {
    private ActivityPersonalUserHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPersonalUserHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }

    private void init(){

    }
}