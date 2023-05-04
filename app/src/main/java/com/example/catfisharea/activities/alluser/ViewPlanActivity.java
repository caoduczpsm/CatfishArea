package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.android.app.catfisharea.databinding.ActivityViewPlanBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ViewPlanActivity extends BaseActivity implements CampusListener {
    private ActivityViewPlanBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityViewPlanBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        mBinding.toolbarViewPlan.setNavigationOnClickListener(view -> onBackPressed());
        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBinding.recyclerViewPlan.setLayoutManager(linearLayoutManager);
        mBinding.recyclerViewPlan.setAdapter(homeAdapter);

        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        switch (type) {
            case Constants.KEY_ADMIN:
                mBinding.fabNewPlan.setVisibility(View.GONE);
                getDataHomeAdmin();
                break;
            case Constants.KEY_REGIONAL_CHIEF:
                getDataHomeRegionalChief();
                break;
            case Constants.KEY_DIRECTOR:
                mBinding.fabNewPlan.setVisibility(View.GONE);
                getDataHomeDirector();
                break;
        }

        mBinding.fabNewPlan.setOnClickListener(view -> {
            Intent intent = new Intent(this, CreatePlanActivity.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataHomeDirector() {
        String campusId = preferenceManager.getString(Constants.KEY_CAMPUS_ID);
        assert campusId != null;
        ItemHome itemHome = new ItemHome();
        database.collection(Constants.KEY_COLLECTION_CAMPUS).document(campusId)
                .get().addOnSuccessListener(campusDocument -> {
                    String name = campusDocument.getString(Constants.KEY_NAME);
                    String areaId = campusDocument.getString(Constants.KEY_AREA_ID);
                    itemHome.setRegionModel(new Campus(campusId, name, null, areaId));
                    itemHomes.add(itemHome);
                    homeAdapter.notifyDataSetChanged();
                });
        database.collection(Constants.KEY_COLLECTION_POND).whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                .get().addOnSuccessListener(pondQuery -> {
                    List<RegionModel> regionModels = new ArrayList<>();
                    for (DocumentSnapshot pondDocument : pondQuery.getDocuments()) {
                        String pondId = pondDocument.getId();
                        String pondName = pondDocument.getString(Constants.KEY_NAME);
                        String acreage = pondDocument.getString(Constants.KEY_ACREAGE);
                        List<String> numOfFeedingList = (List<String>) pondDocument.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                        List<String> amountFedList = (List<String>) pondDocument.get(Constants.KEY_AMOUNT_FED);
                        List<String> specificationsToMeasureList = (List<String>) pondDocument.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                        HashMap<String, Object> parameters = (HashMap<String, Object>) pondDocument.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                        int numOfFeeding = Integer.parseInt(Objects.requireNonNull(pondDocument.getString(Constants.KEY_NUM_OF_FEEDING)));
                        Pond pond = new Pond(pondId, pondName, null, campusId, acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters);
                        regionModels.add(pond);
                    }
                    itemHomes.sort((o1, o2) -> (o1.getRegionModel().getName()
                            .compareToIgnoreCase(o2.getRegionModel().getName())));
                    regionModels.sort((o1, o2) -> (o1.getName()
                            .compareToIgnoreCase(o2.getName())));
                    itemHome.setReginonList(regionModels);
                    homeAdapter.notifyDataSetChanged();
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataHomeRegionalChief() {
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
                                        String acreage = pondDoc.getString(Constants.KEY_ACREAGE);
                                        List<String> numOfFeedingList = (List<String>) pondDoc.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                                        List<String> amountFedList = (List<String>) pondDoc.get(Constants.KEY_AMOUNT_FED);
                                        List<String> specificationsToMeasureList = (List<String>) pondDoc.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                                        HashMap<String, Object> parameters = (HashMap<String, Object>) pondDoc.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                                        int numOfFeeding = Integer.parseInt(Objects.requireNonNull(pondDoc.getString(Constants.KEY_NUM_OF_FEEDING)));
                                        Pond pond = new Pond(pondId, pondName, null, campusId, acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters);
                                        regionModels.add(pond);
                                    }

                                    itemHome.setReginonList(regionModels);
                                    itemHomes.add(itemHome);
                                    itemHome.getReginonList().sort((o1, o2) -> (o1.getName()
                                            .compareToIgnoreCase(o2.getName())));
                                    itemHomes.sort((o1, o2) -> (o1.getRegionModel().getName()
                                            .compareToIgnoreCase(o2.getRegionModel().getName())));
                                    homeAdapter.notifyDataSetChanged();
                                });
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataHomeAdmin() {
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
                                    itemHome.getReginonList().sort((o1, o2) -> (o1.getName().compareToIgnoreCase(o2.getName())));
                                    itemHomes.sort((o1, o2) -> (o1.getRegionModel().getName()
                                            .compareToIgnoreCase(o2.getRegionModel().getName())));
                                    homeAdapter.notifyDataSetChanged();
                                });

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void OnCampusClicker(RegionModel regionModel) {
        if (homeAdapter.isShowed()) {
            homeAdapter.setShowed(false);
            homeAdapter.notifyDataSetChanged();
        } else {
            List<RegionModel> regionModels = new ArrayList<>();
            database.collection(Constants.KEY_COLLECTION_POND)
                    .whereEqualTo(Constants.KEY_CAMPUS_ID, regionModel.getId())
                    .get().addOnSuccessListener(pondQuery -> {
                        for (DocumentSnapshot pondDocument : pondQuery) {
                            String pondId = pondDocument.getId();
                            String pondName = pondDocument.getString(Constants.KEY_NAME);
                            String acreage = pondDocument.getString(Constants.KEY_ACREAGE);
                            List<String> numOfFeedingList = (List<String>) pondDocument.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                            List<String> amountFedList = (List<String>) pondDocument.get(Constants.KEY_AMOUNT_FED);
                            List<String> specificationsToMeasureList = (List<String>) pondDocument.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                            HashMap<String, Object> parameters = (HashMap<String, Object>) pondDocument.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                            int numOfFeeding = Integer.parseInt(Objects.requireNonNull(pondDocument.getString(Constants.KEY_NUM_OF_FEEDING)));
                            Pond pond = new Pond(pondId, pondName, null, regionModel.getId(), acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters);
                            regionModels.add(pond);
                        }
                        homeAdapter.setRegionModels(regionModels);
                        homeAdapter.setShowed(true);
                        homeAdapter.notifyDataSetChanged();
                    });
        }
    }

    @Override
    public void OnPondClicker(RegionModel regionModel) {
        Intent intent = new Intent(this, DetailPlanActivity.class);
        intent.putExtra(Constants.KEY_POND, regionModel);
        startActivity(intent);
    }

    @Override
    public void onCreatePlan(RegionModel regionModel) {

    }

}