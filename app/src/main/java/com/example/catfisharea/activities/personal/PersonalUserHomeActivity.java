package com.example.catfisharea.activities.personal;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityPersonalUserHomeBinding;


public class PersonalUserHomeActivity extends AppCompatActivity {
    private ActivityPersonalUserHomeBinding mBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPersonalUserHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
    }
}