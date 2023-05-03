package com.example.catfisharea.activities.alluser;

import android.os.Bundle;
import com.android.app.catfisharea.databinding.ActivityDetailPlanBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ViewPagerAdapter;
import com.example.catfisharea.fragments.alluser.EnvironmentViewFragment;
import com.example.catfisharea.fragments.alluser.OverviewPlanFragment;
import com.example.catfisharea.fragments.alluser.ViewFoodFragment;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

public class DetailPlanActivity extends BaseActivity {
    private ActivityDetailPlanBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private ViewPagerAdapter adapter;
    private OverviewPlanFragment overviewPlanFragment;
    private ViewFoodFragment viewFoodFragment;
    private EnvironmentViewFragment environmentViewFragment;

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
        preferenceManager = new PreferenceManager(this);

        mBinding.toolbarViewPlan.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.toolbarViewPlan.setTitle("Vụ nuôi " + pond.getName());

        overviewPlanFragment = new OverviewPlanFragment();
        viewFoodFragment = new ViewFoodFragment();
        environmentViewFragment = new EnvironmentViewFragment();

        Bundle bundle = new Bundle();
        bundle.putSerializable(Constants.KEY_POND, pond);
        overviewPlanFragment.setArguments(bundle);
        viewFoodFragment.setArguments(bundle);
        environmentViewFragment.setArguments(bundle);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(overviewPlanFragment, "Tổng quan");
        adapter.addFragment(viewFoodFragment, "Thức ăn");
        adapter.addFragment(environmentViewFragment, "Chất lượng nước");

        mBinding.viewPager.setAdapter(adapter);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);
    }

}