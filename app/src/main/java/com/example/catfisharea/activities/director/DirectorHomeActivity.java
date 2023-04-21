package com.example.catfisharea.activities.director;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import com.android.app.catfisharea.databinding.ActivityDirectorHomeBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.admin.WarehouseActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.activities.alluser.PondDetailsActivity;
import com.example.catfisharea.activities.alluser.TreatmentActivity;
import com.example.catfisharea.activities.alluser.ViewPlanActivity;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class DirectorHomeActivity extends BaseActivity implements CampusListener{
    private ActivityDirectorHomeBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDirectorHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initActivity();
        setListener();
    }

    private void setListener() {
        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes, this);
        mBinding.recyclerViewDirectorHome.setAdapter(homeAdapter);
        getDataHome();

        mBinding.toolbaDirectorHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        mBinding.imageConference.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ConferenceActivity.class)));

        mBinding.imageChat.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ConversationActivity.class)));

        mBinding.imageLogout.setOnClickListener(view -> logOut());

        mBinding.layoutControlDirectorHome.layoutWarehouse.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), WarehouseActivity.class)));

        mBinding.layoutControlDirectorHome.layoutSeason.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewPlanActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlDirectorHome.layoutHR.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), HumanResourceActivity.class)));

        mBinding.layoutControlDirectorHome.layoutTask.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TaskManagerActivity.class)));

        mBinding.layoutControlDirectorHome.layoutRequest.setOnClickListener(view -> {
            Intent intent = new Intent(this, RequestManagementActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlDirectorHome.layoutReportFish.setVisibility(View.VISIBLE);

        mBinding.layoutControlDirectorHome.layoutReportFish.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ReportFishActivity.class)));

        mBinding.layoutControlDirectorHome.layoutChart.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TreatmentActivity.class)));

    }

    private void initActivity() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
    }


    @SuppressLint("NotifyDataSetChanged")
    private void getDataHome() {
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
                    Animatoo.animateSlideLeft(DirectorHomeActivity.this);
                    finish();
                })
                .addOnFailureListener(e -> showToast("Không thể đăng xuất..."));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void OnCampusClicker(RegionModel regionModel) {
        // show dialog campus detail
    }

    @Override
    public void OnPondClicker(RegionModel regionModel) {
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF) ||
                preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
            Intent intent = new Intent(this, PondDetailsActivity.class);
            intent.putExtra(Constants.KEY_POND, regionModel);
            startActivity(intent);
        }
    }

    @Override
    public void onCreatePlan(RegionModel regionModel) {

    }
}