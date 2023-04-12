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

        setVisibleFood(pond.getNumOfFeeding(), pond.getNumOfFeedingList().get(0), pond.getNumOfFeedingList().get(1),
                pond.getNumOfFeedingList().get(2), pond.getNumOfFeedingList().get(3), pond.getNumOfFeedingList().get(4),
                pond.getNumOfFeedingList().get(5), pond.getNumOfFeedingList().get(6), pond.getNumOfFeedingList().get(7));

    }

    private void setListeners() {
        binding.layoutShowWorker.setOnClickListener(view -> openShowWorkerDialog());

        binding.layoutSettingFeed.setOnClickListener(view -> openSettingNumOfFeedingDialog());
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