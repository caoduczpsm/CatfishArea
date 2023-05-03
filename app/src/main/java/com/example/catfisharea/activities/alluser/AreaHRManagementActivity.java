package com.example.catfisharea.activities.alluser;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;

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
import android.widget.TextView;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityAreaHrmanagementBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class AreaHRManagementActivity extends BaseActivity implements MultipleListener {

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private ActivityAreaHrmanagementBinding binding;
    private List<User> users;
    private List<String> campusName;
    private List<String> pondName;
    private MultipleUserSelectionAdapter multipleUserSelectionAdapter;
    private String campusOrPondNeedToChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAreaHrmanagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Animatoo.animateSlideLeft(AreaHRManagementActivity.this);

        init();
        setListeners();
    }

    private void init(){

        database = FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());

        users = new ArrayList<>();

        binding.radioCampus.setChecked(true);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            binding.radioGroup.setVisibility(View.VISIBLE);

        //Adapter
        multipleUserSelectionAdapter = new MultipleUserSelectionAdapter(users, this);
        binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
            getAllPondForDirector();
            getAllWorkerForDirector();
        } else {
            if (binding.radioCampus.isChecked()){
                getAllCampus();
                getAllDirectorForRegional();
            } else {
                getAllPondForRegional();
                getAllWorkerForRegional();
            }
        }

    }

    private void setListeners() {
        binding.toolbarHRManagement.setNavigationOnClickListener(view -> onBackPressed());

        binding.radioCampus.setOnClickListener(view -> {
            getAllDirectorForRegional();
            getAllCampus();
        });

        binding.radioPond.setOnClickListener(view -> {
            getAllWorkerForRegional();
            getAllPondForRegional();
        });

        // Tìm kiếm tài khoản
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                multipleUserSelectionAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                multipleUserSelectionAdapter.getFilter().filter(inputSearch);
                return true;
            }
        });

        // Chọn loại khu hoặc vùng cần đổi
        binding.spinnerCampusAndPond.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                    if (binding.radioCampus.isChecked()){
                        campusOrPondNeedToChange = campusName.get(i);
                    } else {
                        campusOrPondNeedToChange = pondName.get(i);
                    }
                } else {
                    campusOrPondNeedToChange = pondName.get(i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                    if (binding.radioCampus.isChecked()){
                        campusOrPondNeedToChange = campusName.get(0);
                    } else {
                        campusOrPondNeedToChange = pondName.get(0);
                    }
                } else {
                    campusOrPondNeedToChange = pondName.get(0);
                }
            }
        });

        // Đổi khu hoặc ao cho trưởng khu hoặc công nhân
        binding.btnApply.setOnClickListener(view -> openConfirmDialog());
    }

    private void changeDirectorToNewCampus() {

        List<User> selectedUser = multipleUserSelectionAdapter.getSelectedUser();
        if (selectedUser.size() == 0) {
            showToast("Vui lòng chọn một trưởng khu cần đổi khu khác!");
        } else if (selectedUser.size() > 1) {
            showToast("Bạn chỉ có thể chọn một trưởng khu cho khu này!");
        } else {
            for (User user : selectedUser){
                HashMap<String, Object> newCampus = new HashMap<>();

                database.collection(Constants.KEY_COLLECTION_CAMPUS)
                        .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                        .whereEqualTo(Constants.KEY_NAME, campusOrPondNeedToChange)
                        .get()
                        .addOnCompleteListener(task -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                newCampus.put(Constants.KEY_CAMPUS_ID, queryDocumentSnapshot.getId());

                                database.collection(Constants.KEY_COLLECTION_USER)
                                        .document(user.id)
                                        .update(newCampus)
                                        .addOnCompleteListener(task1 -> {
                                            showToast("Đổi trưởng khu thành công!");
                                            showToast("Khu vừa đổi hiện có hai trưởng khu khác nhau! Hãy đổi trưởng khu cũ sang một khu mới!");
                                        });

                            }
                        });
            }
        }

    }

    private void changeWorkerToNewPondOfRegional() {

        List<User> selectedUser = multipleUserSelectionAdapter.getSelectedUser();
        if (selectedUser.size() == 0) {
            showToast("Vui lòng chọn ít nhất một công nhân cần đổi ao khác!");
        } else {
            for (User user : selectedUser){
                HashMap<String, Object> newPond = new HashMap<>();

                database.collection(Constants.KEY_COLLECTION_POND)
                        .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                        .whereEqualTo(Constants.KEY_NAME, campusOrPondNeedToChange)
                        .get()
                        .addOnCompleteListener(task -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                newPond.put(Constants.KEY_POND_ID, queryDocumentSnapshot.getId());

                                database.collection(Constants.KEY_COLLECTION_USER)
                                        .document(user.id)
                                        .update(newPond)
                                        .addOnCompleteListener(task1 -> showToast("Đổi đổi các công nhân được chọn sang ao mới!"));

                            }
                        });
            }
        }

    }

    private void changeWorkerToNewPondOfDirector() {
        List<User> selectedUser = multipleUserSelectionAdapter.getSelectedUser();
        if (selectedUser.size() == 0) {
            showToast("Vui lòng chọn ít nhất một công nhân cần đổi ao khác!");
        } else {
            for (User user : selectedUser){
                HashMap<String, Object> newPond = new HashMap<>();

                database.collection(Constants.KEY_COLLECTION_POND)
                        .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                        .whereEqualTo(Constants.KEY_NAME, campusOrPondNeedToChange)
                        .get()
                        .addOnCompleteListener(task -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                newPond.put(Constants.KEY_POND_ID, queryDocumentSnapshot.getId());

                                database.collection(Constants.KEY_COLLECTION_USER)
                                        .document(user.id)
                                        .update(newPond)
                                        .addOnCompleteListener(task1 -> showToast("Đổi đổi các công nhân được chọn sang ao mới!"));

                            }
                        });
            }
        }
    }

    private void getAllCampus() {
        campusName = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        campusName.add(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                        //ArrayAdapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, campusName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCampusAndPond.setAdapter(adapter);
                    }

                });
    }

    private void getAllPondForRegional() {
        pondName = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        pondName.add(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                        //ArrayAdapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, pondName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCampusAndPond.setAdapter(adapter);
                    }

                });
    }

    private void getAllPondForDirector() {
        pondName = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        pondName.add(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                        //ArrayAdapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, pondName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCampusAndPond.setAdapter(adapter);
                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllDirectorForRegional(){
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_DIRECTOR)){
                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                    User user = new User();
                                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                    user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.address = queryDocumentSnapshot.getString(Constants.KEY_ADDRESS);
                                    user.dateOfBirth = queryDocumentSnapshot.getString(Constants.KEY_DATEOFBIRTH);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                    user.id = queryDocumentSnapshot.getId();
                                    users.add(user);
                                    binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);
                                    multipleUserSelectionAdapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllWorkerForRegional(){
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)){

                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                    User user = new User();
                                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                    user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.address = queryDocumentSnapshot.getString(Constants.KEY_ADDRESS);
                                    user.dateOfBirth = queryDocumentSnapshot.getString(Constants.KEY_DATEOFBIRTH);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                    user.id = queryDocumentSnapshot.getId();
                                    users.add(user);
                                    binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);
                                    multipleUserSelectionAdapter.notifyDataSetChanged();
                                }

                            }

                        }

                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllWorkerForDirector() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)){
                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                    User user = new User();
                                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                    user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.address = queryDocumentSnapshot.getString(Constants.KEY_ADDRESS);
                                    user.dateOfBirth = queryDocumentSnapshot.getString(Constants.KEY_DATEOFBIRTH);
                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                    user.id = queryDocumentSnapshot.getId();
                                    users.add(user);
                                    binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);
                                    multipleUserSelectionAdapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }

                });
    }

    // Hàm mở hộp xác nhận việc đổi trưởng khu qua khu mới hoặc công nhân qua ao mới
    @SuppressLint("SetTextI18n")
    private void openConfirmDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_confirm_delete_user);
        assert dialog != null;

        //Button trong dialog
        AppCompatButton no_btn = dialog.findViewById(R.id.btnClose);
        AppCompatButton btnDelete = dialog.findViewById(R.id.btnDelete);
        TextView textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText("Bạn có chắc chắn việc thay đổi này hay không?");
        btnDelete.setText("Đổi");
        btnDelete.setOnClickListener(view -> {
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                if (binding.radioCampus.isChecked()){
                    changeDirectorToNewCampus();
                } else {
                    changeWorkerToNewPondOfRegional();
                }
            } else {
                changeWorkerToNewPondOfDirector();
            }
            multipleUserSelectionAdapter.setUserUnSelected(users);
            dialog.dismiss();
        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(AreaHRManagementActivity.this);
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

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }
}