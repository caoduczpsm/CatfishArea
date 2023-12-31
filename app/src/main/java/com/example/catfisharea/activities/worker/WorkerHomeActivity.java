package com.example.catfisharea.activities.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWorkerHomeBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.AIActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.activities.director.RequestManagementActivity;
import com.example.catfisharea.activities.director.TaskManagerActivity;
import com.example.catfisharea.adapter.MedicineTreatmentUsedAdapter;
import com.example.catfisharea.models.Medicine;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.Treatment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class WorkerHomeActivity extends BaseActivity {

    private ActivityWorkerHomeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Pond pond;
    private Task feedTask, measureTask, scaleTask;
    private ImageView imageReason;
    private String encodedImage, encodedImageTreatment;
    private Treatment treatment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
        setListener();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void init(){
        preferenceManager = new PreferenceManager(this);
        preferenceManager.putString(Constants.KEY_NOW, "abc");
        database = FirebaseFirestore.getInstance();

        binding.layoutHome.textSelectImage.setVisibility(View.VISIBLE);

        preferenceManager.putString(Constants.KEY_MEDICINES_PRICE_PRICE, "0");


        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .whereEqualTo(Constants.KEY_TASK_TITLE, Constants.KEY_FIXED_TASK_FEED_FISH)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        List<String> receiverFeedFishTask = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                        assert receiverFeedFishTask != null;
                        if (receiverFeedFishTask.contains(preferenceManager.getString(Constants.KEY_USER_ID))){
                            feedTask = new Task();
                            feedTask.id = queryDocumentSnapshot.getId();
                            binding.layoutHome.cardFood.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutHome.cardFood.setVisibility(View.GONE);
                        }

                    }
                });

        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .whereEqualTo(Constants.KEY_TASK_TITLE, Constants.KEY_FIXED_TASK_MEASURE_WATER)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        List<String> receiverFeedFishTask = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                        assert receiverFeedFishTask != null;
                        if (receiverFeedFishTask.contains(preferenceManager.getString(Constants.KEY_USER_ID))){
                            measureTask = new Task();
                            measureTask.id = queryDocumentSnapshot.getId();
                            binding.layoutHome.cardEnvironment.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutHome.cardEnvironment.setVisibility(View.GONE);
                        }

                    }
                });

        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .whereEqualTo(Constants.KEY_TASK_TITLE, Constants.KEY_FIXED_TASK_FISH_SCALES)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (task.getResult() != null){
                            List<String> receiverFeedFishTask = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                            assert receiverFeedFishTask != null;
                            if (receiverFeedFishTask.contains(preferenceManager.getString(Constants.KEY_USER_ID))){
                                binding.layoutHome.btnAddWeight.setVisibility(View.VISIBLE);
                                scaleTask = new Task();
                                scaleTask.id = queryDocumentSnapshot.getId();
                                scaleTask.status = queryDocumentSnapshot.getString(Constants.KEY_STATUS_TASK);
                                binding.layoutHome.cardHealth.setVisibility(View.VISIBLE);
                            } else {
                                binding.layoutHome.cardHealth.setVisibility(View.GONE);
                            }
                        }
                    }
                })
                .addOnSuccessListener(runnable -> {
                    if (scaleTask != null) {
                        if (scaleTask.status.equals(Constants.KEY_COMPLETED)){
                            binding.layoutHome.btnAddWeight.setVisibility(View.GONE);
                        } else {
                            binding.layoutHome.btnAddWeight.setVisibility(View.VISIBLE);
                        }

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (preferenceManager.getString(Constants.KEY_NOW) == null){
                                preferenceManager.putString(Constants.KEY_NOW, LocalDate.now().toString());
                            } else {
                                if (!preferenceManager.getString(Constants.KEY_NOW).equals(LocalDate.now().toString())) {
                                    HashMap<String, Object> unCompleteTask = new HashMap<>();
                                    unCompleteTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);
                                    database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                            .document(scaleTask.id)
                                            .update(unCompleteTask)
                                            .addOnSuccessListener(runnable1 -> {
                                                binding.layoutHome.btnAddWeight.setVisibility(View.VISIBLE);
                                                binding.layoutHome.weight.setText("g/con");
                                                binding.layoutHome.loss.setText("con");
                                                binding.layoutHome.imageEditLoss.setVisibility(View.GONE);
                                                binding.layoutHome.imageEditWeight.setVisibility(View.GONE);
                                            });

                                    LocalDate now = LocalDate.now();
                                    String yesterday = now.minusDays(1).toString();

                                    database.collection(Constants.KEY_COLLECTION_PLAN)
                                            .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                if (task.getResult() != null && task.isSuccessful()){
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                        if (queryDocumentSnapshot.getString(Constants.KEY_POND_ID) != null){
                                                            database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                                                    .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                                                    .whereEqualTo(Constants.KEY_FISH_WEIGH_DATE, yesterday)
                                                                    .get()
                                                                    .addOnCompleteListener(task1-> {
                                                                        for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()){
                                                                            HashMap<String, Object> weight = new HashMap<>();
                                                                            weight.put(Constants.KEY_FISH_WEIGH_DATE, yesterday);
                                                                            weight.put(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID));
                                                                            weight.put(Constants.KEY_FISH_WEIGH_WEIGHT, queryDocumentSnapshot1.getString(Constants.KEY_FISH_WEIGH_WEIGHT));
                                                                            weight.put(Constants.KEY_FISH_WEIGH_LOSS, queryDocumentSnapshot1.getString(Constants.KEY_FISH_WEIGH_LOSS));
                                                                            if (task1.getResult() != null && task1.isSuccessful()){
                                                                                database.collection(Constants.KEY_COLLECTION_PLAN)
                                                                                        .document(queryDocumentSnapshot.getId())
                                                                                        .collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                                                                        .document(yesterday)
                                                                                        .set(weight)
                                                                                        .addOnSuccessListener(runnable1 ->
                                                                                                database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                                                                                        .document(queryDocumentSnapshot1.getId())
                                                                                                        .delete());
                                                                            }
                                                                        }
                                                                    });
                                                        }

                                                    }
                                                }
                                            });
                                } else {
                                    database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                            .whereEqualTo(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString())
                                            .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                            .get()
                                            .addOnCompleteListener(task -> {
                                                if (task.getResult() != null && task.isSuccessful()) {
                                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                        binding.layoutHome.btnAddWeight.setVisibility(View.GONE);
                                                        binding.layoutHome.weight.setText(queryDocumentSnapshot.getString(Constants.KEY_FISH_WEIGH_WEIGHT) + " g/con");
                                                        binding.layoutHome.loss.setText(queryDocumentSnapshot.getString(Constants.KEY_FISH_WEIGH_LOSS) + " con");
                                                        binding.layoutHome.imageEditLoss.setVisibility(View.VISIBLE);
                                                        binding.layoutHome.imageEditWeight.setVisibility(View.VISIBLE);
                                                    }
                                                }
                                            });

                                }
                            }
                        }
                    }
                });

        if (preferenceManager.getString(Constants.KEY_TREATMENT_ASSIGNMENT) != null
                && !Objects.equals(preferenceManager.getString(Constants.KEY_TREATMENT_ASSIGNMENT), "")) {
            if (preferenceManager.getString(Constants.KEY_TREATMENT_ASSIGNMENT).equals(Constants.KEY_TREATMENT_IS_ASSIGNMENT)){
                treatment = new Treatment();
                List<Medicine> medicines = new ArrayList<>();
                List<String> quantityList = new ArrayList<>();
                MedicineTreatmentUsedAdapter medicineAdapter = new MedicineTreatmentUsedAdapter(medicines, quantityList);
                LinearLayoutManager layoutManager
                        = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
                binding.layoutHome.medicineRecyclerView.setAdapter(medicineAdapter);
                binding.layoutHome.medicineRecyclerView.setLayoutManager(layoutManager);
                if (!Objects.equals(preferenceManager.getString(Constants.KEY_TREATMENT_ID), "")) {
                    database.collection(Constants.KEY_COLLECTION_TREATMENT)
                            .document(preferenceManager.getString(Constants.KEY_TREATMENT_ID))
                            .get()
                            .addOnCompleteListener(task -> {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                treatment.id = documentSnapshot.getId();
                                treatment.pondId = documentSnapshot.getString(Constants.KEY_TREATMENT_POND_ID);
                                treatment.campusId = documentSnapshot.getString(Constants.KEY_TREATMENT_CAMPUS_ID);
                                treatment.creatorId = documentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_ID);
                                treatment.creatorName = documentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_NAME);
                                treatment.creatorImage = documentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_IMAGE);
                                treatment.creatorPhone = documentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_PHONE);
                                if (documentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER) != null){
                                    treatment.replaceWater = documentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (documentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD) != null){
                                    treatment.noFood = documentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD);
                                }
                                if (documentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD) != null){
                                    treatment.suckMud = documentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD);
                                }
                                if (documentSnapshot.getString(Constants.KEY_TREATMENT_NOTE) != null){
                                    treatment.note = documentSnapshot.getString(Constants.KEY_TREATMENT_NOTE);
                                }
                                treatment.date = documentSnapshot.getString(Constants.KEY_TREATMENT_DATE);
                                treatment.sickName = documentSnapshot.getString(Constants.KEY_TREATMENT_SICK_NAME);
                                treatment.status = documentSnapshot.getString(Constants.KEY_TREATMENT_STATUS);
                                treatment.medicines = (HashMap<String, Object>) documentSnapshot.get(Constants.KEY_TREATMENT_MEDICINE);
                                treatment.assignmentStatus = documentSnapshot.getString(Constants.KEY_TREATMENT_ASSIGNMENT_STATUS);
                            })
                            .addOnSuccessListener(runnable -> {


                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    if (preferenceManager.getString(Constants.KEY_NOW) == null){
                                        preferenceManager.putString(Constants.KEY_NOW, LocalDate.now().toString());
                                    } else {
                                        if (!preferenceManager.getString(Constants.KEY_NOW).equals(LocalDate.now().toString())){
                                            HashMap<String, Object> newTreatment = new HashMap<>();
                                            newTreatment.put(Constants.KEY_TREATMENT_ASSIGNMENT_STATUS, Constants.KEY_TREATMENT_ASSIGNMENT_STATUS_DOING);
                                            database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                                    .document(treatment.id)
                                                    .update(newTreatment);
                                            treatment.assignmentStatus = Constants.KEY_TREATMENT_ASSIGNMENT_STATUS_DOING;
                                        }
                                    }

                                }

                                if (treatment.assignmentStatus.equals(Constants.KEY_TREATMENT_ASSIGNMENT_STATUS_DOING) &&
                                        treatment.status.equals(Constants.KEY_TREATMENT_ACCEPT)) {
                                    binding.layoutHome.cardTreatment.setVisibility(View.VISIBLE);
                                    if (treatment.noFood == null || treatment.noFood.equals("")){
                                        binding.layoutHome.textNoFood.setVisibility(View.GONE);
                                    }

                                    if (treatment.replaceWater == null || treatment.replaceWater.equals("")){
                                        binding.layoutHome.textReplaceWater.setVisibility(View.GONE);
                                    }

                                    if (treatment.suckMud == null || treatment.suckMud.equals("")){
                                        binding.layoutHome.textSuckMud.setVisibility(View.GONE);
                                    }
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

                            });
                }
            }
        }

        database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                .whereEqualTo(Constants.KEY_RELEASE_FISH_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()){
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            List<String> workerAssignId = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RELEASE_FISH_WORKER_ID_ASSIGN);
                            for (String id : Objects.requireNonNull(workerAssignId)){
                                if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_STATUS), Constants.KEY_RELEASE_FISH_UNCOMPLETED)){
                                    if (id.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                                        binding.layoutHome.cardReleaseFish.setVisibility(View.VISIBLE);
                                        binding.layoutHome.textReleaseFish.setText(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_AMOUNT_RELEASE) + " con");
                                        binding.layoutHome.textNeedToRelease.setText(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_AMOUNT) + " con");
                                    }
                                }
                            }
                        }
                    }
                });

        database.collection(Constants.KEY_COLLECTION_POND)
                .document(preferenceManager.getString(Constants.KEY_POND_ID))
                .get()
                .addOnCompleteListener(pondTask -> {
                    if (pondTask.isSuccessful()){
                        DocumentSnapshot documentSnapshot = pondTask.getResult();
                        List<String> amountFeds = (List<String>) documentSnapshot.get(Constants.KEY_AMOUNT_FED);
                        if (preferenceManager.getString(Constants.KEY_NOW) == null){
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                preferenceManager.putString(Constants.KEY_NOW, String.valueOf(LocalDate.now()));
                            }
                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            if (!preferenceManager.getString(Constants.KEY_NOW).equals(String.valueOf(LocalDate.now()))){
                                int totalFeedInDate = 0;
                                for (String num : amountFeds){
                                    totalFeedInDate = totalFeedInDate + Integer.parseInt(num);
                                }
                                int finalTotalFeedInDate = totalFeedInDate;
                                database.collection(Constants.KEY_COLLECTION_PLAN)
                                        .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                        .get()
                                        .addOnCompleteListener(task -> {
                                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                                if (queryDocumentSnapshot.getString(Constants.KEY_POND_ID) != null){
                                                    LocalDate now = LocalDate.now();
                                                    String yesterday = now.minusDays(1).toString();

                                                    database.collection(Constants.KEY_COLLECTION_PLAN)
                                                            .document(queryDocumentSnapshot.getId())
                                                            .get()
                                                            .addOnCompleteListener(task1 -> {
                                                                DocumentSnapshot documentSnapshot1 = task1.getResult();

                                                                database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                        .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                                                        .get()
                                                                        .addOnCompleteListener(warehouseTask -> {
                                                                            if (warehouseTask.isSuccessful()){
                                                                                for (QueryDocumentSnapshot warehouseQuerySnapshot : warehouseTask.getResult()){
                                                                                    if (warehouseQuerySnapshot.getString(Constants.KEY_POND_ID) != null){

                                                                                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                                                .document(warehouseQuerySnapshot.getId())
                                                                                                .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                                                                .whereEqualTo(Constants.KEY_CATEGORY_TYPE, Constants.KEY_FOOD)
                                                                                                .get()
                                                                                                .addOnCompleteListener(categoryTask -> {
                                                                                                    if (categoryTask.isSuccessful()){
                                                                                                        for (QueryDocumentSnapshot categoryDoc : categoryTask.getResult()){
                                                                                                            if (categoryDoc.getString(Constants.KEY_PRICE) != null){
                                                                                                                double price = finalTotalFeedInDate * Double.parseDouble(Objects.requireNonNull(categoryDoc.getString(Constants.KEY_PRICE)));
                                                                                                                HashMap<String, Object> feeds = new HashMap<>();
                                                                                                                feeds.put(Constants.KEY_DIARY_FEEDS_POND_ID, pond.getId());
                                                                                                                feeds.put(Constants.KEY_DIARY_FEEDS_DATE, yesterday);
                                                                                                                feeds.put(Constants.KEY_AMOUNT_FED, amountFeds);
                                                                                                                feeds.put(Constants.KEY_PRICE, price);

                                                                                                                database.collection(Constants.KEY_COLLECTION_PLAN)
                                                                                                                        .document(documentSnapshot1.getId())
                                                                                                                        .collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                                                                                                                        .document(yesterday)
                                                                                                                        .set(feeds);
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                });
                                                                                    }
                                                                                }
                                                                            }

                                                                        });

                                                            });

                                                }
                                            }
                                        });
                            }
                        }
                    }
                });

        getPondData();
    }

    private void setListener() {
        binding.toolbaWorkerHome.setNavigationOnClickListener(view -> onBackPressed());

        binding.layoutControlWorkerHome.layoutTask.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TaskManagerActivity.class)));

        binding.layoutControlWorkerHome.layoutRequest.setOnClickListener(view -> {
            Intent intent = new Intent(this, RequestManagementActivity.class);
            startActivity(intent);
        });

        binding.toolbaWorkerHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        binding.imageConference.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ConferenceActivity.class)));

        binding.imageChat.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ConversationActivity.class)));

        binding.imageLogout.setOnClickListener(view -> logOut());

        binding.layoutHome.food1.setOnClickListener(view -> setFeedDialog(1));

        binding.layoutHome.food2.setOnClickListener(view -> setFeedDialog(2));

        binding.layoutHome.food3.setOnClickListener(view -> setFeedDialog(3));

        binding.layoutHome.food4.setOnClickListener(view -> setFeedDialog(4));

        binding.layoutHome.food5.setOnClickListener(view -> setFeedDialog(5));

        binding.layoutHome.food6.setOnClickListener(view -> setFeedDialog(6));

        binding.layoutHome.food7.setOnClickListener(view -> setFeedDialog(7));

        binding.layoutHome.food8.setOnClickListener(view -> setFeedDialog(8));

        binding.layoutHome.environment1.setOnClickListener(view ->setMeasureWaterDialog(1));

        binding.layoutHome.environment2.setOnClickListener(view ->setMeasureWaterDialog(2));

        binding.layoutHome.environment3.setOnClickListener(view ->setMeasureWaterDialog(3));

        binding.layoutHome.environment4.setOnClickListener(view ->setMeasureWaterDialog(4));

        binding.layoutHome.environment5.setOnClickListener(view ->setMeasureWaterDialog(5));

        binding.layoutHome.environment6.setOnClickListener(view ->setMeasureWaterDialog(6));

        binding.layoutControlWorkerHome.cardReportFish.setOnClickListener(view -> openReportFishSickDialog());

        binding.layoutHome.btnComplete.setOnClickListener(view -> completeTreatmentInDay());

        binding.layoutHome.textSelectImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
            showToast("Đã chọn ảnh báo cáo hôm nay!");
        });

        binding.layoutHome.btnAddWeight.setOnClickListener(view -> openAddWeightDialog());

        binding.layoutHome.imageEditWeight.setOnClickListener(view ->  openEditWeightDialog());

        binding.layoutHome.imageEditLoss.setOnClickListener(view ->  openEditLossDialog());

        binding.layoutHome.btnAddReleaseFish.setOnClickListener(view -> openAddReleaseFishDialog());

        binding.layoutControlWorkerHome.layoutAI.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), AIActivity.class)));

    }

    private void openAddReleaseFishDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_add_release_fish_worker);
        assert dialog != null;

        AppCompatButton btnClose, btnAdd;
        TextInputEditText edtNumOfFish = dialog.findViewById(R.id.edtNumOfFish);

        btnAdd = dialog.findViewById(R.id.btnAdd);
        btnClose = dialog.findViewById(R.id.btnClose);

        btnAdd.setOnClickListener(view -> {
            if (Objects.requireNonNull(edtNumOfFish.getText()).toString().equals("")){
                showToast("Vui lòng nhập số lượng cá đã thả!");
            } else {
                database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                        .whereEqualTo(Constants.KEY_RELEASE_FISH_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                        .whereEqualTo(Constants.KEY_RELEASE_FISH_STATUS, Constants.KEY_RELEASE_FISH_UNCOMPLETED)
                        .get()
                        .addOnCompleteListener(task -> {
                           if (task.isSuccessful()){
                               for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                   if (!edtNumOfFish.getText().toString().equals(queryDocumentSnapshot.getString(Constants.KEY_RELEASE_FISH_AMOUNT))){
                                       showToast("Bạn phải thả đúng số lượng cá được giao!");
                                   } else {
                                       HashMap<String, Object> release = new HashMap<>();
                                       release.put(Constants.KEY_RELEASE_FISH_STATUS, Constants.KEY_RELEASE_FISH_COMPLETED);
                                       release.put(Constants.KEY_RELEASE_FISH_AMOUNT_RELEASE, edtNumOfFish.getText().toString());
                                       database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                                               .document(queryDocumentSnapshot.getId())
                                               .update(release)
                                               .addOnSuccessListener(runnable -> {
                                                   showToast("Đã cập nhật số lượng cá thành công!");
                                                   binding.layoutHome.cardReleaseFish.setVisibility(View.GONE);
                                                   dialog.dismiss();
                                               })
                                               .addOnFailureListener(runnable -> showToast("Cập nhật số lượng cá thả thất bại!"));
                                   }
                               }
                           }
                        });
            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void getPondData() {
        database.collection(Constants.KEY_COLLECTION_POND)
                .document(preferenceManager.getString(Constants.KEY_POND_ID))
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot pondDocument = task.getResult();
                    String pondId = pondDocument.getId();
                    String pondName = pondDocument.getString(Constants.KEY_NAME);
                    String acreage = pondDocument.getString(Constants.KEY_ACREAGE);
                    List<String> numOfFeedingList = (List<String>) pondDocument.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                    List<String> amountFedList = (List<String>) pondDocument.get(Constants.KEY_AMOUNT_FED);
                    List<String> specificationsToMeasureList = (List<String>) pondDocument.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                    HashMap<String, Object> parameters = (HashMap<String, Object>) pondDocument.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                    int numOfFeeding = Integer.parseInt(Objects.requireNonNull(pondDocument.getString(Constants.KEY_NUM_OF_FEEDING)));
                    pond = new Pond(pondId, pondName, null, null, acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters);
                })
                .addOnSuccessListener(runnable -> {

                    setVisibleData();
                    if (preferenceManager.getString(Constants.KEY_NOW) == null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            preferenceManager.putString(Constants.KEY_NOW, String.valueOf(LocalDate.now()));
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!preferenceManager.getString(Constants.KEY_NOW).equals(String.valueOf(LocalDate.now()))){
                            int totalFeedInDate = 0;
                            for (String num : pond.getAmountFeedList()){
                                totalFeedInDate = totalFeedInDate + Integer.parseInt(num);
                            }
                            int finalTotalFeedInDate = totalFeedInDate;
                            database.collection(Constants.KEY_COLLECTION_PLAN)
                                    .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                    .get()
                                    .addOnCompleteListener(task -> {
                                       for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                           if (queryDocumentSnapshot.getString(Constants.KEY_POND_ID) != null){
                                               LocalDate now = LocalDate.now();
                                               String yesterday = now.minusDays(1).toString();

                                               binding.layoutHome.btnAddWeight.setVisibility(View.VISIBLE);

                                               HashMap<String, Object> water = new HashMap<>();
                                               water.put(Constants.KEY_DIARY_WATER_POND_ID, pond.getId());
                                               water.put(Constants.KEY_DIARY_WATER_DATE, yesterday);
                                               water.put(Constants.KEY_SPECIFICATIONS_MEASURED, pond.getParameters());

                                               database.collection(Constants.KEY_COLLECTION_PLAN)
                                                       .document(queryDocumentSnapshot.getId())
                                                       .get()
                                                       .addOnCompleteListener(task1 -> {
                                                           DocumentSnapshot documentSnapshot = task1.getResult();

                                                           database.collection(Constants.KEY_COLLECTION_PLAN)
                                                                   .document(documentSnapshot.getId())
                                                                   .collection(Constants.KEY_DIARY_COLLECTION_WATER)
                                                                   .document(yesterday)
                                                                   .set(water);


                                                       }).addOnSuccessListener(runnable1 -> {
                                                           database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                   .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                                                                   .get()
                                                                   .addOnCompleteListener(task1 -> {

                                                                       for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()){
                                                                           database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                                   .document(queryDocumentSnapshot1.getId())
                                                                                   .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                                                   .whereEqualTo(Constants.KEY_CATEGORY_TYPE, Constants.KEY_CATEGORY_TYPE_FOOD)
                                                                                   .get()
                                                                                   .addOnCompleteListener(task2 -> {
                                                                                       for (QueryDocumentSnapshot queryDocumentSnapshot2 : task2.getResult()){
                                                                                           int amountFood = Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot2.getString(Constants.KEY_AMOUNT_OF_ROOM)));
                                                                                           amountFood = amountFood - finalTotalFeedInDate;
                                                                                           HashMap<String, Object> updated = new HashMap<>();
                                                                                           updated.put(Constants.KEY_AMOUNT_OF_ROOM, amountFood + "");
                                                                                           database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                                                   .document(queryDocumentSnapshot1.getId())
                                                                                                   .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                                                                   .document(queryDocumentSnapshot2.getId())
                                                                                                   .update(updated);
                                                                                       }
                                                                                   });
                                                                       }

                                                                   });

                                                           List<String> amountFed = pond.getAmountFeedList();
                                                           for (int i = 0; i < amountFed.size(); i++){
                                                               if (!amountFed.get(i).equals("0")){
                                                                   amountFed.set(i, "0");
                                                               }
                                                           }
                                                           HashMap<String, Object> updateMeasuredParameters = pond.getParameters();
                                                           updateMeasuredParameters.replaceAll ((key, value) -> "0");

                                                           HashMap<String, Object> updateAmountFed = new HashMap<>();
                                                           updateAmountFed.put(Constants.KEY_AMOUNT_FED, amountFed);
                                                           updateAmountFed.put(Constants.KEY_SPECIFICATIONS_MEASURED, updateMeasuredParameters);

                                                           if(feedTask != null) {

                                                               database.collection(Constants.KEY_COLLECTION_POND)
                                                                       .document(pond.getId())
                                                                       .update(updateAmountFed)
                                                                       .addOnSuccessListener(runnable2 -> getPondDataAfterUpdate())
                                                                       .addOnCompleteListener(tas1k -> {
                                                                           HashMap<String, Object> unCompletedTask = new HashMap<>();
                                                                           unCompletedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);
                                                                           database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                                                                   .document(feedTask.id)
                                                                                   .update(unCompletedTask)
                                                                                   .addOnSuccessListener(runnable2 -> setVisibleData());

                                                                           database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                                                                   .document(measureTask.id)
                                                                                   .update(unCompletedTask)
                                                                                   .addOnSuccessListener(runnable2 -> setVisibleData());


                                                                           preferenceManager.putString(Constants.KEY_NOW, String.valueOf(LocalDate.now()));
                                                                       });

                                                           }

                                                       });

                                           }
                                       }
                                    });

                        }
                    }

                });
    }

    @SuppressLint("SetTextI18n")
    private void getPondDataAfterUpdate() {
        database.collection(Constants.KEY_COLLECTION_POND)
                .document(preferenceManager.getString(Constants.KEY_POND_ID))
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot pondDocument = task.getResult();
                    String pondId = pondDocument.getId();
                    String pondName = pondDocument.getString(Constants.KEY_NAME);
                    String acreage = pondDocument.getString(Constants.KEY_ACREAGE);
                    List<String> numOfFeedingList = (List<String>) pondDocument.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                    List<String> amountFedList = (List<String>) pondDocument.get(Constants.KEY_AMOUNT_FED);
                    List<String> specificationsToMeasureList = (List<String>) pondDocument.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                    HashMap<String, Object> parameters = (HashMap<String, Object>) pondDocument.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                    int numOfFeeding = Integer.parseInt(Objects.requireNonNull(pondDocument.getString(Constants.KEY_NUM_OF_FEEDING)));
                    pond = new Pond(new Pond(pondId, pondName, null, null, acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters));
                })
                .addOnSuccessListener(runnable -> {

                    setVisibleData();
                    if (preferenceManager.getString(Constants.KEY_NOW) == null){
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            preferenceManager.putString(Constants.KEY_NOW, String.valueOf(LocalDate.now()));
                        }
                    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        if (!preferenceManager.getString(Constants.KEY_NOW).equals(String.valueOf(LocalDate.now()))){

                            int totalFeedInDate = 0;
                            for (String num : pond.getAmountFeedList()){
                                totalFeedInDate = totalFeedInDate + Integer.parseInt(num);
                            }
                            int finalTotalFeedInDate = totalFeedInDate;
                            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                    .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                                    .get()
                                    .addOnCompleteListener(task -> {

                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                    .whereEqualTo(Constants.KEY_CATEGORY_TYPE, Constants.KEY_CATEGORY_TYPE_FOOD)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()){
                                                            int amountFood = Integer.parseInt(Objects.requireNonNull(queryDocumentSnapshot1.getString(Constants.KEY_AMOUNT_OF_ROOM)));
                                                            amountFood = amountFood - finalTotalFeedInDate;
                                                            HashMap<String, Object> updated = new HashMap<>();
                                                            updated.put(Constants.KEY_AMOUNT_OF_ROOM, amountFood + "");
                                                            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                    .document(queryDocumentSnapshot.getId())
                                                                    .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                                    .document(queryDocumentSnapshot1.getId())
                                                                    .update(updated);
                                                        }
                                                    });
                                        }

                                    });

                            List<String> amountFed = pond.getAmountFeedList();
                            for (int i = 0; i < amountFed.size(); i++){
                                if (!amountFed.get(i).equals("0")){
                                    amountFed.set(i, "0");
                                }
                            }
                            HashMap<String, Object> updateMeasuredParameters = pond.getParameters();
                            updateMeasuredParameters.replaceAll ((key, value) -> "0");

                            HashMap<String, Object> updateAmountFed = new HashMap<>();
                            updateAmountFed.put(Constants.KEY_AMOUNT_FED, amountFed);
                            updateAmountFed.put(Constants.KEY_SPECIFICATIONS_MEASURED, updateMeasuredParameters);
                            database.collection(Constants.KEY_COLLECTION_POND)
                                    .document(pond.getId())
                                    .update(updateAmountFed);


                            HashMap<String, Object> unCompletedTask = new HashMap<>();
                            unCompletedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);
                            database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                    .document(feedTask.id)
                                    .update(unCompletedTask)
                                    .addOnSuccessListener(runnable1 -> setVisibleData());

                            database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                    .document(measureTask.id)
                                    .update(unCompletedTask)
                                    .addOnSuccessListener(runnable1 -> setVisibleData());

                            preferenceManager.putString(Constants.KEY_NOW, String.valueOf(LocalDate.now()));
                        }
                    }

                });
    }

    @SuppressLint("SetTextI18n")
    private void setVisibleData(){
        if (pond.getNumOfFeeding() == 1){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 2){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 3){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 4){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 5){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 6){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 7){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.VISIBLE);
            binding.layoutHome.food7.setVisibility(View.VISIBLE);
        } else if (pond.getNumOfFeeding() == 8){
            binding.layoutHome.food1.setVisibility(View.VISIBLE);
            binding.layoutHome.food2.setVisibility(View.VISIBLE);
            binding.layoutHome.food3.setVisibility(View.VISIBLE);
            binding.layoutHome.food4.setVisibility(View.VISIBLE);
            binding.layoutHome.food5.setVisibility(View.VISIBLE);
            binding.layoutHome.food6.setVisibility(View.VISIBLE);
            binding.layoutHome.food7.setVisibility(View.VISIBLE);
            binding.layoutHome.food8.setVisibility(View.VISIBLE);
        }

        binding.layoutHome.textQuantityFood1.setText(pond.getAmountFeedList().get(0));
        binding.layoutHome.textQuantityFood2.setText(pond.getAmountFeedList().get(1));
        binding.layoutHome.textQuantityFood3.setText(pond.getAmountFeedList().get(2));
        binding.layoutHome.textQuantityFood4.setText(pond.getAmountFeedList().get(3));
        binding.layoutHome.textQuantityFood5.setText(pond.getAmountFeedList().get(4));
        binding.layoutHome.textQuantityFood6.setText(pond.getAmountFeedList().get(5));
        binding.layoutHome.textQuantityFood8.setText(pond.getAmountFeedList().get(7));
        binding.layoutHome.textFood1.setText("Lần 1: " + pond.getNumOfFeedingList().get(0));
        binding.layoutHome.textFood2.setText("Lần 2: " + pond.getNumOfFeedingList().get(1));
        binding.layoutHome.textFood3.setText("Lần 3: " + pond.getNumOfFeedingList().get(2));
        binding.layoutHome.textFood4.setText("Lần 4: " + pond.getNumOfFeedingList().get(3));
        binding.layoutHome.textFood5.setText("Lần 5: " + pond.getNumOfFeedingList().get(4));
        binding.layoutHome.textFood6.setText("Lần 6: " + pond.getNumOfFeedingList().get(5));
        binding.layoutHome.textFood7.setText("Lần 7: " + pond.getNumOfFeedingList().get(6));
        binding.layoutHome.textFood8.setText("Lần 8: " + pond.getNumOfFeedingList().get(7));

        if (pond.getAmountFeedList().get(0).equals("0")){
            binding.layoutHome.textQuantityFood1.setVisibility(View.GONE);
            binding.layoutHome.imageFood1.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood1.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood1.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(1).equals("0")){
            binding.layoutHome.textQuantityFood2.setVisibility(View.GONE);
            binding.layoutHome.imageFood2.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood2.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood2.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(2).equals("0")){
            binding.layoutHome.textQuantityFood3.setVisibility(View.GONE);
            binding.layoutHome.imageFood3.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood3.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood3.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(3).equals("0")){
            binding.layoutHome.textQuantityFood4.setVisibility(View.GONE);
            binding.layoutHome.imageFood4.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood4.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood4.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(4).equals("0")){
            binding.layoutHome.textQuantityFood5.setVisibility(View.GONE);
            binding.layoutHome.imageFood5.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood5.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood5.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(5).equals("0")){
            binding.layoutHome.textQuantityFood6.setVisibility(View.GONE);
            binding.layoutHome.imageFood6.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood6.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood6.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(6).equals("0")){
            binding.layoutHome.textQuantityFood7.setVisibility(View.GONE);
            binding.layoutHome.imageFood7.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood7.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood7.setVisibility(View.GONE);
        }

        if (pond.getAmountFeedList().get(7).equals("0")){
            binding.layoutHome.textQuantityFood8.setVisibility(View.GONE);
            binding.layoutHome.imageFood8.setVisibility(View.VISIBLE);
        } else {
            binding.layoutHome.textQuantityFood8.setVisibility(View.VISIBLE);
            binding.layoutHome.imageFood8.setVisibility(View.GONE);
        }

        List<String> specificationsToMeasureList = pond.getSpecificationsToMeasureList();
        if (specificationsToMeasureList.contains(Constants.KEY_SPECIFICATION_PH)) {
            binding.layoutHome.environment1.setVisibility(View.VISIBLE);
        }

        if (specificationsToMeasureList.contains(Constants.KEY_SPECIFICATION_SALINITY)) {
            binding.layoutHome.environment2.setVisibility(View.VISIBLE);
        }

        if (specificationsToMeasureList.contains(Constants.KEY_SPECIFICATION_ALKALINITY)) {
            binding.layoutHome.environment3.setVisibility(View.VISIBLE);
        }

        if (specificationsToMeasureList.contains(Constants.KEY_SPECIFICATION_TEMPERATE)) {
            binding.layoutHome.environment4.setVisibility(View.VISIBLE);
        }

        if (specificationsToMeasureList.contains(Constants.KEY_SPECIFICATION_H2S)) {
            binding.layoutHome.environment5.setVisibility(View.VISIBLE);
        }

        if (specificationsToMeasureList.contains(Constants.KEY_SPECIFICATION_NH3)) {
            binding.layoutHome.environment6.setVisibility(View.VISIBLE);
        }

        HashMap<String, Object> parameters = pond.getParameters();
        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_PH), "0")){
            binding.layoutHome.textQuantityEnvironment1.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment1.setVisibility(View.GONE);
            binding.layoutHome.textQuantityEnvironment1.setText(parameters.get(Constants.KEY_SPECIFICATION_PH) + "");
        } else {
            binding.layoutHome.textQuantityEnvironment1.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment1.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_SALINITY), "0")){
            binding.layoutHome.textQuantityEnvironment2.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment2.setVisibility(View.GONE);
            binding.layoutHome.textQuantityEnvironment2.setText(parameters.get(Constants.KEY_SPECIFICATION_SALINITY) + "");
        } else {
            binding.layoutHome.textQuantityEnvironment2.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment2.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_ALKALINITY), "0")){
            binding.layoutHome.textQuantityEnvironment3.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment3.setVisibility(View.GONE);
            binding.layoutHome.textQuantityEnvironment3.setText(parameters.get(Constants.KEY_SPECIFICATION_ALKALINITY) + "");
        } else {
            binding.layoutHome.textQuantityEnvironment3.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment3.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_TEMPERATE), "0")){
            binding.layoutHome.textQuantityEnvironment4.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment4.setVisibility(View.GONE);
            binding.layoutHome.textQuantityEnvironment4.setText(parameters.get(Constants.KEY_SPECIFICATION_TEMPERATE) + "");
        } else {
            binding.layoutHome.textQuantityEnvironment4.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment4.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_H2S), "0")){
            binding.layoutHome.textQuantityEnvironment5.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment5.setVisibility(View.GONE);
            binding.layoutHome.textQuantityEnvironment5.setText(parameters.get(Constants.KEY_SPECIFICATION_H2S) + "");
        } else {
            binding.layoutHome.textQuantityEnvironment5.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment5.setVisibility(View.VISIBLE);
        }

        if (!Objects.equals(parameters.get(Constants.KEY_SPECIFICATION_NH3), "0")){
            binding.layoutHome.textQuantityEnvironment6.setVisibility(View.VISIBLE);
            binding.layoutHome.imageEnvironment6.setVisibility(View.GONE);
            binding.layoutHome.textQuantityEnvironment6.setText(parameters.get(Constants.KEY_SPECIFICATION_NH3) + "");
        } else {
            binding.layoutHome.textQuantityEnvironment6.setVisibility(View.GONE);
            binding.layoutHome.imageEnvironment6.setVisibility(View.VISIBLE);
        }
    }

    private void setFeedDialog(int numOfFeed){
        Dialog dialog = openDialog(R.layout.layout_dialog_set_feed_for_fish);
        assert dialog != null;

        AppCompatButton btnSave, btnClose;

        btnSave = dialog.findViewById(R.id.btnSave);
        btnClose = dialog.findViewById(R.id.btnClose);
        TextInputEditText edtFood = dialog.findViewById(R.id.edtFood);

        btnSave.setOnClickListener(view -> {

            String feed = Objects.requireNonNull(edtFood.getText()).toString();

            if (numOfFeed == 1) {
                binding.layoutHome.imageFood1.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood1.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood1.setText(feed);
            } else if (numOfFeed == 2) {
                binding.layoutHome.imageFood2.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood2.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood2.setText(feed);
            } else if (numOfFeed == 3) {
                binding.layoutHome.imageFood3.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood3.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood3.setText(feed);
            } else if (numOfFeed == 4) {
                binding.layoutHome.imageFood4.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood4.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood4.setText(feed);
            } else if (numOfFeed == 5) {
                binding.layoutHome.imageFood5.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood5.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood5.setText(feed);
            } else if (numOfFeed == 6) {
                binding.layoutHome.imageFood6.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood6.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood6.setText(feed);
            } else if (numOfFeed == 7) {
                binding.layoutHome.imageFood7.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood7.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood7.setText(feed);
            } else if (numOfFeed == 8) {
                binding.layoutHome.imageFood8.setVisibility(View.GONE);
                binding.layoutHome.textQuantityFood8.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityFood8.setText(feed);
            }

            if (!feed.equals("")){
                List<String> updateFoodFedList = pond.getAmountFeedList();
                updateFoodFedList.set(numOfFeed - 1, feed + "");
                HashMap<String, Object> updateList = new HashMap<>();
                updateList.put(Constants.KEY_AMOUNT_FED, updateFoodFedList);
                database.collection(Constants.KEY_COLLECTION_POND)
                        .document(pond.getId())
                        .update(updateList)
                        .addOnSuccessListener(unused -> {
                            showToast("Đã cập nhật lượng thức ăn thành công!");
                            int count = 0;
                            for (String num : updateFoodFedList){
                                if (!num.equals("0"))
                                    count++;
                            }
                            HashMap<String, Object> completedTask = new HashMap<>();
                            if (count == pond.getNumOfFeeding() && pond.getAmountFeedList().equals(pond.getNumOfFeedingList())){
                                completedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_COMPLETED);
                            } else {
                                completedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);
                            }
                            database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                    .document(feedTask.id)
                                    .update(completedTask);
                            dialog.dismiss();
                        })
                        .addOnFailureListener(runnable -> {
                            showToast("Cập nhật lượng thức ăn thất bại!");
                            dialog.dismiss();
                        });
            } else {
                showToast("Vui lòng nhập số lượng thức ăn!");
            }

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void setMeasureWaterDialog(int numOfFeed){
        Dialog dialog = openDialog(R.layout.layout_dialog_set_measure_water);
        assert dialog != null;

        AppCompatButton btnSave, btnClose;

        btnSave = dialog.findViewById(R.id.btnSave);
        btnClose = dialog.findViewById(R.id.btnClose);
        TextInputEditText edtWater = dialog.findViewById(R.id.edtWater);

        btnSave.setOnClickListener(view -> {

            HashMap<String, Object> parameters = pond.getParameters();
            String parameter = Objects.requireNonNull(edtWater.getText()).toString();

            if (numOfFeed == 1) {
                binding.layoutHome.imageEnvironment1.setVisibility(View.GONE);
                binding.layoutHome.textQuantityEnvironment1.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityEnvironment1.setText(parameter);
                parameters.replace(Constants.KEY_SPECIFICATION_PH, parameter);
            } else if (numOfFeed == 2) {
                binding.layoutHome.imageEnvironment2.setVisibility(View.GONE);
                binding.layoutHome.textQuantityEnvironment2.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityEnvironment2.setText(parameter);
                parameters.replace(Constants.KEY_SPECIFICATION_SALINITY, parameter);
            } else if (numOfFeed == 3) {
                binding.layoutHome.imageEnvironment3.setVisibility(View.GONE);
                binding.layoutHome.textQuantityEnvironment3.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityEnvironment3.setText(parameter);
                parameters.replace(Constants.KEY_SPECIFICATION_ALKALINITY, parameter);
            } else if (numOfFeed == 4) {
                binding.layoutHome.imageEnvironment4.setVisibility(View.GONE);
                binding.layoutHome.textQuantityEnvironment4.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityEnvironment4.setText(parameter);
                parameters.replace(Constants.KEY_SPECIFICATION_TEMPERATE, parameter);
            } else if (numOfFeed == 5) {
                binding.layoutHome.imageEnvironment5.setVisibility(View.GONE);
                binding.layoutHome.textQuantityEnvironment5.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityEnvironment5.setText(parameter);
                parameters.replace(Constants.KEY_SPECIFICATION_H2S, parameter);
            } else if (numOfFeed == 6) {
                binding.layoutHome.imageEnvironment6.setVisibility(View.GONE);
                binding.layoutHome.textQuantityEnvironment6.setVisibility(View.VISIBLE);
                binding.layoutHome.textQuantityEnvironment6.setText(parameter);
                parameters.replace(Constants.KEY_SPECIFICATION_NH3, parameter);
            }

            if (!parameter.equals("")){

                HashMap<String, Object> updatedParameter = new HashMap<>();
                updatedParameter.put(Constants.KEY_SPECIFICATIONS_MEASURED, parameters);
                pond.setParameters(parameters);

                database.collection(Constants.KEY_COLLECTION_POND)
                        .document(pond.getId())
                        .update(updatedParameter)
                        .addOnCompleteListener(task -> {

                            int countParameterMeasure = 0;
                            List<String> specificationsToMeasureList = pond.getSpecificationsToMeasureList();

                            for (String spec : specificationsToMeasureList) {
                                if (!spec.equals("0")){
                                    countParameterMeasure++;
                                }
                            }

                            int countParameter;
                            HashMap<String, Object> parametersMeasured = pond.getParameters();
                            countParameter = Collections.frequency (parametersMeasured.values(), "0");

                            HashMap<String, Object> completedTask = new HashMap<>();
                            if ((6 - countParameter) == countParameterMeasure){
                                completedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_COMPLETED);
                            } else {
                                completedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);
                            }

                            database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                    .document(measureTask.id)
                                    .update(completedTask);

                            showToast("Đã cập nhật thông số thành công!");
                            dialog.dismiss();
                        })
                        .addOnFailureListener(runnable -> showToast("Cập nhật thông số thất bại!"));



            } else {
                showToast("Vui lòng nhập số lượng thức ăn!");
            }

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void openReportFishSickDialog(){
        final Dialog dialog = openDialog(R.layout.layout_dialog_report_fish);
        assert dialog != null;

        AppCompatButton btnCreate, btnClose;
        TextInputEditText edtGuess;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnCreate = dialog.findViewById(R.id.btnCreate);
        imageReason = dialog.findViewById(R.id.imageReason);
        edtGuess = dialog.findViewById(R.id.edtGuess);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            imageReason.setOnClickListener(view -> {
                preferenceManager.putString(Constants.KEY_IMAGE, Constants.KEY_IMAGE);
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                pickImage.launch(intent);
            });
        }

        btnCreate.setOnClickListener(view -> {

            HashMap<String, Object> report = new HashMap<>();
            report.put(Constants.KEY_REPORT_FISH_IMAGE, encodedImage);
            report.put(Constants.KEY_REPORT_FISH_GUESS, Objects.requireNonNull(edtGuess.getText()).toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                report.put(Constants.KEY_REPORT_FISH_DATE, Objects.requireNonNull(LocalDate.now().toString()));
            }
            report.put(Constants.KEY_REPORTER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            report.put(Constants.KEY_REPORTER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            report.put(Constants.KEY_REPORTER_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
            report.put(Constants.KEY_REPORTER_TYPE_ACCOUNT, preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT));
            report.put(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID));
            report.put(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID));
            report.put(Constants.KEY_REPORTER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            report.put(Constants.KEY_REPORT_STATUS, Constants.KEY_REPORT_PENDING);

            database.collection(Constants.KEY_COLLECTION_REPORT_FISH)
                    .add(report)
                    .addOnSuccessListener(runnable -> {
                        showToast("Tạo báo cáo thành công!");
                        dialog.dismiss();
                    })
                    .addOnFailureListener(runnable -> showToast("Tạo báo cáo thất bại!"));

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    @SuppressLint("SetTextI18n")
    private void openAddWeightDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_add_weight);
        assert dialog != null;

        TextInputEditText edtWeight, edtLoss;
        AppCompatButton btnClose, btnEnter;

        edtWeight = dialog.findViewById(R.id.edtWeight);
        edtLoss = dialog.findViewById(R.id.edtLoss);
        btnClose = dialog.findViewById(R.id.btnClose);
        btnEnter = dialog.findViewById(R.id.btnEnter);

        btnEnter.setOnClickListener(view -> {
            if (Objects.requireNonNull(edtWeight.getText()).toString().equals("") || Objects.requireNonNull(edtLoss.getText()).toString().equals("")){
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else {
                HashMap<String, Object> weight = new HashMap<>();
                weight.put(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    weight.put(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString());
                }
                weight.put(Constants.KEY_FISH_WEIGH_WEIGHT, edtWeight.getText().toString());
                weight.put(Constants.KEY_FISH_WEIGH_LOSS, edtLoss.getText().toString());
                database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                        .add(weight)
                        .addOnSuccessListener(runnable -> {
                            showToast("Cập nhật thành công!");
                            dialog.dismiss();
                            binding.layoutHome.btnAddWeight.setVisibility(View.GONE);
                            HashMap<String, Object> completedTask = new HashMap<>();
                            completedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_COMPLETED);
                            database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                    .document(scaleTask.id)
                                    .update(completedTask);
                            binding.layoutHome.weight.setText(edtWeight.getText().toString() + " g/con");
                            binding.layoutHome.loss.setText(edtLoss.getText().toString() + " con");
                            binding.layoutHome.imageEditLoss.setVisibility(View.VISIBLE);
                            binding.layoutHome.imageEditWeight.setVisibility(View.VISIBLE);


                        })
                        .addOnFailureListener(runnable -> showToast("Cập nhật thất bại!"));
            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    @SuppressLint("SetTextI18n")
    private void openEditWeightDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_edit_weight);
        assert dialog != null;

        AppCompatButton btnEdit, btnClose;
        TextInputEditText edtWeight = dialog.findViewById(R.id.edtWeight);


        btnEdit = dialog.findViewById(R.id.btnEdit);
        btnClose = dialog.findViewById(R.id.btnClose);

        btnEdit.setOnClickListener(view -> {
            if (Objects.requireNonNull(edtWeight.getText()).toString().equals("")){
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    HashMap<String, Object> updateWeight = new HashMap<>();
                    updateWeight.put(Constants.KEY_FISH_WEIGH_WEIGHT, Objects.requireNonNull(edtWeight.getText()).toString());
                    updateWeight.put(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID));
                    updateWeight.put(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString());

                    database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                            .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                            .whereEqualTo(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                        updateWeight.put(Constants.KEY_FISH_WEIGH_LOSS, queryDocumentSnapshot.getString(Constants.KEY_FISH_WEIGH_LOSS));
                                        database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                                .document(queryDocumentSnapshot.getId())
                                                .set(updateWeight)
                                                .addOnSuccessListener(runnable -> {
                                                    showToast("Đã cập nhật trọng lượng thành công");
                                                    dialog.dismiss();
                                                    binding.layoutHome.weight.setText(Objects.requireNonNull(edtWeight.getText()) + " g/con");
                                                })
                                                .addOnFailureListener(runnable -> showToast("Cập nhật trọng lượng thất bại"));
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
    private void openEditLossDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_edit_loss);
        assert dialog != null;

        AppCompatButton btnEdit, btnClose;
        TextInputEditText edtLoss = dialog.findViewById(R.id.edtLoss);

        btnEdit = dialog.findViewById(R.id.btnEdit);
        btnClose = dialog.findViewById(R.id.btnClose);

        btnEdit.setOnClickListener(view -> {
            if (Objects.requireNonNull(edtLoss.getText()).toString().equals("")){
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                    HashMap<String, Object> updateWeight = new HashMap<>();
                    updateWeight.put(Constants.KEY_FISH_WEIGH_LOSS, Objects.requireNonNull(edtLoss.getText()).toString());
                    updateWeight.put(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID));
                    updateWeight.put(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString());

                    database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                            .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                            .whereEqualTo(Constants.KEY_FISH_WEIGH_DATE, LocalDate.now().toString())
                            .get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()){
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                        updateWeight.put(Constants.KEY_FISH_WEIGH_WEIGHT, queryDocumentSnapshot.getString(Constants.KEY_FISH_WEIGH_WEIGHT));
                                        database.collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                                .document(queryDocumentSnapshot.getId())
                                                .set(updateWeight)
                                                .addOnSuccessListener(runnable -> {
                                                    showToast("Đã cập nhật trọng lượng thành công");
                                                    dialog.dismiss();
                                                    binding.layoutHome.loss.setText(Objects.requireNonNull(edtLoss.getText()) + " g/con");
                                                })
                                                .addOnFailureListener(runnable -> showToast("Cập nhật trọng lượng thất bại"));
                                    }
                                }
                            });
                }
            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void completeTreatmentInDay() {
        if (encodedImageTreatment != null){
            HashMap<String, Object> completeTreatment = new HashMap<>();
            completeTreatment.put(Constants.KEY_TREATMENT_ASSIGNMENT_STATUS, Constants.KEY_TREATMENT_COMPLETED);
            database.collection(Constants.KEY_COLLECTION_TREATMENT)
                    .document(treatment.id)
                    .update(completeTreatment)
                    .addOnSuccessListener(runnable -> {
                        showToast("Đã cập nhật thành công!");
                        binding.layoutHome.cardTreatment.setVisibility(View.GONE);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            HashMap<String, Object> updateReport = new HashMap<>();
                            updateReport.put(Constants.KEY_TREATMENT_IMAGE_REPORT, encodedImageTreatment);
                            database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                    .document(treatment.id)
                                    .collection(Constants.KEY_TREATMENT_COLLECTION_IMAGE_EVERYDAY)
                                    .document(LocalDate.now().toString())
                                    .set(updateReport);
                        }

                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                .whereEqualTo(Constants.KEY_POND_ID, preferenceManager.getString(Constants.KEY_POND_ID))
                                .get()
                                .addOnCompleteListener(task -> {
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                        HashMap<String, Object> medicines = treatment.medicines;
                                        medicines.forEach((key, value) ->
                                                database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                        .document(queryDocumentSnapshot.getId())
                                                        .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                        .document(key)
                                                        .get()
                                                        .addOnCompleteListener(task1 -> {

                                                            DocumentSnapshot documentSnapshot = task1.getResult();
                                                            int amountInWareHouse = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_AMOUNT)));

                                                            amountInWareHouse = amountInWareHouse - Integer.parseInt(String.valueOf(value));
                                                            HashMap<String, Object> amount = new HashMap<>();
                                                            amount.put(Constants.KEY_AMOUNT, amountInWareHouse + "");

                                                            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                                    .document(queryDocumentSnapshot.getId())
                                                                    .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                                    .document(documentSnapshot.getId())
                                                                    .update(amount)
                                                                    .addOnCompleteListener(runnable1 -> {
                                                                        final double price = Double.parseDouble(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_PRICE))) * Integer.parseInt(String.valueOf(value));
                                                                        double totalPrice = Double.parseDouble(preferenceManager.getString(Constants.KEY_MEDICINES_PRICE_PRICE)) + price;
                                                                        preferenceManager.putString(Constants.KEY_MEDICINES_PRICE_PRICE, totalPrice + "");
                                                                    })
                                                                    .addOnSuccessListener(runnable1 -> {
                                                                        HashMap<String, Object> medicinePrice = new HashMap<>();
                                                                        medicinePrice.put(Constants.KEY_PRICE, preferenceManager.getString(Constants.KEY_MEDICINES_PRICE_PRICE));
                                                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                                                            database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                                                                    .document(treatment.id)
                                                                                    .collection(Constants.KEY_COLLECTION_MEDICINES_PRICE)
                                                                                    .document(LocalDate.now().toString())
                                                                                    .set(medicinePrice);
                                                                        }
                                                                    });

                                                        }));
                                    }
                                });
                    });
        } else {
            showToast("Vui lòng cập nhật ảnh để hoàn thành điều trị hôm nay!");
        }
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            if (preferenceManager.getString(Constants.KEY_IMAGE) != null &&
                                    preferenceManager.getString(Constants.KEY_IMAGE).equals(Constants.KEY_IMAGE)){
                                imageReason.setImageBitmap(bitmap);
                                preferenceManager.remove(Constants.KEY_IMAGE);
                            }
                            encodedImage = encodeImage(bitmap);
                            encodedImageTreatment = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 600;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
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
                    finish();
                })
                .addOnFailureListener(e -> showToast("Không thể đăng xuất..."));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}