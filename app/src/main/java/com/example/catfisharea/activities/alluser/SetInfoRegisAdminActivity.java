package com.example.catfisharea.activities.alluser;

import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivitySetInfoRegisAdminBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ViewPagerAdapter;
import com.example.catfisharea.fragments.alluser.CompanyRegisFragment;
import com.example.catfisharea.fragments.alluser.PersonalRegisFragment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

public class SetInfoRegisAdminActivity extends BaseActivity {
    private ActivitySetInfoRegisAdminBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySetInfoRegisAdminBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        
        initActivity();
        
    }

    private void initActivity() {
        //PreferenceManager
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());

        // Lấy trạng thái đăng ký cá nhân hoặc công ty từ bộ nhớ tạm
        String typeRegis = preferenceManager.getString(Constants.KEY_TYPE_REGIS);

        if (typeRegis.equals(Constants.KEY_PERSONAL_REGIS)){
            adapter.addFragment(new PersonalRegisFragment(), "");
        } else {
            adapter.addFragment(new PersonalRegisFragment(), "");
            adapter.addFragment(new CompanyRegisFragment(), "");
        }

        mBinding.viewPager.setAdapter(adapter);
        mBinding.tabLayout.setupWithViewPager(mBinding.viewPager);

        mBinding.toolbarSetInfo.setNavigationOnClickListener(view -> onBackPressed());
    }
}