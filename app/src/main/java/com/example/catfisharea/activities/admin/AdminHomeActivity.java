package com.example.catfisharea.activities.admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityAdminHomeBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.DetailPlanActivity;
import com.example.catfisharea.activities.alluser.DiaryActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.activities.alluser.PondDetailsActivity;
import com.example.catfisharea.activities.alluser.ViewPlanActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.models.Area;
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

public class AdminHomeActivity extends BaseActivity implements CampusListener {

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

    @SuppressLint("LogNotTimber")
    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes, this);
        mBinding.recyclerViewAdminHome.setAdapter(homeAdapter);
        getDataHome();

        mBinding.toolbarAdminHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        mBinding.imageLogout.setOnClickListener(view -> logOut());

        mBinding.imageConference.setOnClickListener(view -> {
            Log.d("Action chat", "call");
            startActivity(new Intent(getApplicationContext(), ConferenceActivity.class));
        });

        mBinding.imageChat.setOnClickListener(view -> {
            Log.d("Action chat", "call");
            startActivity(new Intent(getApplicationContext(), ConversationActivity.class));
        });

        mBinding.layoutControlAdminHome.layoutSeason.setOnClickListener(view -> {
            Intent intent = new Intent(this, ViewPlanActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlAdminHome.layoutDiary.setOnClickListener(view -> {
            Intent intent = new Intent(this, DiaryActivity.class);
            startActivity(intent);
        });

        mBinding.layoutControlAdminHome.layoutAccount.setOnClickListener(view -> openDialogAccount());

        mBinding.layoutControlAdminHome.layoutArea.setOnClickListener(view -> openDialogArea());

        mBinding.layoutControlAdminHome.layoutWarehouse.setOnClickListener(view ->
                startActivity(new Intent(getApplicationContext(), WarehouseActivity.class)));

    }

    private void openDialogAccount() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_options_account);
        assert dialog != null;

        ConstraintLayout layoutCreate = dialog.findViewById(R.id.layoutCreate);
        ConstraintLayout layoutChange = dialog.findViewById(R.id.layoutChange);
        ConstraintLayout layoutDelete = dialog.findViewById(R.id.layoutDelete);
        AppCompatButton btnClose = dialog.findViewById(R.id.btnClose);

        layoutCreate.setOnClickListener(view -> {
            selectOptionToCreateAccount();
            dialog.dismiss();
        });

        layoutChange.setOnClickListener(view -> {
            Intent intent = new Intent(this, PermissionEditActivity.class);
            intent.putExtra("typeActivity", Constants.KEY_CAMPUS);
            startActivity(intent);
            dialog.dismiss();
        });

        layoutDelete.setOnClickListener(view -> {
            Intent intent = new Intent(this, DeleteAccountActivity.class);
            intent.putExtra("typeActivity", Constants.KEY_POND);
            startActivity(intent);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void selectOptionToCreateAccount() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_select_option_create_account);
        assert dialog != null;

        //Button trong dialog
        AppCompatButton no_btn = dialog.findViewById(R.id.btnClose);

        //ConstrainLayout trong dialog
        ConstraintLayout layoutALotAccount = dialog.findViewById(R.id.layoutALotAccount);
        ConstraintLayout layoutOneAccount = dialog.findViewById(R.id.layoutOneAccount);

        layoutALotAccount.setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), CreateMultipleAccountActivity.class);
            Animatoo.animateSlideLeft(AdminHomeActivity.this);
            startActivity(intent);
        });

        layoutOneAccount.setOnClickListener(view -> {
            dialog.dismiss();
            Intent intent = new Intent(getApplicationContext(), CreateSimpleAccountActivity.class);
            Animatoo.animateSlideLeft(AdminHomeActivity.this);
            startActivity(intent);
        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void openDialogArea() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_options_area);
        assert dialog != null;

        ConstraintLayout layoutArea = dialog.findViewById(R.id.layoutArea);
        ConstraintLayout layoutCampus = dialog.findViewById(R.id.layoutCampus);
        ConstraintLayout layoutPond = dialog.findViewById(R.id.layoutPond);
        AppCompatButton btnClose = dialog.findViewById(R.id.btnClose);

        layoutArea.setOnClickListener(view -> {
            Intent intent = new Intent(this, AreaManagementActivity.class);
            intent.putExtra("typeActivity", Constants.KEY_AREA);
            startActivity(intent);
            dialog.dismiss();
        });

        layoutCampus.setOnClickListener(view -> {
            Intent intent = new Intent(this, AreaManagementActivity.class);
            intent.putExtra("typeActivity", Constants.KEY_CAMPUS);
            startActivity(intent);
            dialog.dismiss();
        });

        layoutPond.setOnClickListener(view -> {
            Intent intent = new Intent(this, AreaManagementActivity.class);
            intent.putExtra("typeActivity", Constants.KEY_POND);
            startActivity(intent);
            dialog.dismiss();
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();

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
//        Intent intent = new Intent(this, PondDetailsActivity.class);
//        intent.putExtra(Constants.KEY_POND, regionModel);
//        startActivity(intent);
    }

    @Override
    public void onCreatePlan(RegionModel regionModel) {

    }
}