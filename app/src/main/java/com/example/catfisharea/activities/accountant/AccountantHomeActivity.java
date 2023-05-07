package com.example.catfisharea.activities.accountant;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.android.app.catfisharea.databinding.ActivityAccountantHomeBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.admin.WarehouseActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.listeners.CampusListener;
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
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AccountantHomeActivity extends BaseActivity implements CampusListener {
    private ActivityAccountantHomeBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAccountantHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        mBinding.toolbarAdminHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        mBinding.imageLogout.setOnClickListener(view -> logOut());

        mBinding.imageConference.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ConferenceActivity.class));
        });

        mBinding.imageChat.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), ConversationActivity.class));
        });

        mBinding.layoutControlAccountantHome.layoutWarehouse.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), WarehouseActivity.class)));

        mBinding.layoutControlAccountantHome.layoutAccounting.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), AccountingActivity.class));
        });

        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes, this);
        mBinding.recyclerViewAccountingHome.setAdapter(homeAdapter);
        getDataHome();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataHome() {
        String areaId = preferenceManager.getString(Constants.KEY_AREA_ID);
        if (areaId == null) return;
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

    @Override
    public void OnCampusClicker(RegionModel regionModel) {

    }

    @Override
    public void OnPondClicker(RegionModel regionModel) {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, regionModel.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.getString(Constants.KEY_POND_ID) != null) {
                                Intent intent = new Intent(this, AccountingPondActivity.class);
                                intent.putExtra(Constants.KEY_POND, regionModel);
                                intent.putExtra(Constants.KEY_ID_PLAN, queryDocumentSnapshot.getId());
                                startActivity(intent);
                            }
                        }
                    }
                });
    }

    @Override
    public void onCreatePlan(RegionModel regionModel) {

    }
}