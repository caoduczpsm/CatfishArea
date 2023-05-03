package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityDiaryBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.DiaryPickAdapter;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.listeners.DiaryListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.DiaryPickItem;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DiaryActivity extends BaseActivity implements CampusListener, DiaryListener {
    private ActivityDiaryBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;
    private Dialog dialog;
    private List<DiaryPickItem> itemList;
    private DiaryPickAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDiaryBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        mBinding.toolbarDiary.setNavigationOnClickListener(view -> onBackPressed());
        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes, this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mBinding.recyclerviewDiary.setLayoutManager(linearLayoutManager);
        mBinding.recyclerviewDiary.setAdapter(homeAdapter);

        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        if (type.equals(Constants.KEY_ADMIN)) {
            getDataHomeAdmin();
        } else if (type.equals(Constants.KEY_REGIONAL_CHIEF)) {
            getDataHomeRegionalChief();
        } else if (type.equals(Constants.KEY_DIRECTOR)) {
            getDataHomeDirector();
        }
    }

    private void openPickDiary(String pondId) {
        dialog = openDialog(R.layout.layout_dialog_pick_diary);
        assert dialog != null;

        AppCompatButton no_btn = dialog.findViewById(R.id.btnClose);
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerPickDiary);
        itemList = new ArrayList<>();
        adapter = new DiaryPickAdapter(itemList, this);
        no_btn.setOnClickListener(view -> dialog.dismiss());
        recyclerView.setAdapter(adapter);
        getDiary(pondId);
        dialog.show();
    }

    private void getDiary(String pondId) {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, pondId)
                .get().addOnSuccessListener(diaryQuery -> {
                   for (DocumentSnapshot diaryDoc: diaryQuery.getDocuments()) {
                       Timestamp dateStart = diaryDoc.getTimestamp(Constants.KEY_DATE_OF_PLAN);
                       Timestamp dateEnd = diaryDoc.getTimestamp(Constants.KEY_DATE_HARVEST);
                       DiaryPickItem item = new DiaryPickItem();
                       item.dateStart = DecimalHelper.formatDate(dateStart.toDate());
                       item.dateEnd = DecimalHelper.formatDate(dateEnd.toDate());
                       item.id = diaryDoc.getId();
                       itemList.add(item);
                   }
                   adapter.notifyDataSetChanged();
                });
    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        return dialog;
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
                    Collections.sort(regionModels,
                            (o1, o2) -> (o1.getName()
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
        openPickDiary(regionModel.getId());
    }

    @Override
    public void onCreatePlan(RegionModel regionModel) {

    }

    @Override
    public void clickDiary(DiaryPickItem item) {
        Intent intent = new Intent(this, DiaryDetailActivity.class);
        intent.putExtra(Constants.KEY_ID_PLAN, item.id);
        startActivity(intent);
    }
}