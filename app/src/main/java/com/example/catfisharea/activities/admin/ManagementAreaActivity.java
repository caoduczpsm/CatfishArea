package com.example.catfisharea.activities.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityManagementAreaBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.fragments.admin.CreateAreaFragment;
import com.example.catfisharea.fragments.admin.CreateCampusFragment;
import com.example.catfisharea.fragments.admin.CreatePondFragment;
import com.example.catfisharea.ultilities.Constants;

public class ManagementAreaActivity extends BaseActivity {
    private ActivityManagementAreaBinding mBinding;
    private String request;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityManagementAreaBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Animatoo.animateSlideLeft(ManagementAreaActivity.this);
        request = getIntent().getStringExtra("request");
        String typeItem = getIntent().getStringExtra("typeItem");
        Bundle bundle = new Bundle();
        if (request.equals("create")) {
//            Gửi action qua fragment
            bundle.clear();
            bundle.putString("action", "create");
            if (typeItem.equals(Constants.KEY_AREA)) {
                CreateAreaFragment createAreaFragment = new CreateAreaFragment();
                replaceFragment(createAreaFragment, bundle);
            } else if (typeItem.equals(Constants.KEY_CAMPUS)) {
                CreateCampusFragment createCampusFragment = new CreateCampusFragment();
                replaceFragment(createCampusFragment, bundle);
            } else if (typeItem.equals(Constants.KEY_POND)) {
                CreatePondFragment createPondFragment = new CreatePondFragment();
                replaceFragment(createPondFragment, bundle);
            }
        } else if (request.equals("edit")) {
            String idItem = getIntent().getStringExtra("idItem");
            if (typeItem.equals(Constants.KEY_AREA)) {
                CreateAreaFragment createAreaFragment = new CreateAreaFragment();
                bundle.clear();
                bundle.putString("action", "edit");
                bundle.putString("idItem", idItem);
                replaceFragment(createAreaFragment, bundle);
            } else if (typeItem.equals(Constants.KEY_CAMPUS)) {
                CreateCampusFragment createCampusFragment = new CreateCampusFragment();
                bundle.clear();
                bundle.putString("action", "edit");
                bundle.putString("idItem", idItem);
                replaceFragment(createCampusFragment, bundle);
            } else if (typeItem.equals(Constants.KEY_POND)) {
                CreatePondFragment createPondFragment = new CreatePondFragment();
                bundle.clear();
                bundle.putString("action", "edit");
                bundle.putString("idItem", idItem);
                replaceFragment(createPondFragment, bundle);
            }
        }

    }

    private void replaceFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentManageArea, fragment);
        transaction.commitAllowingStateLoss();
    }

    private void replaceFragment(Fragment fragment, Bundle bundle) {
        fragment.setArguments(bundle);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.contentManageArea, fragment);
        transaction.commitAllowingStateLoss();
    }
}