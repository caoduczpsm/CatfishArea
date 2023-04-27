package com.example.catfisharea.activities.admin;

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
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityPermissionEditBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PermissionEditActivity extends BaseActivity implements MultipleListener{

    private ActivityPermissionEditBinding mBinding;
    private ArrayList<String> listTypeAccount;
    private PreferenceManager preferenceManager;
    private MultipleUserSelectionAdapter usersAdapter;
    private List<User> users;
    private FirebaseFirestore database;
    private String typeAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPermissionEditBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();

        getUsers();
        setListeners();
    }

    private void init(){
        Animatoo.animateSlideLeft(this);
        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        //FireStore
        database = FirebaseFirestore.getInstance();

//        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
//        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        if (image != null) {
//            imageProfile.setImageBitmap(image);
//        }


        //List
        users = new ArrayList<>();

        //Adapter
        usersAdapter = new MultipleUserSelectionAdapter(users, this);
        mBinding.userRecyclerview.setAdapter(usersAdapter);

        //ArrayList
        listTypeAccount = new ArrayList<>();
        listTypeAccount.add("Admin");
        listTypeAccount.add("Kế Toán");
        listTypeAccount.add("Trưởng Vùng");
        listTypeAccount.add("Trưởng Khu");
        listTypeAccount.add("Công Nhân");
        typeAccount = Constants.KEY_WORKER;

        //ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, listTypeAccount);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spinnerTypeAccount.setAdapter(adapter);

    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void setListeners(){
        mBinding.toolbarPermisionEdit.setNavigationOnClickListener(view -> onBackPressed());

        mBinding.btnEdit.setOnClickListener(view -> {

            loading(true);
            List<User> selectedUsers = usersAdapter.getSelectedUser();

            if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Admin")) {
                typeAccount = Constants.KEY_ADMIN;
            } else if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Kế Toán")) {
                typeAccount = Constants.KEY_ACCOUNTANT;
            } else if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Trưởng Vùng")) {
                typeAccount = Constants.KEY_REGIONAL_CHIEF;
            } else if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Trưởng Khu")) {
                typeAccount = Constants.KEY_DIRECTOR;
            } else typeAccount = Constants.KEY_WORKER;

            for (int i = 0; i < selectedUsers.size(); i++) {
                HashMap<String, Object> permission = new HashMap<>();
                permission.put(Constants.KEY_TYPE_ACCOUNT, typeAccount);
                int finalI = i;
                database.collection(Constants.KEY_COLLECTION_USER)
                        .document(selectedUsers.get(i).id)
                        .update(permission)
                        .addOnSuccessListener(runnable -> {
                            if (finalI == 0){
                                showToast("Đã đổi quyền thành công");
                            }
                        })
                        .addOnFailureListener(runnable -> showToast("Đổi quyền thất bại"));
            }



        });

        // Chọn loại tài khoản cần tạo
        mBinding.spinnerTypeAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (listTypeAccount.get(i)) {
                    case Constants.KEY_ADMIN:
                        typeAccount = Constants.KEY_ADMIN;
                        break;
                    case Constants.KEY_ACCOUNTANT:
                        typeAccount = Constants.KEY_ACCOUNTANT;
                        break;
                    case Constants.KEY_REGIONAL_CHIEF:
                        typeAccount = Constants.KEY_REGIONAL_CHIEF;
                        break;
                    case Constants.KEY_DIRECTOR:
                        typeAccount = Constants.KEY_DIRECTOR;
                        break;
                    default:
                        typeAccount = Constants.KEY_WORKER;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeAccount = Constants.KEY_WORKER;
            }
        });

        // Tìm kiếm tài khoản
        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                usersAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                usersAdapter.getFilter().filter(inputSearch);
                return true;
            }
        });

        // Mở hộp thoại bộ lọc
        mBinding.imageFilter.setOnClickListener(view -> openFilterSearchDialog());

        // Chọn hoặc bỏ chọn tất cả tài khoản
        mBinding.cbAllAccount.setOnClickListener(view -> {
            if (mBinding.cbAllAccount.getText().toString().equals("Tất Cả")){
                usersAdapter.setAllSelected(users);
                mBinding.cbAllAccount.setText("Bỏ Chọn");
            } else {
                usersAdapter.setAllUnSelected(users);
                mBinding.cbAllAccount.setText("Tất Cả");
            }
        });

    }

    // Hàm mở họp thoại tài khoản
    @SuppressLint("SetTextI18n")
    private void openFilterSearchDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_filter_search);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);

        //CheckBox
        CheckBox cbAllAccount, cbAdmin, cbAccountant, cbRegionalChief, cbDirector, cbWorker;

        cbAllAccount = dialog.findViewById(R.id.cbAllAccount);
        cbAdmin = dialog.findViewById(R.id.cbAdmin);
        cbAccountant = dialog.findViewById(R.id.cbAccountant);
        cbRegionalChief = dialog.findViewById(R.id.cbRegionalChief);
        cbDirector = dialog.findViewById(R.id.cbDirector);
        cbWorker = dialog.findViewById(R.id.cbWorker);

        cbAdmin.setOnClickListener(view -> {
            if (cbAccountant.isChecked() && cbRegionalChief.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbAccountant.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbRegionalChief.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbRegionalChief.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbDirector.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbRegionalChief.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbWorker.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbRegionalChief.isChecked() && cbDirector.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        // Chọn tất cả loại tài khoản
        cbAllAccount.setOnClickListener(view -> {
            if (cbAllAccount.getText().toString().equals("Tất Cả")){
                cbAdmin.setChecked(true);
                cbAccountant.setChecked(true);
                cbRegionalChief.setChecked(true);
                cbDirector.setChecked(true);
                cbWorker.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            } else {
                cbAdmin.setChecked(false);
                cbAccountant.setChecked(false);
                cbRegionalChief.setChecked(false);
                cbDirector.setChecked(false);
                cbWorker.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }

        });

        // Bấm áp dụng
        btnApply.setOnClickListener(view -> {
            // Nếu checkbox tất cả tài khoản hoặc tất cả vùng được chọn thì hiển thị ra tất cả người dùng
            if (cbAllAccount.isChecked()){
                users.clear();
                getUsers();
                dialog.dismiss();
            } else {
                users.clear();
                // Ngược lại thì truyền loại tài khoản người dùng vào hàm bộ lọc
                if (cbAdmin.isChecked()){
                    getFilterUser(Constants.KEY_ADMIN);
                }

                if (cbAccountant.isChecked()){
                    getFilterUser(Constants.KEY_ACCOUNTANT);
                }

                if (cbRegionalChief.isChecked()){
                    getFilterUser(Constants.KEY_REGIONAL_CHIEF);
                }

                if (cbDirector.isChecked()){
                    getFilterUser(Constants.KEY_DIRECTOR);
                }

                if (cbWorker.isChecked()){
                    getFilterUser(Constants.KEY_WORKER);
                }

                dialog.dismiss();
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    // Hàm hiển thị người dùng theo bộ lọc
    @SuppressLint("NotifyDataSetChanged")
    private void getFilterUser(String type){
        // Kiểm tra nếu tài khoản có cùng loại với bộ lọc thì mới hiển thị
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, type)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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

                    }

                });
    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(PermissionEditActivity.this);
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

    // Hàm lấy các user từ database về và hiển thị ra recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private void getUsers(){

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
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

                    }

                });

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

    // Hàm giả lập trạng thái loading và ẩn lúc tạo
    private void loading(Boolean isLoading){
        if(isLoading){
            mBinding.btnEdit.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else{
            mBinding.btnEdit.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}