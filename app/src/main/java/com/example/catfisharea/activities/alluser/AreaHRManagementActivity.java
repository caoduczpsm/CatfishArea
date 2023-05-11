package com.example.catfisharea.activities.alluser;

import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.SearchView;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityAreaHrmanagementBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.UserPickerAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.listeners.PickUserListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class AreaHRManagementActivity extends BaseActivity implements MultipleListener, PickUserListener {

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private ActivityAreaHrmanagementBinding binding;
    private List<User> users;
    private List<String> campusName;
    private List<String> pondName;
    private MultipleUserSelectionAdapter multipleUserSelectionAdapter;
    private String campusOrPondNeedToChange;

    private UserPickerAdapter mAdapter;
    private ArrayList<User> mUsers;
    private User magager;
    private Dialog dialog;
    AutoCompleteTextView nameItem;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAreaHrmanagementBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Animatoo.animateSlideLeft(AreaHRManagementActivity.this);

        init();
        setListeners();
    }

    private void init() {

        database = FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());

        users = new ArrayList<>();

        binding.radioCampus.setChecked(true);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            binding.radioGroup.setVisibility(View.VISIBLE);

        //Adapter
        multipleUserSelectionAdapter = new MultipleUserSelectionAdapter(users, this);
        binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
            binding.newBtn.setVisibility(View.GONE);
            getAllPondForDirector();
            getAllWorkerForDirector();
        } else {
            binding.newBtn.setVisibility(View.VISIBLE);
            if (binding.radioCampus.isChecked()) {
                getAllCampus();
                getAllDirectorForRegional();
            } else {
                getAllPondForRegional();
                getAllWorkerForRegional();
            }
        }

        binding.newBtn.setOnClickListener(view -> {
            openAddAccountantDialog();
        });

    }

    private void openAddAccountantDialog() {
        mUsers = new ArrayList<>();
        mAdapter = new UserPickerAdapter(mUsers, this);

        final Dialog dialog = openDialog(R.layout.layout_dialog_add_accountant);
        CircleImageView imageAccountant = dialog.findViewById(R.id.imageAccountant);
        nameItem = dialog.findViewById(R.id.nameItem);
        AppCompatButton btnAdd = dialog.findViewById(R.id.btnAdd);
        AppCompatButton btnClose = dialog.findViewById(R.id.btnClose);

        String areaId = preferenceManager.getString(Constants.KEY_AREA_ID);
        if (areaId == null) return;

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ACCOUNTANT)
                .whereEqualTo(Constants.KEY_AREA_ID, areaId)
                .get().addOnSuccessListener(userQuery -> {
                   if (userQuery != null && userQuery.getDocuments().size() > 0) {
                       nameItem.setText(
                       userQuery.getDocuments().get(0).getString(Constants.KEY_NAME));
                       String image = userQuery.getDocuments().get(0).getString(Constants.KEY_IMAGE);
                       if (image != null) {
                           imageAccountant.setImageBitmap(User.getUserImage(image));
                       }
                   }
                });

        nameItem.setOnClickListener(view -> {
            openPickUserDialog();
        });

        btnAdd.setOnClickListener(view -> {
            if (nameItem.getText().toString().isEmpty()) {
                Toast.makeText(this, "Chọn kế toán", Toast.LENGTH_SHORT);
                return;
            }
            if (magager == null) return;

            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ACCOUNTANT)
                    .whereEqualTo(Constants.KEY_AREA_ID, areaId)
                    .get().addOnSuccessListener(userQuery -> {
                        if (userQuery != null && userQuery.getDocuments().size() > 0) {
                            database.collection(Constants.KEY_COLLECTION_USER)
                                    .document(userQuery.getDocuments().get(0).getId())
                                    .update(Constants.KEY_AREA_ID, FieldValue.delete())
                                    .addOnSuccessListener(command -> {
                                       database.collection(Constants.KEY_COLLECTION_USER)
                                               .document(magager.id)
                                               .update(Constants.KEY_AREA_ID, areaId);
                                       dialog.dismiss();
                                    });
                        }
                    });

        });

        btnClose.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void openPickUserDialog() {
        dialog = openDialog(R.layout.layout_dialog_pick_user);
        assert dialog != null;

        //Button trong dialog
        AppCompatButton no_btn = dialog.findViewById(R.id.btnClose);

        //ConstrainLayout trong dialog
        RecyclerView recyclerView = dialog.findViewById(R.id.recyclerPickUserDialog);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(mAdapter);

        getUser();
        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    private void getUser() {
        mUsers.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ACCOUNTANT)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot qr : queryDocumentSnapshots.getDocuments()) {
                        String id = qr.getId();
                        String name = qr.getString(Constants.KEY_NAME);
                        String image = qr.getString(Constants.KEY_IMAGE);
                        String phone = qr.getString(Constants.KEY_PHONE);
                        String type = qr.getString(Constants.KEY_TYPE_ACCOUNT);
                        String areaId = qr.getString(Constants.KEY_AREA_ID);
                        String disable = qr.getString("disable");
                        if ((areaId == null || areaId.isEmpty()) && (disable == null || disable.equals("0"))) {
                            User user = new User();
                            user.name = name;
                            user.image = image;
                            user.id = id;
                            user.phone = phone;
                            user.position = type;
                            mUsers.add(user);
                        }
                    }
                    mAdapter.notifyDataSetChanged();
                });
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
                if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
                    if (binding.radioCampus.isChecked()) {
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
                if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
                    if (binding.radioCampus.isChecked()) {
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
            for (User user : selectedUser) {
                HashMap<String, Object> newCampus = new HashMap<>();

                database.collection(Constants.KEY_COLLECTION_CAMPUS)
                        .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                        .whereEqualTo(Constants.KEY_NAME, campusOrPondNeedToChange)
                        .get()
                        .addOnCompleteListener(task -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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
            for (User user : selectedUser) {
                HashMap<String, Object> newPond = new HashMap<>();

                database.collection(Constants.KEY_COLLECTION_POND)
                        .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                        .whereEqualTo(Constants.KEY_NAME, campusOrPondNeedToChange)
                        .get()
                        .addOnCompleteListener(task -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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
            for (User user : selectedUser) {
                HashMap<String, Object> newPond = new HashMap<>();

                database.collection(Constants.KEY_COLLECTION_POND)
                        .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                        .whereEqualTo(Constants.KEY_NAME, campusOrPondNeedToChange)
                        .get()
                        .addOnCompleteListener(task -> {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        pondName.add(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                        //ArrayAdapter
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, pondName);
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        binding.spinnerCampusAndPond.setAdapter(adapter);
                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllDirectorForRegional() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_DIRECTOR)) {
                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null) {
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
    private void getAllWorkerForRegional() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)) {

                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null) {
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

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)) {
                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null) {
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
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
                if (binding.radioCampus.isChecked()) {
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

    private void showToast(String message) {
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

    @Override
    public void onClickUser(User user) {
        magager = user;
        if (nameItem != null) {
            nameItem.setText(magager.name);
        }
        dialog.dismiss();
    }
}