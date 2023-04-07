package com.example.catfisharea.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityCreateMultipleAccountBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Objects;

public class CreateMultipleAccountActivity extends BaseActivity {
    private ActivityCreateMultipleAccountBinding mBinding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCreateMultipleAccountBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        Animatoo.animateSlideLeft(CreateMultipleAccountActivity.this);
        init();
        setListeners();

    }

    private void init() {
        //FireStore
        database = FirebaseFirestore.getInstance();
        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setListeners() {

        // Trở lại màn hình trước
        mBinding.toolbarMultipleAccount.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        mBinding.btnCreate.setOnClickListener(view -> {
            if (checkAvailableInput()) {
                createMultipleAccount();
            }
        });

    }

    //Hàm tạo nhiều tài khoản
    private void createMultipleAccount() {

        // Giả lập trạng thái loading và ẩn lúc tạo
        loading(true);
        database.collection(Constants.KEY_COLLECTION_COMPANY)
                .document(preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {

                    loading(false);
                    DocumentSnapshot documentSnapshot = task.getResult();

                    /*
                     * Admin: 1 8 9 20 = 4
                     * Accountant: 2 3 10 11 12 21 22 = 7
                     * Regional: 4 13 14 23 = 4
                     * Director: 5 6 16 17 24 25  = 6
                     * Worker: 7 15 18 19 26 = 5
                     * 1 2 1 = 4
                     * 2 3 2 = 7
                     * 1 2 1 = 4
                     * 2 3 2 = 7
                     * 1 2 1 = 4
                     * */

                    // Lấy số lượng từng tài khoản
                    int currentAdmin = Integer.parseInt(mBinding.edtAmountAdmin.getText().toString());
                    int currentAccountant = Integer.parseInt(mBinding.edtAmountAccountant.getText().toString());
                    int currentRegionalChief = Integer.parseInt(mBinding.edtAmountRegionalChief.getText().toString());
                    int currentDirector = Integer.parseInt(mBinding.edtAmountDirectory.getText().toString());
                    int currentWorker = Integer.parseInt(mBinding.edtAmountWorker.getText().toString());

                    // Tổng số lượng các account người dùng đang tạo
                    int totalAccount = currentAdmin + currentAccountant + currentRegionalChief + currentDirector + currentWorker;

                    // Lấy số lượng các loại tài khoản của công ty
                    int amountCompanyTotalAccount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_TOTAL_ACCOUNT)));
                    int amountCompanyAdmin = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_ADMIN)));
                    int amountCompanyAccountant = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT)));
                    int amountCompanyRegionalChief = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF)));
                    int amountCompanyDirector = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_DIRECTOR)));
                    int amountCompanyWorker = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_WORKER)));

                    //Tạo các trưởng dữ liệu trong bảng công ty
                    HashMap<String, Object> company = new HashMap<>();
                    company.put(Constants.KEY_COMPANY_TOTAL_ACCOUNT, totalAccount + amountCompanyTotalAccount + "");
                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, currentAdmin + amountCompanyAdmin + "");
                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, currentAccountant + amountCompanyAccountant + "");
                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, currentRegionalChief + amountCompanyRegionalChief + "");
                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, currentDirector + amountCompanyDirector + "");
                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, currentWorker + amountCompanyWorker + "");

                    // Cập nhật số tài khoản hiện tại lên trường công ty
                    database.collection(Constants.KEY_COLLECTION_COMPANY)
                            .document(preferenceManager.getString(Constants.KEY_COMPANY_ID))
                            .update(company);


                    // Lần lượt tạo các tài khoản admin có số điện thoại mặc định từ 0 đến amountAdmin - 1
                    for (int i = amountCompanyTotalAccount; i < currentAdmin + amountCompanyTotalAccount; i++) {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_ADDRESS, "");
                        user.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                        user.put(Constants.KEY_DATEOFBIRTH, "");
                        user.put(Constants.KEY_IMAGE, "");
                        user.put(Constants.KEY_NAME, "");
                        user.put(Constants.KEY_PASSWORD, mBinding.edtPasswordDefault.getText().toString());
                        user.put(Constants.KEY_PERSONAL_ID, "");
                        user.put(Constants.KEY_PHONE, "" + i);
                        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ADMIN);

                        database.collection(Constants.KEY_COLLECTION_USER)
                                .add(user);
                    }

                    // Lần lượt tạo các tài khoản kế toán có số điện thoại mặc định từ amountAdmin đến amountAccountant - 1
                    for (int i = currentAdmin + amountCompanyTotalAccount; i < (currentAccountant + currentAdmin + amountCompanyTotalAccount); i++) {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_ADDRESS, "");
                        user.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                        user.put(Constants.KEY_DATEOFBIRTH, "");
                        user.put(Constants.KEY_IMAGE, "");
                        user.put(Constants.KEY_NAME, "");
                        user.put(Constants.KEY_PASSWORD, mBinding.edtPasswordDefault.getText().toString());
                        user.put(Constants.KEY_PERSONAL_ID, "");
                        user.put(Constants.KEY_PHONE, "" + i);
                        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ACCOUNTANT);

                        database.collection(Constants.KEY_COLLECTION_USER)
                                .add(user);
                    }

                    // Lần lượt tạo các tài khoản trưởng vùng có số điện thoại mặc định từ amountAccountant đến amountRegionalChief - 1
                    for (int i = (currentAccountant + currentAdmin + amountCompanyTotalAccount);
                         i < (currentAccountant + currentAdmin + currentRegionalChief + amountCompanyTotalAccount); i++) {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_ADDRESS, "");
                        user.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                        user.put(Constants.KEY_DATEOFBIRTH, "");
                        user.put(Constants.KEY_IMAGE, "");
                        user.put(Constants.KEY_NAME, "");
                        user.put(Constants.KEY_PASSWORD, mBinding.edtPasswordDefault.getText().toString());
                        user.put(Constants.KEY_PERSONAL_ID, "");
                        user.put(Constants.KEY_PHONE, "" + i);
                        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF);

                        database.collection(Constants.KEY_COLLECTION_USER)
                                .add(user);
                    }
                    // Lần lượt tạo các tài khoản trưởng khu có số điện thoại mặc định từ amountRegionalChief đến amountDirector - 1

                    for (int i = (currentAccountant + currentAdmin + currentRegionalChief + amountCompanyTotalAccount);
                         i < (currentAccountant + currentAdmin + currentRegionalChief + currentDirector + amountCompanyTotalAccount); i++) {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_ADDRESS, "");
                        user.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                        user.put(Constants.KEY_DATEOFBIRTH, "");
                        user.put(Constants.KEY_IMAGE, "");
                        user.put(Constants.KEY_NAME, "");
                        user.put(Constants.KEY_PASSWORD, mBinding.edtPasswordDefault.getText().toString());
                        user.put(Constants.KEY_PERSONAL_ID, "");
                        user.put(Constants.KEY_PHONE, "" + i);
                        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR);

                        database.collection(Constants.KEY_COLLECTION_USER)
                                .add(user);
                    }
                    // Lần lượt tạo các tài khoản công nhân có số điện thoại mặc định từ amountDirector đến amountWorker - 1

                    for (int i = (currentAccountant + currentAdmin + currentRegionalChief + currentDirector + amountCompanyTotalAccount);
                         i < (currentAccountant + currentAdmin + currentRegionalChief + currentDirector + currentWorker + amountCompanyTotalAccount); i++) {
                        HashMap<String, Object> user = new HashMap<>();
                        user.put(Constants.KEY_ADDRESS, "");
                        user.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                        user.put(Constants.KEY_DATEOFBIRTH, "");
                        user.put(Constants.KEY_IMAGE, "");
                        user.put(Constants.KEY_NAME, "");
                        user.put(Constants.KEY_PASSWORD, mBinding.edtPasswordDefault.getText().toString());
                        user.put(Constants.KEY_PERSONAL_ID, "");
                        user.put(Constants.KEY_PHONE, "" + i);
                        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER);

                        database.collection(Constants.KEY_COLLECTION_USER)
                                .add(user);
                    }


                    showToast("Đã tạo các tài khoản thành công!");
                    mBinding.edtAmountAdmin.setText("");
                    mBinding.edtAmountAccountant.setText("");
                    mBinding.edtAmountRegionalChief.setText("");
                    mBinding.edtAmountDirectory.setText("");
                    mBinding.edtAmountWorker.setText("");

                });

    }

    // Hàm kiểm tra người dùng có nhập đủ dữ liệu hay không
    private boolean checkAvailableInput() {

        if (mBinding.edtAmountAdmin.getText().toString().isEmpty()) {
            showToast("Số tài khoản admin đang trống!");
            return false;
        } else if (mBinding.edtAmountAccountant.getText().toString().isEmpty()) {
            showToast("Số tài khoản kế toán đang trống!");
            return false;
        } else if (mBinding.edtAmountRegionalChief.getText().toString().isEmpty()) {
            showToast("Số tài khoản trưởng vùng đang trống!");
            return false;
        } else if (mBinding.edtAmountDirectory.getText().toString().isEmpty()) {
            showToast("Số tài khoản trưởng khu đang trống!");
            return false;
        } else if (mBinding.edtAmountWorker.getText().toString().isEmpty()) {
            showToast("Số tài khoản công nhân đang trống!");
            return false;
        } else if (mBinding.edtPasswordDefault.getText().toString().isEmpty()) {
            showToast("Mật khẩu mặc định đang trống!");
            return false;
        } else {
            return true;
        }
    }

    // Hàm giả lập trạng thái loading và ẩn lúc tạo
    private void loading(Boolean isLoading) {
        if (isLoading) {
            mBinding.btnCreate.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnCreate.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}