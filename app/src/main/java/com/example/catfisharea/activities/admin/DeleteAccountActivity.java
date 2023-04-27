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
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityDeleteAccountBinding;
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

public class DeleteAccountActivity extends BaseActivity implements MultipleListener {
    private ActivityDeleteAccountBinding mBinding;
    private PreferenceManager preferenceManager;
    private MultipleUserSelectionAdapter usersAdapter;
    private List<User> users;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDeleteAccountBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        init();
        getUsers();
        setListeners();
    }

    private void init(){

        //FireStore
        database = FirebaseFirestore.getInstance();

        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        //List
        users = new ArrayList<>();

        //Adapter
        usersAdapter = new MultipleUserSelectionAdapter(users, this);
        mBinding.userRecyclerview.setAdapter(usersAdapter);
    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void setListeners(){
        mBinding.toolbarDeleteAccount.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.btnDelete.setOnClickListener(view -> openConfirmDialog());

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

        // Chọn tất cả hoặc bỏ chọn tất cả tài khoản
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

    @SuppressLint("NotifyDataSetChanged")
    private void deleteUser(){

        loading(true);
        List<User> selectedUsers = usersAdapter.getSelectedUser();
        if (selectedUsers.size() == 0 ){
            showToast("Hãy chọn ít nhất một tài khoản!");
            loading(false);
        } else {
            loading(false);
            for (int i = 0; i < selectedUsers.size(); i++){

                // Xóa các tài khoản được chọn trên giao diện
                users.remove(selectedUsers.get(i));
                usersAdapter.notifyDataSetChanged();

                HashMap<String, Object> disableUser = new HashMap<>();
                disableUser.put(Constants.KEY_DISABLE_USER, "1");

                database.collection(Constants.KEY_COLLECTION_USER)
                        .document(selectedUsers.get(i).id)
                        .update(disableUser)
                        .addOnCompleteListener(unused -> showToast("Đã xóa những tài khoản được chọn!"))
                        .addOnFailureListener(e -> showToast("Đã xảy ra lỗi trong quá trình xóa!"));

            }
        }

    }

    // Hàm mở hộp thoại hiển thị các khu hoặc khu
    @SuppressLint("SetTextI18n")
    private void openConfirmDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_confirm_delete_user);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnDelete = dialog.findViewById(R.id.btnDelete);

        btnDelete.setOnClickListener(view -> {
            dialog.dismiss();
            deleteUser();
        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

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
        final Dialog dialog = new Dialog(DeleteAccountActivity.this);
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
        loading(true);
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
                            }
                        }
                        usersAdapter.notifyDataSetChanged();
                        loading(false);
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
            mBinding.btnDelete.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else{
            mBinding.btnDelete.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.GONE);
        }
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }
}