package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.R;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.databinding.ActivityRegisterBinding;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

public class RegisterActivity extends BaseActivity {
    private ActivityRegisterBinding mBinding;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        initActivity();
        setListener();
    }

    private void initActivity() {
        preferenceManager = new PreferenceManager(this);
    }

    private void setListener() {
        mBinding.textLogin.setOnClickListener(view -> {
            onBackPressed();
        });

        mBinding.btnRegis.setOnClickListener(view -> {
            if (isValidSignUpDetails())
                signUp();
        });

        mBinding.radioPersonal.setOnClickListener(view -> {

            // Check vào cá nhân và ẩn công ty
            mBinding.radioPersonal.setChecked(true);
            mBinding.radioCompany.setChecked(false);

            // Đổi trạng thái loại đăng ký thành đăng ký cá nhân
            preferenceManager.putString(Constants.KEY_TYPE_REGIS, Constants.KEY_PERSONAL_REGIS);

        });

        mBinding.radioCompany.setOnClickListener(view -> {

            // Check vào cá nhân và ẩn công ty
            mBinding.radioCompany.setChecked(true);
            mBinding.radioPersonal.setChecked(false);

            // Đổi trạng thái loại đăng ký thành đăng ký công ty
            preferenceManager.putString(Constants.KEY_TYPE_REGIS, Constants.KEY_COMPANY_REGIS);

        });
    }

    private void signUp(){
//        Intent intent = new Intent(getApplicationContext(), SendOTPActivity.class);
//        intent.putExtra(Constants.KEY_PHONE, mBinding.edtPhoneRegister.getText().toString());
//        intent.putExtra(Constants.KEY_PASSWORD, mBinding.edtPasswordRegister.getText().toString().trim());
//        Animatoo.animateSlideLeft(RegisterActivity.this);
//        startActivity(intent);
    }

    private Boolean isValidSignUpDetails(){
        if(mBinding.edtPhoneRegister.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập số điện thoại!");
            return false;
        } /*else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.getText().toString()).matches()){
            showToast("Enter valid image");
            return false;
        }*/ else if(mBinding.edtPasswordRegister.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập mật khẩu!");
            return false;
        } else if(mBinding.edtRePasswordRegister.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập lại mật khẩu!");
            return false;
        } else if(mBinding.edtPhoneRegister.getText().toString().length() != 10){
            showToast("Sai định dạng số điện thoại!");
            return false;
        } else if(!mBinding.edtPasswordRegister.getText().toString().equals(mBinding.edtRePasswordRegister.getText().toString().trim())){
            showToast("Mật khẩu và nhập lại mật khẩu khác nhau!");
            return false;
        } else{
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}