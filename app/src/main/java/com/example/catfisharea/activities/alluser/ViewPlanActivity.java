package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityViewPlanBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.adapter.PlanAdapter;

import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.Plan;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class ViewPlanActivity extends BaseActivity implements CampusListener{
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
        mBinding.recyclerViewPlan.setAdapter(homeAdapter);

        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        if (type.equals(Constants.KEY_ADMIN)) {
            getDataHomeAdmin();
        } else if (type.equals(Constants.KEY_REGIONAL_CHIEF)) {
            getDataHomeRegionalChief();
        } else if (type.equals(Constants.KEY_DIRECTOR)) {
            getDataHomeDirector();
        }
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
                    Collections.sort(itemHomes,
                            (o1, o2) -> (o1.getRegionModel().getName()
                                    .compareToIgnoreCase(o2.getRegionModel().getName())));
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
                                    Collections.sort(itemHome.getReginonList(),
                                            (o1, o2) -> (o1.getName().compareToIgnoreCase(o2.getName())));
                                    Collections.sort(itemHomes,
                                            (o1, o2) -> (o1.getRegionModel().getName()
                                                    .compareToIgnoreCase(o2.getRegionModel().getName())));
                                    homeAdapter.notifyDataSetChanged();
                                });

                    }
                });
    }

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
                        for (DocumentSnapshot pondDocument: pondQuery) {
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

//    @SuppressLint("NotifyDataSetChanged")
//    private void getDataPlan() {
//        mPlans = new ArrayList<>();
//        planAdapter = new PlanAdapter(mPlans);
////        mBinding.recyclerViewPlan.setAdapter(planAdapter);
//        database.collection(Constants.KEY_COLLECTION_CAMPUS)
//                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
//                .get().addOnSuccessListener(snapShotCampus -> {
//                    for (DocumentSnapshot documentSnapshot : snapShotCampus.getDocuments()) {
//                        database.collection(Constants.KEY_COLLECTION_PLAN)
//                                .whereEqualTo(Constants.KEY_CAMPUS_ID, documentSnapshot.getId())
//                                .get().addOnSuccessListener(queryDocumentSnapshots -> {
//                                    Plan plan = new Plan();
//                                    plan.setPlanId(documentSnapshot.getId());
//                                    plan.setPondName(documentSnapshot.getString(Constants.KEY_NAME));
//                                    plan.setPondId(documentSnapshot.getString(Constants.KEY_POND_ID));
//
//                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
//                                        String acreage = doc.getString(Constants.KEY_ACREAGE);
//                                        int consistence = Objects.requireNonNull(doc.getLong(Constants.KEY_CONSISTENCE)).intValue();
//                                        int numberOfFish = Objects.requireNonNull(doc.getLong(Constants.KEY_NUMBER_OF_FISH)).intValue();
//                                        float survivalRate = Objects.requireNonNull(doc.getDouble(Constants.KEY_SURVIVAL_RATE)).floatValue();
//                                        int numberOfFishAlive = Objects.requireNonNull(doc.getLong(Constants.KEY_NUMBER_OF_FISH_ALIVE)).intValue();
//                                        float harvestSize = Objects.requireNonNull(doc.getDouble(Constants.KEY_HARVEST_SIZE)).floatValue();
//                                        int harvestYield = Objects.requireNonNull(doc.getLong(Constants.KEY_HARVEST_YIELD)).intValue();
//                                        float fcr = Objects.requireNonNull(doc.getDouble(Constants.KEY_FCR)).floatValue();
//                                        int food = Objects.requireNonNull(doc.getLong(Constants.KEY_FOOD)).intValue();
//                                        int fingerlingSamples = Objects.requireNonNull(doc.getLong(Constants.KEY_FINGERLING_SAMPLES)).intValue();
//                                        assert acreage != null;
//                                        plan.setAcreage(Integer.parseInt(plan.getAcreage()) + Integer.parseInt(acreage) + "");
//                                        plan.setConsistence(plan.getConsistence() + consistence);
//                                        plan.setNumberOfFish(plan.getNumberOfFish() + numberOfFish);
//                                        plan.setSurvivalRate(plan.getSurvivalRate() + survivalRate);
//                                        plan.setNumberOfFishAlive(plan.getNumberOfFishAlive() + numberOfFishAlive);
//                                        plan.setHarvestYield(plan.getHarvestYield() + harvestYield);
//                                        plan.setHarvestSize(plan.getHarvestSize() + harvestSize);
//                                        plan.setFcr(plan.getFcr() + fcr);
//                                        plan.setFood(plan.getFood() + food);
//                                        plan.setFingerlingSamples(plan.getFingerlingSamples() + fingerlingSamples);
//                                    }
//                                    if (plan.getNumberOfFish() != 0) {
//                                        mPlans.add(plan);
//                                    }
//                                    planAdapter.notifyDataSetChanged();
//                                });
//                    }
//                });
//
//    }

}