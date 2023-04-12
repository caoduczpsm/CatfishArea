package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityPondDetailsBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PondDetailsActivity extends BaseActivity implements UserListener {

    private Pond pond;
    private ActivityPondDetailsBinding binding;
    private FirebaseFirestore database;
    private UsersAdapter usersAdapter;
    private List<User> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPondDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
    }

    @SuppressLint("SetTextI18n")
    void init() {

        database = FirebaseFirestore.getInstance();

        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);

        binding.textName.setText(pond.getName());
        binding.textAcreage.setText(pond.getAcreage() + " (m2)");

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

    }

    private void setListeners() {
        binding.layoutShowWorker.setOnClickListener(view -> openShowWorkerDialog());

        binding.layoutSettingFeed.setOnClickListener(view -> openSettingNumOfFeedingDialog());
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openSettingNumOfFeedingDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_setting_num_of_feeding);
        assert dialog != null;

        Spinner spinnerNumOfFeed = dialog.findViewById(R.id.spinnerNumOfFeed);
        TextInputLayout textOne, textTwo, textThree, textFour, textFive, textSix, textSeven, textEight;
        Button btnClose, btnSave;
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
            edtOne.setText(pond.getSt1Feeding() + "");
        } else if (pond.getNumOfFeeding() == 2) {
            spinnerNumOfFeed.setSelection(2);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
        } else if (pond.getNumOfFeeding() == 3) {
            spinnerNumOfFeed.setSelection(3);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
            edtThree.setText(pond.getSt3Feeding() + "");
        } else if (pond.getNumOfFeeding() == 4) {
            spinnerNumOfFeed.setSelection(4);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
            edtThree.setText(pond.getSt3Feeding() + "");
            edtFour.setText(pond.getSt4Feeding() + "");
        } else if (pond.getNumOfFeeding() == 5) {
            spinnerNumOfFeed.setSelection(5);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
            edtThree.setText(pond.getSt3Feeding() + "");
            edtFour.setText(pond.getSt4Feeding() + "");
            edtFive.setText(pond.getSt5Feeding() + "");
        } else if (pond.getNumOfFeeding() == 6) {
            spinnerNumOfFeed.setSelection(6);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            textSix.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
            edtThree.setText(pond.getSt3Feeding() + "");
            edtFour.setText(pond.getSt4Feeding() + "");
            edtFive.setText(pond.getSt5Feeding() + "");
            edtSix.setText(pond.getSt6Feeding() + "");
        } else if (pond.getNumOfFeeding() == 7) {
            spinnerNumOfFeed.setSelection(7);
            textOne.setVisibility(View.VISIBLE);
            textTwo.setVisibility(View.VISIBLE);
            textThree.setVisibility(View.VISIBLE);
            textFour.setVisibility(View.VISIBLE);
            textFive.setVisibility(View.VISIBLE);
            textSix.setVisibility(View.VISIBLE);
            textSeven.setVisibility(View.VISIBLE);
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
            edtThree.setText(pond.getSt3Feeding() + "");
            edtFour.setText(pond.getSt4Feeding() + "");
            edtFive.setText(pond.getSt5Feeding() + "");
            edtSix.setText(pond.getSt6Feeding() + "");
            edtSeven.setText(pond.getSt7Feeding() + "");
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
            edtOne.setText(pond.getSt1Feeding() + "");
            edtTwo.setText(pond.getSt2Feeding() + "");
            edtThree.setText(pond.getSt3Feeding() + "");
            edtFour.setText(pond.getSt4Feeding() + "");
            edtFive.setText(pond.getSt5Feeding() + "");
            edtSix.setText(pond.getSt6Feeding() + "");
            edtSeven.setText(pond.getSt7Feeding() + "");
            edtEight.setText(pond.getSt8Feeding() + "");
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

            int st_1 = 0,st_2 = 0, st_3 = 0, st_4 = 0, st_5=0, st_6 = 0, st_7 =0, st_8 = 0;
            boolean isValidData = true;

            int numOfFeeding;
            if (spinnerNumOfFeed.getSelectedItem().equals("0")){
                numOfFeeding = 0;
            } else if (spinnerNumOfFeed.getSelectedItem().equals("1")){
                numOfFeeding = 1;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("2")){
                numOfFeeding = 2;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("3")){
                numOfFeeding = 3;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                    st_3 = Integer.parseInt(Objects.requireNonNull(edtThree.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("4")){
                numOfFeeding = 4;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                    st_3 = Integer.parseInt(Objects.requireNonNull(edtThree.getText()).toString());
                    st_4 = Integer.parseInt(Objects.requireNonNull(edtFour.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("5")){
                numOfFeeding = 5;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                    st_3 = Integer.parseInt(Objects.requireNonNull(edtThree.getText()).toString());
                    st_4 = Integer.parseInt(Objects.requireNonNull(edtFour.getText()).toString());
                    st_5 = Integer.parseInt(Objects.requireNonNull(edtFive.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("6")){
                numOfFeeding = 6;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                    st_3 = Integer.parseInt(Objects.requireNonNull(edtThree.getText()).toString());
                    st_4 = Integer.parseInt(Objects.requireNonNull(edtFour.getText()).toString());
                    st_5 = Integer.parseInt(Objects.requireNonNull(edtFive.getText()).toString());
                    st_6 = Integer.parseInt(Objects.requireNonNull(edtSix.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else if (spinnerNumOfFeed.getSelectedItem().equals("7")){
                numOfFeeding = 7;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                    st_3 = Integer.parseInt(Objects.requireNonNull(edtThree.getText()).toString());
                    st_4 = Integer.parseInt(Objects.requireNonNull(edtFour.getText()).toString());
                    st_5 = Integer.parseInt(Objects.requireNonNull(edtFive.getText()).toString());
                    st_6 = Integer.parseInt(Objects.requireNonNull(edtSix.getText()).toString());
                    st_7 = Integer.parseInt(Objects.requireNonNull(edtSeven.getText()).toString());
                } else {
                    isValidData = false;
                }
            } else {
                numOfFeeding = 8;
                if (!Objects.requireNonNull(Objects.requireNonNull(edtOne.getText())).toString().equals("")){
                    st_1 = Integer.parseInt(Objects.requireNonNull(edtOne.getText()).toString());
                    st_2 = Integer.parseInt(Objects.requireNonNull(edtTwo.getText()).toString());
                    st_3 = Integer.parseInt(Objects.requireNonNull(edtThree.getText()).toString());
                    st_4 = Integer.parseInt(Objects.requireNonNull(edtFour.getText()).toString());
                    st_5 = Integer.parseInt(Objects.requireNonNull(edtFive.getText()).toString());
                    st_6 = Integer.parseInt(Objects.requireNonNull(edtSix.getText()).toString());
                    st_7 = Integer.parseInt(Objects.requireNonNull(edtSeven.getText()).toString());
                    st_8 = Integer.parseInt(Objects.requireNonNull(edtEight.getText()).toString());
                } else {
                    isValidData = false;
                }
            }
            if (isValidData) {
                HashMap<String, Object> update = new HashMap<>();
                update.put(Constants.KEY_NUM_OF_FEEDING, numOfFeeding + "");
                update.put(Constants.KEY_NUM_1ST, st_1 + "");
                update.put(Constants.KEY_NUM_2ST, st_2 + "");
                update.put(Constants.KEY_NUM_3ST, st_3 + "");
                update.put(Constants.KEY_NUM_4ST, st_4 + "");
                update.put(Constants.KEY_NUM_5ST, st_5 + "");
                update.put(Constants.KEY_NUM_6ST, st_6 + "");
                update.put(Constants.KEY_NUM_7ST, st_7 + "");
                update.put(Constants.KEY_NUM_8ST, st_8 + "");


                database.collection(Constants.KEY_COLLECTION_POND)
                        .document(pond.getId())
                        .update(update)
                        .addOnSuccessListener(unused -> {
                            showToast("Cập nhật số lần cho ăn trong ngày thành công!");
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
    private void openShowWorkerDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_show_worker_in_pond);
        assert dialog != null;

        TextView textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText("Danh sách công nhân làm việc tại ao " + pond.getName());

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> dialog.dismiss());

        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);

        userRecyclerView.setAdapter(usersAdapter);

        users.clear();

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
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
                });
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

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserClicker(User user) {

    }
}