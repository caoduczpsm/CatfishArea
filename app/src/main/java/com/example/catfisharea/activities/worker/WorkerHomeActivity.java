package com.example.catfisharea.activities.worker;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.catfisharea.R;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.databinding.ActivityWorkerHomeBinding;

public class WorkerHomeActivity extends BaseActivity {

    private ActivityWorkerHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWorkerHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
    }
}