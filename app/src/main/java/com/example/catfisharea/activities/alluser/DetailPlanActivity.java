package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityDetailPlanBinding;
import com.android.app.catfisharea.databinding.ActivityImportWarehouseBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailPlanActivity extends BaseActivity {
    private ActivityDetailPlanBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDetailPlanBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        mBinding.toolbarViewPlan.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.toolbarViewPlan.setTitle("Vụ nuôi " + pond.getName());
        

    }


}