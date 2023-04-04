package com.example.catfisharea.activities.admin;

import android.content.Intent;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityAdminHomeBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.ViewPlanActivity;
import com.example.catfisharea.adapter.HomeAdapter;

import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.Filter;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firestore.v1.StructuredQuery;

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
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes);
        mBinding.recyclerViewAdminHome.setAdapter(homeAdapter);
        getDataHome();

        mBinding.toolbarAdminHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        mBinding.imageChat.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ConversationActivity.class));
        });

        mBinding.layoutControlAdminHome.layoutSeason.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewPlanActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlAdminHome.layoutAccount.setOnClickListener(view -> {

        });

    }

    private void getDataHome() {
        database.collection(Constants.KEY_COLLECTION_AREA)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(areaQueryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : areaQueryDocumentSnapshots.getDocuments()) {
                        String id = documentSnapshot.getId();
                        String name = documentSnapshot.getString(Constants.KEY_NAME);
                        Area area = new Area(id, name, null);
                        ItemHome itemHome = new ItemHome();
                        itemHome.setRegionModel(area);
                        List<RegionModel> campuses = new ArrayList<>();
                        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                                .whereEqualTo(Constants.KEY_AREA_ID, id).get().addOnSuccessListener(campusQueryDocumentSnapshots -> {
                                    for (DocumentSnapshot doc : campusQueryDocumentSnapshots.getDocuments()) {
                                        String idcampus = doc.getId();
                                        String nameCampus = doc.getString(Constants.KEY_NAME);
                                        Campus campus = new Campus(idcampus, nameCampus, null, id);
                                        campuses.add(campus);
                                    }
                                    itemHome.setReginonList(campuses);
                                    itemHomes.add(itemHome);
                                    homeAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }


}