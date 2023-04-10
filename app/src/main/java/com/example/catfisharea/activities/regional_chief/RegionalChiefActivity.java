package com.example.catfisharea.activities.regional_chief;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityDirectorHomeBinding;
import com.android.app.catfisharea.databinding.ActivityRegionalChiefBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.admin.WarehouseActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.activities.alluser.ViewPlanActivity;
import com.example.catfisharea.activities.director.DirectorHomeActivity;
import com.example.catfisharea.activities.director.HumanResourceActivity;
import com.example.catfisharea.activities.director.RequestManagementActivity;
import com.example.catfisharea.activities.director.TaskManagerActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class RegionalChiefActivity extends BaseActivity {
    private ActivityRegionalChiefBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRegionalChiefBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initActivity();
        setListener();
    }

    private void setListener() {
        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes);
        mBinding.recyclerViewRegionalChiefHome.setAdapter(homeAdapter);
        getDataHome();

        mBinding.toolbaRegionalChiefHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        mBinding.imageConference.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ConferenceActivity.class));
        });

        mBinding.imageChat.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ConversationActivity.class));
        });

        mBinding.imageLogout.setOnClickListener(view -> {
            logOut();
        });

        mBinding.layoutControlRegionalChiefHome.layoutWarehouse.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), WarehouseActivity.class));
        });

        mBinding.layoutControlRegionalChiefHome.layoutSeason.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewPlanActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlRegionalChiefHome.layoutHR.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), HumanResourceActivity.class));
        });

        mBinding.layoutControlRegionalChiefHome.layoutTask.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), TaskManagerActivity.class));
        });

        mBinding.layoutControlRegionalChiefHome.layoutRequest.setOnClickListener(view -> {
            Intent intent = new Intent(this, RequestManagementActivity.class);
            startActivity(intent);
        });

    }

    private void initActivity() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
    }


    private void getDataHome() {
        String areaId = preferenceManager.getString(Constants.KEY_AREA_ID);
        assert areaId != null;

        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, areaId)
                .get().addOnSuccessListener(campusQuery -> {
                    for (DocumentSnapshot campusDoc : campusQuery.getDocuments()) {
                        ItemHome itemHome = new ItemHome();
                        List<RegionModel> regionModels = new ArrayList<>();
                        String campusId = campusDoc.getId();
                        String campusName = campusDoc.getString(Constants.KEY_NAME);
                        Campus campus = new Campus(campusId, campusName);
                        itemHome.setRegionModel(campus);
                        database.collection(Constants.KEY_COLLECTION_POND)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                                .get().addOnSuccessListener(pondQuery -> {
                                    for (DocumentSnapshot pondDoc : pondQuery.getDocuments()) {
                                        String pondId = pondDoc.getId();
                                        String pondName = pondDoc.getString(Constants.KEY_NAME);
                                        Pond pond = new Pond(pondId, pondName);
                                        regionModels.add(pond);
                                    }

                                    itemHome.setReginonList(regionModels);
                                    itemHomes.add(itemHome);
                                    Collections.sort(itemHome.getReginonList(),
                                            (o1, o2) -> (o1.getName()
                                                    .compareToIgnoreCase(o2.getName())));
                                    Collections.sort(itemHomes,
                                            (o1, o2) -> (o1.getRegionModel().getName()
                                                    .compareToIgnoreCase(o2.getRegionModel().getName())));
                                    homeAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    // Hàm đăng xuất
    public void logOut() {

        showToast("Đang đăng xuất...");

        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );

        // Xóa bộ nhớ tạm
        preferenceManager.clear();

        // Xóa FCM TOKEN
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    // Xóa bộ nhớ tạm
                    preferenceManager.clear();
                    // Chuyển sang màn hình đăng nhập
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    Animatoo.animateSlideLeft(this);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Không thể đăng xuất..."));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}