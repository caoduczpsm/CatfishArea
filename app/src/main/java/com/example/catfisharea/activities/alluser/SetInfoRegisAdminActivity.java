package com.example.catfisharea.activities.alluser;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.android.app.catfisharea.databinding.ActivitySetInfoRegisAdminBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ViewPagerAdapter;
import com.example.catfisharea.fragments.alluser.CompanyRegisFragment;
import com.example.catfisharea.fragments.alluser.PersonalRegisFragment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

public class SetInfoRegisAdminActivity extends AppCompatActivity {
    private ActivitySetInfoRegisAdminBinding mBinding;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySetInfoRegisAdminBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Animatoo.animateSlideLeft(this);
        initActivity();
        
    }

    private void initActivity() {
        //PreferenceManager
        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        override.fontScale = 1.0f;
        applyOverrideConfiguration(override);

        super.attachBaseContext(newBase);
    }
}