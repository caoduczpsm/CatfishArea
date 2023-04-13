package com.example.catfisharea.activities.worker;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWorkerHomeBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.activities.director.RequestManagementActivity;
import com.example.catfisharea.activities.director.TaskManagerActivity;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;


public class WorkerHomeActivity extends BaseActivity {

    private ActivityWorkerHomeBinding binding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private Pond pond;
    private Task fixedTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWorkerHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListener();
    }

    @SuppressLint("SetTextI18n")
    private void init(){
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();

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
                });

        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .whereEqualTo(Constants.KEY_TASK_TITLE, Constants.KEY_FIXED_TASK_FEED_FISH)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        List<String> receiverFeedFishTask = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);
                        assert receiverFeedFishTask != null;
                        if (receiverFeedFishTask.contains(preferenceManager.getString(Constants.KEY_USER_ID))){
                            fixedTask = new Task();
                            fixedTask.id = queryDocumentSnapshot.getId();
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
                            fixedTask = new Task();
                            fixedTask.id = queryDocumentSnapshot.getId();
                            binding.layoutHome.cardEnvironment.setVisibility(View.VISIBLE);
                        } else {
                            binding.layoutHome.cardEnvironment.setVisibility(View.GONE);
                        }

                    }
                });

        if (preferenceManager.getString(Constants.KEY_NOW) == null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                preferenceManager.putString(Constants.KEY_NOW, String.valueOf(LocalDate.now()));
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!preferenceManager.getString(Constants.KEY_NOW).equals(String.valueOf(LocalDate.now()))){
                List<String> amountFed = pond.getAmountFeedList();
                for (int i = 0; i < amountFed.size(); i++){
                    if (!amountFed.get(i).equals("0")){
                        amountFed.set(i, "0");
                    }
                }
                HashMap<String, Object> unCompletedTask = new HashMap<>();
                unCompletedTask.put(Constants.KEY_STATUS_TASK, Constants.KEY_UNCOMPLETED);
                database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                        .document(fixedTask.id)
                        .update(unCompletedTask);
                
                int totalFeedInDate = 0;
                for (String num : pond.getAmountFeedList()){
                    totalFeedInDate = totalFeedInDate + Integer.parseInt(num);
                }
                int finalTotalFeedInDate = totalFeedInDate;
                database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                        .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                        .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                        .get()
                        .addOnCompleteListener(task -> {

                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                        .document(queryDocumentSnapshot.getId())
                                        .collection(Constants.KEY_COLLECTION_CATEGORY)
                                        .whereEqualTo(Constants.KEY_NAME, Constants.KEY_CATEGORY_FOOD)
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

            }
        }
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

    }

    private void setFeedDialog(int numOfFeed){
        Dialog dialog = openDialog(R.layout.layout_dialog_set_feed_for_fish);
        assert dialog != null;

        Button btnSave, btnClose;

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
                                    .document(fixedTask.id)
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