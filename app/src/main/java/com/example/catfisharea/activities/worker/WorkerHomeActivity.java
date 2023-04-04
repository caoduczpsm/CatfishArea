package com.example.catfisharea.activities.worker;

import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityWorkerHomeBinding;
import com.example.catfisharea.activities.BaseActivity;

public class WorkerHomeActivity extends BaseActivity {

    private ActivityWorkerHomeBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWorkerHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
    }
}