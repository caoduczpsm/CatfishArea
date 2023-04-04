package com.example.catfisharea.activities.admin;

import android.content.Intent;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityAdminHomeBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ViewPlanActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class AdminHomeActivity extends BaseActivity {

    private ActivityAdminHomeBinding mBinding;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAdminHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setListener();
    }

    private void setListener() {
        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes);
        mBinding.recyclerViewAdminHome.setAdapter(homeAdapter);

        mBinding.layoutControlAdminHome.layoutSeason.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewPlanActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlAdminHome.layoutAccount.setOnClickListener(view -> {

        });

//        ItemHome item1 = new ItemHome();
//        Campus re1 = new Campus("1", "Khu 01", null, "1");
//        item1.setRegionModel(re1);
//
//        Pond p1 = new Pond("1", "Ao 01", null, "1", "1000");
//        Pond p2 = new Pond("2", "Ao 02", null, "1", "1000");
//        Pond p3 = new Pond("3", "Ao 03", null, "1", "1000");
//        List<RegionModel> l1 = new ArrayList<>();
//        l1.add(p1);
//        l1.add(p2);
//        l1.add(p3);
//
//        item1.setReginonList(l1);
//        itemHomes.add(item1);
//        itemHomes.add(item1);
//        itemHomes.add(item1);
//        homeAdapter.notifyDataSetChanged();
    }


}