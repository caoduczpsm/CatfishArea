package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityDiaryDetailBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ViewPagerAdapter;
import com.example.catfisharea.fragments.alluser.EnvironmentViewFragment;
import com.example.catfisharea.fragments.alluser.HarvestViewFragment;
import com.example.catfisharea.fragments.alluser.OverviewPlanFragment;
import com.example.catfisharea.fragments.alluser.ViewFoodFragment;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class DiaryDetailActivity extends BaseActivity {
    private ActivityDiaryDetailBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String planId;
    private ViewPagerAdapter adapter;
    private OverviewPlanFragment overviewPlanFragment;
    private ViewFoodFragment viewFoodFragment;
    private EnvironmentViewFragment environmentViewFragment;
    private HarvestViewFragment harvestViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDiaryDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        planId = getIntent().getStringExtra(Constants.KEY_ID_PLAN);
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        mBinding.toolbarViewPlan.setNavigationOnClickListener(view -> onBackPressed());


        overviewPlanFragment = new OverviewPlanFragment();
        viewFoodFragment = new ViewFoodFragment();
        environmentViewFragment = new EnvironmentViewFragment();
        harvestViewFragment = new HarvestViewFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_ID_PLAN, planId);
        overviewPlanFragment.setArguments(bundle);
        viewFoodFragment.setArguments(bundle);
        environmentViewFragment.setArguments(bundle);
        harvestViewFragment.setArguments(bundle);


        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(overviewPlanFragment, "Tổng quan");
        adapter.addFragment(viewFoodFragment, "Thức ăn");
        adapter.addFragment(environmentViewFragment, "Chất lượng nước");
        adapter.addFragment(harvestViewFragment, "Thu hoạch");

        mBinding.viewPager.setAdapter(adapter);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
    }
}