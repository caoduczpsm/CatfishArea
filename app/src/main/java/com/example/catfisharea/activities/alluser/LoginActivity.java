package com.example.catfisharea.activities.alluser;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.app.catfisharea.databinding.ActivityLoginBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.admin.AdminHomeActivity;
import com.example.catfisharea.activities.director.DirectorHomeActivity;
import com.example.catfisharea.activities.personal.PersonalUserHomeActivity;
import com.example.catfisharea.activities.regional_chief.RegionalChiefActivity;
import com.example.catfisharea.activities.worker.WorkerHomeActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.EncryptHandler;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding mBinding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Animatoo.animateSlideLeft(this);
        Animatoo.animateSlideLeft(LoginActivity.this);
        initActivity();
        setListener();
    }

    private void initActivity() {
        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        //FireStore
        database = FirebaseFirestore.getInstance();

        if (preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN)) {
            Intent intent;
            intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_ADMIN)) {
                intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
            } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_ACCOUNTANT)) {
//                intent = new Intent(getApplicationContext(), AccountantHomeActivity.class);
            } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
                intent = new Intent(getApplicationContext(), RegionalChiefActivity.class);
            } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
                intent = new Intent(getApplicationContext(), DirectorHomeActivity.class);
            } else {
                intent = new Intent(getApplicationContext(), WorkerHomeActivity.class);
            }
            //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }

    private void setListener() {

        mBinding.btnLogin.setOnClickListener(view -> logIn());

        mBinding.textRegis.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void logIn() {
        // Giả lập trạng thái loading và ẩn nút đăng nhập
        loading(true);
        String password = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                password = EncryptHandler.encryptPassword(Objects.requireNonNull(mBinding.edtPasswordLogin.getText()).toString());
            }
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_PHONE, Objects.requireNonNull(mBinding.edtPhoneLogin.getText()).toString())
                .whereEqualTo(Constants.KEY_PASSWORD, password)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {

                        // Đọc dữ liệu từ Firebase và lưu vào bộ nhớ tạm
                        DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
                        preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                        preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.getId());
                        preferenceManager.putString(Constants.KEY_PHONE, documentSnapshot.getString(Constants.KEY_PHONE));
                        preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME));
                        preferenceManager.putString(Constants.KEY_PASSWORD, documentSnapshot.getString(Constants.KEY_PASSWORD));
                        preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE));
                        preferenceManager.putString(Constants.KEY_PERSONAL_ID, documentSnapshot.getString(Constants.KEY_PERSONAL_ID));
                        preferenceManager.putString(Constants.KEY_DATEOFBIRTH, documentSnapshot.getString(Constants.KEY_DATEOFBIRTH));
                        preferenceManager.putString(Constants.KEY_ADDRESS, documentSnapshot.getString(Constants.KEY_ADDRESS));
                        preferenceManager.putString(Constants.KEY_TYPE_ACCOUNT, documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT));
                        preferenceManager.putString(Constants.KEY_COMPANY_ID, documentSnapshot.getString(Constants.KEY_COMPANY_ID));
                        showToast("Đăng nhập thành công");
                        Intent intent = null;

                        // Kiểm tra tài khoản đăng nhập có chức vụ gì và chuyển đến màn hình trang chủ tương ứng
                        if (Objects.equals(documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_ADMIN)) {
                            intent = new Intent(getApplicationContext(), AdminHomeActivity.class);
                        } else if (Objects.equals(documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_REGIONAL_CHIEF)) {
                            preferenceManager.putString(Constants.KEY_AREA_ID, documentSnapshot.getString(Constants.KEY_AREA_ID));
                            intent = new Intent(getApplicationContext(), RegionalChiefActivity.class);
                        } else if (Objects.equals(documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_DIRECTOR)) {
                            preferenceManager.putString(Constants.KEY_AREA_ID, documentSnapshot.getString(Constants.KEY_AREA_ID));
                            preferenceManager.putString(Constants.KEY_CAMPUS_ID, documentSnapshot.getString(Constants.KEY_CAMPUS_ID));
                            intent = new Intent(getApplicationContext(), DirectorHomeActivity.class);
                        } else if (Objects.equals(documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_ACCOUNTANT)) {
//                            intent = new Intent(getApplicationContext(), AccountantHomeActivity.class);
                        } else if (Objects.equals(documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT), Constants.KEY_WORKER)) {
                            preferenceManager.putString(Constants.KEY_POND_ID, documentSnapshot.getString(Constants.KEY_POND_ID));
                            preferenceManager.putString(Constants.KEY_CAMPUS_ID, documentSnapshot.getString(Constants.KEY_CAMPUS_ID));
                            preferenceManager.putString(Constants.KEY_AREA_ID, documentSnapshot.getString(Constants.KEY_AREA_ID));
                            if (documentSnapshot.getString(Constants.KEY_TREATMENT_ASSIGNMENT) != null){
                                preferenceManager.putString(Constants.KEY_TREATMENT_ASSIGNMENT, Constants.KEY_TREATMENT_IS_ASSIGNMENT);
                                if (documentSnapshot.getString(Constants.KEY_TREATMENT_ID) != null){
                                    preferenceManager.putString(Constants.KEY_TREATMENT_ID, documentSnapshot.getString(Constants.KEY_TREATMENT_ID));
                                }
                            }
                            intent = new Intent(getApplicationContext(), WorkerHomeActivity.class);
                        } else {
                            intent = new Intent(getApplicationContext(), PersonalUserHomeActivity.class);
                        }
                        assert intent != null;
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        Animatoo.animateSlideLeft(LoginActivity.this);
                        startActivity(intent);
                    } else {
                        loading(false);
                        showToast("Số điện thoại hoặc mật khẩu không đúng!");
                    }
                });
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            mBinding.btnLogin.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnLogin.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Animatoo.animateSlideRight(this);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        override.fontScale = 1.0f;
        applyOverrideConfiguration(override);
        super.attachBaseContext(newBase);
    }
}