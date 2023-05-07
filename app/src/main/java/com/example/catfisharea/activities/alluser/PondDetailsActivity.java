package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityPondDetailsBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.regional_chief.HarvestFishActivity;
import com.example.catfisharea.adapter.MedicineTreatmentUsedAdapter;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Medicine;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.Treatment;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PondDetailsActivity extends BaseActivity implements UserListener, MultipleListener {

    private Pond pond;
    private ActivityPondDetailsBinding binding;
    private FirebaseFirestore database;
    private UsersAdapter usersAdapter;
    private MultipleUserSelectionAdapter multipleUserSelectionAdapter;
    private List<User> users;
    private Treatment treatment;
    private String encodeImageReport;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPondDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    void init() {

        database = FirebaseFirestore.getInstance();

        PreferenceManager preferenceManager = new PreferenceManager(getApplicationContext());

        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
            binding.layoutReleaseFish.setVisibility(View.VISIBLE);
            binding.layoutHarvest.setVisibility(View.VISIBLE);
        }

        binding.layoutHome.btnAddReleaseFish.setVisibility(View.GONE);

        treatment = new Treatment();

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);

        binding.layoutHome.textShowImageReport.setVisibility(View.VISIBLE);
        binding.layoutHome.cardHealth.setVisibility(View.VISIBLE);

        binding.textName.setText(pond.getName());
        binding.textAcreage.setText(pond.getAcreage() + " m\u00b2");

        binding.layoutHome.btnAddWeight.setVisibility(View.GONE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                    .whereEqualTo(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString())
                    .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.getResult() != null && task.isSuccessful()){
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                binding.layoutHome.weight.setText(queryDocumentSnapshot.getString(Constants.KEY_FISH_WEIGH_WEIGHT) + " g/con");
                                binding.layoutHome.loss.setText(queryDocumentSnapshot.getString(Constants.KEY_FISH_WEIGH_LOSS) + " con");
                            }
                        }
                    });
        }

        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.getString(Constants.KEY_POND_ID) != null){
                                binding.textPreparationCos.setText(String.format(Locale.US, "%,d"
                                        , queryDocumentSnapshot.get(Constants.KEY_PREPARATION_COST)) + " VNĐ");
                                binding.textFishModel.setText(queryDocumentSnapshot.get(Constants.KEY_FINGERLING_SAMPLES) + " con/kg");
                                binding.textNumOfFish.setText(String.format(Locale.US, "%,d"
                                        , queryDocumentSnapshot.get(Constants.KEY_NUMBER_OF_FISH)) + " con");
                            }
                        }
                    }
                });

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    int countWorker = 0;
                    for (QueryDocumentSnapshot ignored : task.getResult()){
                        countWorker++;
                    }
                    binding.textNumOfWorker.setText(countWorker + " người");
                });

        setVisibleFood(pond.getNumOfFeeding(), pond.getNumOfFeedingList().get(0), pond.getNumOfFeedingList().get(1),
                pond.getNumOfFeedingList().get(2), pond.getNumOfFeedingList().get(3), pond.getNumOfFeedingList().get(4),
                pond.getNumOfFeedingList().get(5), pond.getNumOfFeedingList().get(6), pond.getNumOfFeedingList().get(7));


        List<String> specificationsToMeasureList = pond.getSpecificationsToMeasureList();
        for (String parameter : specificationsToMeasureList){
            if (parameter.equals(Constants.KEY_SPECIFICATION_PH)){
                binding.layoutHome.environment1.setVisibility(View.VISIBLE);
            }

            if (parameter.equals(Constants.KEY_SPECIFICATION_SALINITY)){
                binding.layoutHome.environment2.setVisibility(View.VISIBLE);
            }

            if (parameter.equals(Constants.KEY_SPECIFICATION_ALKALINITY)){
                binding.layoutHome.environment3.setVisibility(View.VISIBLE);
            }

            if (parameter.equals(Constants.KEY_SPECIFICATION_TEMPERATE)){
                binding.layoutHome.environment4.setVisibility(View.VISIBLE);
            }

            if (parameter.equals(Constants.KEY_SPECIFICATION_H2S)){
                binding.layoutHome.environment5.setVisibility(View.VISIBLE);
            }

            if (parameter.equals(Constants.KEY_SPECIFICATION_NH3)){
                binding.layoutHome.environment6.setVisibility(View.VISIBLE);
            }

        }

        HashMap<String, Object> parameters = pond.getParameters();
        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_PH), "0")){
            binding.layoutHome.textQuantityEnvironment1.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment1.setVisibility(View.GONE);
        } else {
            binding.layoutHome.textQuantityEnvironment1.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment1.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_SALINITY), "0")){
            binding.layoutHome.textQuantityEnvironment2.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment2.setVisibility(View.GONE);
        } else {
            binding.layoutHome.textQuantityEnvironment2.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment2.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_ALKALINITY), "0")){
            binding.layoutHome.textQuantityEnvironment3.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment3.setVisibility(View.GONE);
        } else {
            binding.layoutHome.textQuantityEnvironment3.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment3.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_TEMPERATE), "0")){
            binding.layoutHome.textQuantityEnvironment4.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment4.setVisibility(View.GONE);
        } else {
            binding.layoutHome.textQuantityEnvironment4.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment4.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_H2S), "0")){
            binding.layoutHome.textQuantityEnvironment5.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment5.setVisibility(View.GONE);
        } else {
            binding.layoutHome.textQuantityEnvironment5.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment5.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_NH3), "0")){
            binding.layoutHome.textQuantityEnvironment6.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment6.setVisibility(View.GONE);
        } else {
            binding.layoutHome.textQuantityEnvironment6.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment6.setVisibility(View.VISIBLE);
        }

        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_TREATMENT_POND_ID, pond.getId())
                .whereEqualTo(Constants.KEY_TREATMENT_STATUS, Constants.KEY_TREATMENT_ACCEPT)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        treatment.id = queryDocumentSnapshot.getId();
                        treatment.pondId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_POND_ID);
                        treatment.campusId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CAMPUS_ID);
                        treatment.creatorId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_ID);
                        treatment.creatorName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_NAME);
                        treatment.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_IMAGE);
                        treatment.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_PHONE);
                        if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER) != null){
                            treatment.replaceWater = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                        }
                        if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD) != null){
                            treatment.noFood = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD);
                        }
                        if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD) != null){
                            treatment.suckMud = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD);
                        }
                        if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE) != null){
                            treatment.note = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE);
                        }
                        treatment.date = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE);
                        treatment.sickName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SICK_NAME);
                        treatment.status = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS);
                        treatment.medicines = (HashMap<String, Object>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_MEDICINE);
                        treatment.assignmentStatus = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_ASSIGNMENT_STATUS);
                    }
                })
                .addOnSuccessListener(runnable -> {
                    if (treatment.id != null){
                        binding.layoutHome.cardTreatment.setVisibility(View.VISIBLE);
                        binding.layoutHome.btnComplete.setVisibility(View.GONE);
                        if (treatment.noFood == null || treatment.noFood.equals("")){
                            binding.layoutHome.textNoFood.setVisibility(View.GONE);
                        }

                        if (treatment.replaceWater == null || treatment.replaceWater.equals("")){
                            binding.layoutHome.textReplaceWater.setVisibility(View.GONE);
                        }

                        if (treatment.suckMud == null || treatment.suckMud.equals("")){
                            binding.layoutHome.textSuckMud.setVisibility(View.GONE);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                    .document(treatment.id)
                                    .collection(Constants.KEY_TREATMENT_COLLECTION_IMAGE_EVERYDAY)
                                    .document(LocalDate.now().toString())
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        if (task.getResult() != null){
                                            DocumentSnapshot documentSnapshot = task.getResult();
                                            encodeImageReport = documentSnapshot.getString(Constants.KEY_TREATMENT_IMAGE_REPORT);
                                        }
                                    });
                            List<Medicine> medicines = new ArrayList<>();
                            List<String> quantityList = new ArrayList<>();
                            MedicineTreatmentUsedAdapter medicineAdapter = new MedicineTreatmentUsedAdapter(medicines, quantityList);
                            LinearLayoutManager layoutManager
                                    = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                            binding.layoutHome.medicineRecyclerView.setAdapter(medicineAdapter);
                            binding.layoutHome.medicineRecyclerView.setLayoutManager(layoutManager);
                            treatment.medicines.forEach((key, value) ->
                                    database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                            .whereEqualTo(Constants.KEY_POND_ID, treatment.pondId)
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                    database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                            .document(queryDocumentSnapshot.getId())
                                                            .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                            .document(key)
                                                            .get()
                                                            .addOnCompleteListener(task1 -> {
                                                                DocumentSnapshot documentSnapshot = task1.getResult();
                                                                Medicine medicine = new Medicine();
                                                                medicine.id = documentSnapshot.getId();
                                                                medicine.amount = documentSnapshot.getString(Constants.KEY_AMOUNT_OF_ROOM);
                                                                medicine.effect = documentSnapshot.getString(Constants.KEY_EFFECT);
                                                                medicine.producer = documentSnapshot.getString(Constants.KEY_PRODUCER);
                                                                medicine.type = documentSnapshot.getString(Constants.KEY_CATEGORY_TYPE);
                                                                medicine.name = documentSnapshot.getString(Constants.KEY_NAME);
                                                                medicine.unit = documentSnapshot.getString(Constants.KEY_UNIT);
                                                                medicines.add(medicine);
                                                                quantityList.add(value.toString());
                                                                medicineAdapter.notifyDataSetChanged();
                                                            });
                                                }
                                            })
                            );
                        }

                    } else {
                        binding.layoutHome.cardTreatment.setVisibility(View.GONE);
                    }

                });

        database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                .whereEqualTo(Constants.KEY_RELEASE_FISH_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            if (queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_POND_ID) != null){
                                if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_POND_ID), pond.getId())){
                                    binding.layoutHome.cardReleaseFish.setVisibility(View.VISIBLE);
                                    binding.layoutHome.textNeedToRelease.setText(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_AMOUNT) + " con");
                                    binding.layoutHome.textReleaseFish.setText(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_AMOUNT_RELEASE) + " con");
                                }
                            }
                        }
                    }
                });
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> onBackPressed());

        binding.layoutShowWorker.setOnClickListener(view -> openShowWorkerDialog());

        binding.layoutSettingFeed.setOnClickListener(view -> openSettingNumOfFeedingDialog());

        binding.layoutSettingWater.setOnClickListener(view -> openSettingNumOfWateringDialog());

        binding.layoutHome.textShowImageReport.setOnClickListener(view -> openReportImageOfToDayDialog());

        binding.cardReleaseFish.setOnClickListener(view ->
                database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                .whereEqualTo(Constants.KEY_RELEASE_FISH_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    boolean isAvailable = false;
                   if (task.isSuccessful()){
                       for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                           isAvailable = Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_STATUS), Constants.KEY_RELEASE_FISH_UNCOMPLETED);
                       }
                   }
                   if (isAvailable){
                       showToast("Thả giống vẫn chưa được hoàn thành!");
                   } else {
                       openSettingReleaseFishDialog();
                   }
                }));

        binding.layoutHarvest.setOnClickListener(view -> {
            Intent intent = new Intent(this, HarvestFishActivity.class);
            intent.putExtra(Constants.KEY_POND, pond);
            startActivity(intent);
        });

        binding.textDetail.setOnClickListener(view -> {
            if (binding.layoutInfoDetail.getVisibility() == View.VISIBLE){
                binding.layoutInfoDetail.setVisibility(View.GONE);
                binding.imageDetail.setImageResource(R.drawable.ic_down);
            } else {
                binding.layoutInfoDetail.setVisibility(View.VISIBLE);
                binding.imageDetail.setImageResource(R.drawable.ic_up);
            }
        });

        binding.imageDetail.setOnClickListener(view -> {
            if (binding.layoutInfoDetail.getVisibility() == View.VISIBLE){
                binding.layoutInfoDetail.setVisibility(View.GONE);
                binding.imageDetail.setImageResource(R.drawable.ic_up);
            } else {
                binding.layoutInfoDetail.setVisibility(View.VISIBLE);
                binding.imageDetail.setImageResource(R.drawable.ic_down);
            }
        });
    }


    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void openSettingReleaseFishDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_setting_release_fish);
        assert dialog != null;

        AppCompatButton btnCreate, btnClose;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnCreate = dialog.findViewById(R.id.btnCreate);

        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        TextInputEditText edtNumOfFish, edtModel, edtPrice;
        edtNumOfFish = dialog.findViewById(R.id.edtNumOfFish);
        edtModel = dialog.findViewById(R.id.edtModel);
        edtPrice = dialog.findViewById(R.id.edtPrice);

        multipleUserSelectionAdapter = new MultipleUserSelectionAdapter(users, this);
        userRecyclerView.setAdapter(multipleUserSelectionAdapter);

        users.clear();

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            multipleUserSelectionAdapter.notifyDataSetChanged();
                        }
                    }
                });

        btnCreate.setOnClickListener(view -> {
            List<User> selectedUser = multipleUserSelectionAdapter.getSelectedUser();
            if (selectedUser.size() == 0){
                showToast("Vui lòng chọn ít nhất một công nhân!");
            } else {
                if (Objects.requireNonNull(edtNumOfFish.getText()).toString().equals("")
                    || Objects.requireNonNull(edtModel.getText()).toString().equals("")
                    || Objects.requireNonNull(edtPrice.getText()).toString().equals("")){
                    showToast("Vui lòng chọn nhập đầy đủ thông tin");
                } else {
                    HashMap<String, Object> releaseFist = new HashMap<>();
                    List<String> workerIds = new ArrayList<>();
                    for (User user : selectedUser){
                        workerIds.add(user.id);
                    }

                    database.collection(Constants.KEY_COLLECTION_PLAN)
                            .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                            .get()
                            .addOnCompleteListener(task -> {

                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                                        releaseFist.put(Constants.KEY_RELEASE_FISH_PLAN_ID, queryDocumentSnapshot.getId());
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_AMOUNT, Objects.requireNonNull(edtNumOfFish.getText()).toString());
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                            releaseFist.put(Constants.KEY_RELEASE_FISH_DATE, LocalDate.now().toString());
                                        }
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_WORKER_ID_ASSIGN, workerIds);
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_AMOUNT_RELEASE, "0");
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_POND_ID, pond.getId());
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_STATUS, Constants.KEY_RELEASE_FISH_UNCOMPLETED);
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_CREATED_AT, new Date());
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_MODEL, Objects.requireNonNull(edtModel.getText()).toString());
                                        releaseFist.put(Constants.KEY_RELEASE_FISH_PRICE, Objects.requireNonNull(edtPrice.getText()).toString());

                                        database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                                                .document(new Date().toString())
                                                .set(releaseFist)
                                                .addOnSuccessListener(runnable -> {
                                                    showToast("Đã tạo nhiệm vụ thả giống cho công nhân");
                                                    dialog.dismiss();
                                                })
                                                .addOnFailureListener(runnable -> showToast("Tạo nhiệm vụ thả giống cho công nhân thất bại"));

                                        binding.layoutHome.cardReleaseFish.setVisibility(View.VISIBLE);
                                        binding.layoutHome.textNeedToRelease.setText(edtNumOfFish.getText().toString() + " con");
                                        binding.layoutHome.textReleaseFish.setText("0 con");

                                    }
                                }


                            });
                }
            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void setVisibleFood(int numOfFeed, String st_1, String st_2, String st_3, String st_4, String st_5, String st_6, String st_7, String st_8){
        if (numOfFeed == 1){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.GONE);
            binding.layoutHome.food3.setVisibility(View.GONE);
            binding.layoutHome.food4.setVisibility(View.GONE);
            binding.layoutHome.food5.setVisibility(View.GONE);
            binding.layoutHome.food6.setVisibility(View.GONE);
            binding.layoutHome.food7.setVisibility(View.GONE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 2){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.GONE);
            binding.layoutHome.food4.setVisibility(View.GONE);
            binding.layoutHome.food5.setVisibility(View.GONE);
            binding.layoutHome.food6.setVisibility(View.GONE);
            binding.layoutHome.food7.setVisibility(View.GONE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 3){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.GONE);
            binding.layoutHome.food5.setVisibility(View.GONE);
            binding.layoutHome.food6.setVisibility(View.GONE);
            binding.layoutHome.food7.setVisibility(View.GONE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 4){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.GONE);
            binding.layoutHome.food6.setVisibility(View.GONE);
            binding.layoutHome.food7.setVisibility(View.GONE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 5){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.GONE);
            binding.layoutHome.food7.setVisibility(View.GONE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 6){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.VISIBLE);
            binding.layoutHome.food7.setVisibility(View.GONE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 7){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.VISIBLE);
            binding.layoutHome.food7.setVisibility(View.VISIBLE);
            binding.layoutHome.food8.setVisibility(View.GONE);
        } else if (numOfFeed == 8){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.VISIBLE);
            binding.layoutHome.food7.setVisibility(View.VISIBLE);
            binding.layoutHome.food8.setVisibility(View.VISIBLE);
        }
        binding.layoutHome.textQuantityFood1.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood2.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood3.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood4.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood5.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood6.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood7.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood8.setVisibility(View.VISIBLE);
        binding.layoutHome.textQuantityFood1.setText(pond.getAmountFeedList().get(0));
        binding.layoutHome.textQuantityFood2.setText(pond.getAmountFeedList().get(1));
        binding.layoutHome.textQuantityFood3.setText(pond.getAmountFeedList().get(2));
        binding.layoutHome.textQuantityFood4.setText(pond.getAmountFeedList().get(3));
        binding.layoutHome.textQuantityFood5.setText(pond.getAmountFeedList().get(4));
        binding.layoutHome.textQuantityFood6.setText(pond.getAmountFeedList().get(5));
        binding.layoutHome.textQuantityFood7.setText(pond.getAmountFeedList().get(6));
        binding.layoutHome.textQuantityFood8.setText(pond.getAmountFeedList().get(7));
        binding.layoutHome.textFood1.setText("Lần 1: " + st_1);
        binding.layoutHome.textFood2.setText("Lần 2: " + st_2);
        binding.layoutHome.textFood3.setText("Lần 3: " + st_3);
        binding.layoutHome.textFood4.setText("Lần 4: " + st_4);
        binding.layoutHome.textFood5.setText("Lần 5: " + st_5);
        binding.layoutHome.textFood6.setText("Lần 6: " + st_6);
        binding.layoutHome.textFood7.setText("Lần 7: " + st_7);
        binding.layoutHome.textFood8.setText("Lần 8: " + st_8);
    }

    private void openReportImageOfToDayDialog() {

        if (encodeImageReport != null){
            Dialog dialog = openDialog(R.layout.layout_dialog_show_report_image_treatment_today);
            assert dialog != null;

            PhotoView imageReportImage = dialog.findViewById(R.id.imageReportImage);
            AppCompatButton btnClose = dialog.findViewById(R.id.btnClose);

            imageReportImage.setImageBitmap(getImage(encodeImageReport));

            btnClose.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        } else {
            showToast("Ảnh chưa được cập nhật!");
        }

    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openSettingNumOfFeedingDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_setting_num_of_feeding);
        assert dialog != null;

        Spinner spinnerNumOfFeed = dialog.findViewById(R.id.spinnerNumOfFeed);
        TextInputLayout textOne, textTwo, textThree, textFour, textFive, textSix, textSeven, textEight;
        AppCompatButton btnClose, btnSave;
        TextInputEditText edtOne, edtTwo, edtThree, edtFour, edtFive, edtSix, edtSeven, edtEight;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnSave = dialog.findViewById(R.id.btnSave);

        textOne = dialog.findViewById(R.id.textOne);
        textTwo = dialog.findViewById(R.id.textTwo);
        textThree = dialog.findViewById(R.id.textThree);
        textFour = dialog.findViewById(R.id.textFour);
        textFive = dialog.findViewById(R.id.textFive);
        textSix = dialog.findViewById(R.id.textSix);
        textSeven = dialog.findViewById(R.id.textSeven);
        textEight = dialog.findViewById(R.id.textEight);

        edtOne = dialog.findViewById(R.id.edtOne);
        edtTwo = dialog.findViewById(R.id.edtTwo);
        edtThree = dialog.findViewById(R.id.edtThree);
        edtFour = dialog.findViewById(R.id.edtFour);
        edtFive = dialog.findViewById(R.id.edtFive);
        edtSix = dialog.findViewById(R.id.edtSix);
        edtSeven = dialog.findViewById(R.id.edtSeven);
        edtEight = dialog.findViewById(R.id.edtEight);

        List<String> numOfFeedList = new ArrayList<>();
        for (int i = 0; i < 9; i++){
            numOfFeedList.add(i + "");
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, numOfFeedList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerNumOfFeed.setAdapter(adapter);

        if (pond.getNumOfFeeding() == 1) {
            spinnerNumOfFeed.setSelection(1);
            textOne.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
        } else if (pond.getNumOfFeeding() == 2) {
            spinnerNumOfFeed.setSelection(2);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
        } else if (pond.getNumOfFeeding() == 3) {
            spinnerNumOfFeed.setSelection(3);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
            edtThree.setText(pond.getNumOfFeedingList().get(2) + "");
        } else if (pond.getNumOfFeeding() == 4) {
            spinnerNumOfFeed.setSelection(4);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
            edtThree.setText(pond.getNumOfFeedingList().get(2) + "");
            edtFour.setText(pond.getNumOfFeedingList().get(3) + "");
        } else if (pond.getNumOfFeeding() == 5) {
            spinnerNumOfFeed.setSelection(5);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
            edtThree.setText(pond.getNumOfFeedingList().get(2) + "");
            edtFour.setText(pond.getNumOfFeedingList().get(3) + "");
            edtFive.setText(pond.getNumOfFeedingList().get(4) + "");
        } else if (pond.getNumOfFeeding() == 6) {
            spinnerNumOfFeed.setSelection(6);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            textSix.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
            edtThree.setText(pond.getNumOfFeedingList().get(2) + "");
            edtFour.setText(pond.getNumOfFeedingList().get(3) + "");
            edtFive.setText(pond.getNumOfFeedingList().get(4) + "");
            edtSix.setText(pond.getNumOfFeedingList().get(5) + "");
        } else if (pond.getNumOfFeeding() == 7) {
            spinnerNumOfFeed.setSelection(7);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            textSix.setVisibility(View.VISIBLE);
            textSeven.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
            edtThree.setText(pond.getNumOfFeedingList().get(2) + "");
            edtFour.setText(pond.getNumOfFeedingList().get(3) + "");
            edtFive.setText(pond.getNumOfFeedingList().get(4) + "");
            edtSix.setText(pond.getNumOfFeedingList().get(5) + "");
            edtSeven.setText(pond.getNumOfFeedingList().get(6) + "");
        } else if (pond.getNumOfFeeding() == 8) {
            spinnerNumOfFeed.setSelection(8);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            textSix.setVisibility(View.VISIBLE);
            textSeven.setVisibility(View.VISIBLE);
            textEight.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getNumOfFeedingList().get(0) + "");
            edtTwo.setText(pond.getNumOfFeedingList().get(1) + "");
            edtThree.setText(pond.getNumOfFeedingList().get(2) + "");
            edtFour.setText(pond.getNumOfFeedingList().get(3) + "");
            edtFive.setText(pond.getNumOfFeedingList().get(4) + "");
            edtSix.setText(pond.getNumOfFeedingList().get(5) + "");
            edtSeven.setText(pond.getNumOfFeedingList().get(6) + "");
            edtEight.setText(pond.getNumOfFeedingList().get(7) + "");
        } else {
            spinnerNumOfFeed.setSelection(0);
        }

        spinnerNumOfFeed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0){
                    textOne.setVisibility(View.GONE);
                    textTwo.setVisibility(View.GONE);
                    textThree.setVisibility(View.GONE);
                    textFour.setVisibility(View.GONE);
                    textFive.setVisibility(View.GONE);
                    textSix.setVisibility(View.GONE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 1){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.GONE);
                    textThree.setVisibility(View.GONE);
                    textFour.setVisibility(View.GONE);
                    textFive.setVisibility(View.GONE);
                    textSix.setVisibility(View.GONE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 2){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.GONE);
                    textFour.setVisibility(View.GONE);
                    textFive.setVisibility(View.GONE);
                    textSix.setVisibility(View.GONE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 3){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.VISIBLE);
                    textFour.setVisibility(View.GONE);
                    textFive.setVisibility(View.GONE);
                    textSix.setVisibility(View.GONE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 4){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.VISIBLE);
                    textFour.setVisibility(View.VISIBLE);
                    textFive.setVisibility(View.GONE);
                    textSix.setVisibility(View.GONE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 5){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.VISIBLE);
                    textFour.setVisibility(View.VISIBLE);
                    textFive.setVisibility(View.VISIBLE);
                    textSix.setVisibility(View.GONE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 6){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.VISIBLE);
                    textFour.setVisibility(View.VISIBLE);
                    textFive.setVisibility(View.VISIBLE);
                    textSix.setVisibility(View.VISIBLE);
                    textSeven.setVisibility(View.GONE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 7){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.VISIBLE);
                    textFour.setVisibility(View.VISIBLE);
                    textFive.setVisibility(View.VISIBLE);
                    textSix.setVisibility(View.VISIBLE);
                    textSeven.setVisibility(View.VISIBLE);
                    textEight.setVisibility(View.GONE);
                } else if (i == 8){
                    textOne.setVisibility(View.VISIBLE);
                    textTwo.setVisibility(View.VISIBLE);
                    textThree.setVisibility(View.VISIBLE);
                    textFour.setVisibility(View.VISIBLE);
                    textFive.setVisibility(View.VISIBLE);
                    textSix.setVisibility(View.VISIBLE);
                    textSeven.setVisibility(View.VISIBLE);
                    textEight.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        btnSave.setOnClickListener(view -> {

            boolean isValidData = true;
            List<String> numOfFeedingList = new ArrayList<>();

            int numOfFeeding;
            if (spinnerNumOfFeed.getSelectedItem().equals("0")){
                numOfFeeding = 0;
            } else if (spinnerNumOfFeed.getSelectedItem().equals("1")){
                numOfFeeding = 1;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("2")){
                numOfFeeding = 2;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("3")){
                numOfFeeding = 3;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtThree.getText()).toString());
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("4")){
                numOfFeeding = 4;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtThree.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFour.getText()).toString());
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("5")){
                numOfFeeding = 5;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtThree.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFour.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFive.getText()).toString());
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("6")){
                numOfFeeding = 6;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtThree.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFour.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFive.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtSix.getText()).toString());
                    numOfFeedingList.add(0 + "");
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("7")){
                numOfFeeding = 7;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtThree.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFour.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFive.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtSix.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtSeven.getText()).toString());
                    numOfFeedingList.add(0 + "");
                } else {
                    isValidData = false;
                }
            } else {
                numOfFeeding = 8;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    numOfFeedingList.add(Objects.requireNonNull(edtOne.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtTwo.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtThree.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFour.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtFive.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtSix.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtSeven.getText()).toString());
                    numOfFeedingList.add(Objects.requireNonNull(edtEight.getText()).toString());
                } else {
                    isValidData = false;
                }
            }
            if (isValidData) {
                HashMap<String, Object> update = new HashMap<>();
                update.put(Constants.KEY_NUM_OF_FEEDING, numOfFeeding + "");
                update.put(Constants.KEY_NUM_OF_FEEDING_LIST, numOfFeedingList);

                database.collection(Constants.KEY_COLLECTION_POND)
                        .document(pond.getId())
                        .update(update)
                        .addOnSuccessListener(unused -> {
                            showToast("Cập nhật số lần cho ăn trong ngày thành công!");
                            setVisibleFood(numOfFeeding, numOfFeedingList.get(0), numOfFeedingList.get(1),
                                    numOfFeedingList.get(2), numOfFeedingList.get(3), numOfFeedingList.get(4),
                                    numOfFeedingList.get(5), numOfFeedingList.get(6), numOfFeedingList.get(7));
                            dialog.dismiss();
                        })
                        .addOnFailureListener(e -> {
                            showToast("Cập nhật số lần ăn thất bại!");
                            dialog.dismiss();
                        });
            } else {
                showToast("Vui lòng nhập đầy đủ thông tin!");
            }

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openSettingNumOfWateringDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_setting_num_of_watering);
        assert dialog != null;

        AppCompatButton btnClose, btnSave;
        CheckBox checkboxPH, checkboxSalinity, checkboxAlkalinity, checkboxTemperate, checkboxH2S, checkboxNH3;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnSave = dialog.findViewById(R.id.btnSave);

        checkboxPH = dialog.findViewById(R.id.checkboxPH);
        checkboxSalinity = dialog.findViewById(R.id.checkboxSalinity);
        checkboxAlkalinity = dialog.findViewById(R.id.checkboxAlkalinity);
        checkboxTemperate = dialog.findViewById(R.id.checkboxTemperate);
        checkboxH2S = dialog.findViewById(R.id.checkboxH2S);
        checkboxNH3 = dialog.findViewById(R.id.checkboxNH3);

        List<String> spec = pond.getSpecificationsToMeasureList();

        if (spec.contains(Constants.KEY_SPECIFICATION_PH)){
            checkboxPH.setChecked(true);
        }

        if (spec.contains(Constants.KEY_SPECIFICATION_SALINITY)){
            checkboxSalinity.setChecked(true);
        }

        if (spec.contains(Constants.KEY_SPECIFICATION_ALKALINITY)){
            checkboxAlkalinity.setChecked(true);
        }

        if (spec.contains(Constants.KEY_SPECIFICATION_TEMPERATE)){
            checkboxTemperate.setChecked(true);
        }

        if (spec.contains(Constants.KEY_SPECIFICATION_H2S)){
            checkboxH2S.setChecked(true);
        }

        if (spec.contains(Constants.KEY_SPECIFICATION_NH3)){
            checkboxNH3.setChecked(true);
        }

        btnSave.setOnClickListener(view -> {
            if (checkboxPH.isChecked()){
                binding.layoutHome.environment1.setVisibility(View.VISIBLE);
                spec.set(0, Constants.KEY_SPECIFICATION_PH);
            } else {
                binding.layoutHome.environment1.setVisibility(View.GONE);
                spec.set(0, "0");
            }

            if (checkboxSalinity.isChecked()){
                binding.layoutHome.environment2.setVisibility(View.VISIBLE);
                spec.set(1, Constants.KEY_SPECIFICATION_SALINITY);
            } else {
                binding.layoutHome.environment2.setVisibility(View.GONE);
                spec.set(1, "0");
            }

            if (checkboxAlkalinity.isChecked()){
                binding.layoutHome.environment3.setVisibility(View.VISIBLE);
                spec.set(2, Constants.KEY_SPECIFICATION_ALKALINITY);
            } else {
                binding.layoutHome.environment3.setVisibility(View.GONE);
                spec.set(2, "0");
            }

            if (checkboxTemperate.isChecked()){
                binding.layoutHome.environment4.setVisibility(View.VISIBLE);
                spec.set(3, Constants.KEY_SPECIFICATION_TEMPERATE);
            } else {
                binding.layoutHome.environment4.setVisibility(View.GONE);
                spec.set(3, "0");
            }

            if (checkboxH2S.isChecked()){
                binding.layoutHome.environment5.setVisibility(View.VISIBLE);
                spec.set(4, Constants.KEY_SPECIFICATION_H2S);
            } else {
                binding.layoutHome.environment5.setVisibility(View.GONE);
                spec.set(4, "0");
            }

            if (checkboxNH3.isChecked()){
                binding.layoutHome.environment6.setVisibility(View.VISIBLE);
                spec.set(5, Constants.KEY_SPECIFICATION_NH3);
            } else {
                binding.layoutHome.environment6.setVisibility(View.GONE);
                spec.set(5, "0");
            }

            HashMap<String, Object> updatedList = new HashMap<>();
            updatedList.put(Constants.KEY_SPECIFICATIONS_TO_MEASURE, spec);

            database.collection(Constants.KEY_COLLECTION_POND)
                    .document(pond.getId())
                    .update(updatedList)
                    .addOnSuccessListener(unused -> {
                        showToast("Đã cập nhật các thông số cần đo thành công!");
                        dialog.dismiss();
                    })
                    .addOnFailureListener(runnable -> showToast("Cập nhật các thông số cần đo thất bại!"));

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }


    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openShowWorkerDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_show_worker_in_pond);
        assert dialog != null;

        TextView textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText("Danh sách công nhân làm việc tại ao " + pond.getName());

        AppCompatButton btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> dialog.dismiss());

        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);

        userRecyclerView.setAdapter(usersAdapter);

        users.clear();

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            usersAdapter.notifyDataSetChanged();
                        }
                    }
                });
        dialog.show();
    }

    private Bitmap getImage(String encodedImage){
        byte[] bytes = new byte[0];
        if (encodedImage != null){
            bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
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

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserClicker(User user) {

    }

    @Override
    public void onMultipleUserSelection(Boolean isSelected) {

    }

    @Override
    public void onChangeTeamLeadClicker(User user) {

    }

    @Override
    public void onTaskClicker(Task task) {

    }

    @Override
    public void onTaskSelectedClicker(Boolean isSelected, Boolean isMultipleSelection) {

    }
}