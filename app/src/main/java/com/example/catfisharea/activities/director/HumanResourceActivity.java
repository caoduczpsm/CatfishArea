package com.example.catfisharea.activities.director;

import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityHumanResourceBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleCampusSelectionAdapter;
import com.example.catfisharea.adapter.MultiplePondSelectionAdapter;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.MultipleCampusListener;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class HumanResourceActivity extends BaseActivity implements MultipleCampusListener, MultipleListener, UserListener {

    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private ActivityHumanResourceBinding binding;
    private RecyclerView campusRecyclerView;
    private List<Campus> campuses;
    private List<Pond> ponds;
    private List<User> users;
    private MultipleCampusSelectionAdapter multipleCampusSelectionAdapter;
    private MultiplePondSelectionAdapter multiplePondSelectionAdapter;
    private MultipleUserSelectionAdapter multipleUserSelectionAdapter;
    private UsersAdapter usersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHumanResourceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Animatoo.animateSlideLeft(HumanResourceActivity.this);
        init();
        setListeners();
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            getAllUsersForShowDetailUserOfRegional();
        else {
            getAllUsersForDirector();
        }
    }

    private void init(){

        database = FirebaseFirestore.getInstance();

        preferenceManager = new PreferenceManager(getApplicationContext());

        campuses = new ArrayList<>();
        ponds = new ArrayList<>();
        users = new ArrayList<>();

        binding.radioShowUserDetail.setChecked(true);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            binding.layoutRadioButton.setVisibility(View.VISIBLE);

        //Adapter
        multipleUserSelectionAdapter = new MultipleUserSelectionAdapter(users, this);
        multiplePondSelectionAdapter = new MultiplePondSelectionAdapter(ponds, this);
        usersAdapter = new UsersAdapter(users, this);
        if (binding.radioShowUserDetail.isChecked())
            binding.usersRecyclerview.setAdapter(usersAdapter);
        else binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);

    }

    private void setListeners(){

        binding.imageFilter.setOnClickListener(view -> openFilterDialog(preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT)));

        binding.toolbarHRManagement.setNavigationOnClickListener(view -> onBackPressed());

        // Tìm kiếm tài khoản
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                    multipleUserSelectionAdapter.getFilter().filter(query);
                } else {
                    usersAdapter.getFilter().filter(query);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                    multipleUserSelectionAdapter.getFilter().filter(inputSearch);
                } else {
                    usersAdapter.getFilter().filter(inputSearch);
                }
                return true;
            }
        });

        binding.radioShowUserDetail.setOnClickListener(view -> {
            binding.usersRecyclerview.setAdapter(usersAdapter);
            binding.btnDelete.setVisibility(View.GONE);
            getAllUsersForShowDetailUserOfRegional();
        });

        binding.radioDeleteUser.setOnClickListener(view -> {
            binding.usersRecyclerview.setAdapter(multipleUserSelectionAdapter);
            binding.btnDelete.setVisibility(View.VISIBLE);
            getAllUsersForDeleteUserOfRegional();
        });

        binding.btnDelete.setOnClickListener(view -> openConfirmDialog());
    }

    // Hàm mở hộp thoại hiển thị các khu hoặc khu
    @SuppressLint("SetTextI18n")
    private void openFilterDialog(String typeAccount) {
        final Dialog dialog = openDialog(R.layout.layout_dialog_campus);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        campusRecyclerView = dialog.findViewById(R.id.campusRecyclerView);

        if (typeAccount.equals(Constants.KEY_REGIONAL_CHIEF))
            getCampuses();
        if (typeAccount.equals(Constants.KEY_DIRECTOR))
            getPonds();

        btnApply.setOnClickListener(view -> {
            if (typeAccount.equals(Constants.KEY_REGIONAL_CHIEF)){
                List<Campus> selectedCampuses = multipleCampusSelectionAdapter.getSelectedCampuses();
                showCampusesSelected(selectedCampuses);
            } else {
                List<Pond> selectedPonds = multiplePondSelectionAdapter.getSelectedPonds();
                showPondSelected(selectedPonds);
            }

            dialog.dismiss();

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    @SuppressLint("NotifyDataSetChanged")
    private void showCampusesSelected(List<Campus> selectedCampuses) {
        users.clear();
        for (int i = 0 ; i <  selectedCampuses.size(); i++){
            int finalI = i;
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                                if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_DIRECTOR) ||
                                        Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER) ||
                                        Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_ACCOUNTANT)){

                                    if (finalI == 0){
                                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_ACCOUNTANT)){
                                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_AREA_ID), preferenceManager.getString(Constants.KEY_AREA_ID))){
                                                if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                                    User user = new User();
                                                    user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                                    user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                                    user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                                    user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                                    user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                                    user.id = queryDocumentSnapshot.getId();
                                                    users.add(user);
                                                    multipleUserSelectionAdapter.notifyDataSetChanged();
                                                }
                                            }
                                        } else if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_CAMPUS_ID), selectedCampuses.get(finalI).getId())){
                                            if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                                User user = new User();
                                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                                user.id = queryDocumentSnapshot.getId();
                                                users.add(user);
                                                multipleUserSelectionAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    } else {
                                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_CAMPUS_ID), selectedCampuses.get(finalI).getId())){
                                            if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                                User user = new User();
                                                user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                                user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                                user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                                user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                                user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                                user.id = queryDocumentSnapshot.getId();
                                                users.add(user);
                                                multipleUserSelectionAdapter.notifyDataSetChanged();
                                            }
                                        }
                                    }
                                }

                            }

                        }

                    });
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void showPondSelected(List<Pond> selectedPonds) {
        users.clear();
        for (int i = 0 ; i <  selectedPonds.size(); i++){
            int finalI = i;
            database.collection(Constants.KEY_COLLECTION_USER)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .get()
                    .addOnCompleteListener(task -> {

                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_POND_ID), selectedPonds.get(finalI).getId())){
                                    if (queryDocumentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                                        User user = new User();
                                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                                        user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                                        user.dateOfBirth = queryDocumentSnapshot.getString(Constants.KEY_DATEOFBIRTH);
                                        user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                                        user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                        user.id = queryDocumentSnapshot.getId();
                                        users.add(user);
                                        binding.usersRecyclerview.setAdapter(usersAdapter);
                                        usersAdapter.notifyDataSetChanged();
                                    }
                                }
                            }

                        }

                    });
        }

    }

    private void getCampuses() {
        campuses.clear();
        database.collection(Constants.KEY_CAMPUS)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        Campus campus = new Campus("","");
                        campus.setId(queryDocumentSnapshot.getId());
                        campus.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                        campuses.add(campus);

                        multipleCampusSelectionAdapter = new MultipleCampusSelectionAdapter(campuses, this);
                        campusRecyclerView.setAdapter(multipleCampusSelectionAdapter);
                        campusRecyclerView.setVisibility(View.VISIBLE);

                    }
                });

    }

    private void getPonds() {
        ponds.clear();

        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_CAMPUS_ID), preferenceManager.getString(Constants.KEY_CAMPUS_ID))){
                            Pond pond = new Pond("","");
                            pond.setId(queryDocumentSnapshot.getId());
                            pond.setName(queryDocumentSnapshot.getString(Constants.KEY_NAME));
                            ponds.add(pond);
                            multiplePondSelectionAdapter = new MultiplePondSelectionAdapter(ponds, this);
                            campusRecyclerView.setAdapter(multiplePondSelectionAdapter);
                            campusRecyclerView.setVisibility(View.VISIBLE);
                        }
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllUsersForShowDetailUserOfRegional() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_DIRECTOR)
                                    || Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)
                                    || Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_ACCOUNTANT)){
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
                                    usersAdapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }

                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllUsersForDeleteUserOfRegional() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_DIRECTOR)
                                    || Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)
                                    || Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_ACCOUNTANT)){
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
                                    multipleUserSelectionAdapter.notifyDataSetChanged();
                                }
                            }

                        }

                    }

                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getAllUsersForDirector() {
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
                                    binding.usersRecyclerview.setAdapter(usersAdapter);
                                    usersAdapter.notifyDataSetChanged();
                                }

                            }

                        }

                    }

                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteUser() {
        List<User> selectedUser = multipleUserSelectionAdapter.getSelectedUser();
        if (selectedUser.size() != 0 ){
            HashMap<String, Object> softDelete = new HashMap<>();
            softDelete.put(Constants.KEY_DISABLE_USER, "1");
            for (User user : selectedUser){
                database.collection(Constants.KEY_COLLECTION_USER)
                        .document(user.id)
                        .update(softDelete)
                        .addOnCompleteListener(task -> {
                            int count = 0;
                            count++;
                            if (count == selectedUser.size()){
                                showToast("Đã xóa tất cả người dùng được chọn");
                            }
                            users.remove(user);
                            multipleUserSelectionAdapter.notifyDataSetChanged();
                        });
            }
        } else {
            showToast("Vui lòng chọn ít nhất một người dùng cần xóa!");
        }

    }

    // Hàm mở hộp xác nhận việc xóa người dùng
    @SuppressLint("SetTextI18n")
    private void openConfirmDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_confirm_delete_user);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(view -> deleteUser());

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(HumanResourceActivity.this);
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
    public void onChangeTeamLeadClicker(Campus campus) {

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onUserClicker(User user) {
        final Dialog dialog = openDialog(R.layout.layout_dialog_user_detail);
        assert dialog != null;

        //TextView
        TextView textName, textPhone, textDateOfBirth, textAddress, textPosition;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        RoundedImageView imageUser = dialog.findViewById(R.id.imageUser);
        textName = dialog.findViewById(R.id.textName);
        textPhone = dialog.findViewById(R.id.textPhone);
        textDateOfBirth = dialog.findViewById(R.id.textDateOfBirth);
        textAddress = dialog.findViewById(R.id.textAddress);
        textPosition = dialog.findViewById(R.id.textPosition);

        byte[] bytes = Base64.decode(user.image, Base64.DEFAULT);
        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        if (image != null) {
            imageUser.setImageBitmap(image);
        }

        textName.setText(user.name);
        textPhone.setText(user.phone);
        textAddress.setText(user.address);
        if (user.position.equals(Constants.KEY_ADMIN))
            textPosition.setText("Admin");
        if (user.position.equals(Constants.KEY_ACCOUNTANT))
            textPosition.setText("Kế Toán");
        if (user.position.equals(Constants.KEY_REGIONAL_CHIEF))
            textPosition.setText("Trưởng Vùng");
        if (user.position.equals(Constants.KEY_DIRECTOR))
            textPosition.setText("Trưởng Khu");
        if (user.position.equals(Constants.KEY_WORKER))
            textPosition.setText("Công Nhân");
        textDateOfBirth.setText(user.dateOfBirth);

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

}