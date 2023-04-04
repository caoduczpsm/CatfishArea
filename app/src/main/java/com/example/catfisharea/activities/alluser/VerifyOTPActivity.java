package com.example.catfisharea.activities.alluser;

import androidx.annotation.NonNull;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Toast;


import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityVerifyOtpactivityBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;

import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class VerifyOTPActivity extends BaseActivity {
    private ActivityVerifyOtpactivityBinding mBinding;
    private String verificationId;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityVerifyOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        mBinding.textMobile.setText(getIntent().getStringExtra(Constants.KEY_PHONE));
        password = getIntent().getStringExtra(Constants.KEY_PASSWORD);

        setupOTPInputs();

        verificationId = getIntent().getStringExtra("verificationId");
        setListener();
    }

    private void setListener() {
        mBinding.buttonVerify.setOnClickListener(view -> {
            if (mBinding.inputCode1.getText().toString().trim().isEmpty()
                    || mBinding.inputCode2.getText().toString().trim().isEmpty()
                    || mBinding.inputCode3.getText().toString().trim().isEmpty()
                    || mBinding.inputCode4.getText().toString().trim().isEmpty()
                    || mBinding.inputCode5.getText().toString().trim().isEmpty()
                    || mBinding.inputCode6.getText().toString().trim().isEmpty()){
                Toast.makeText(VerifyOTPActivity.this, getString(R.string.enter_valid_code), Toast.LENGTH_SHORT).show();
                return;
            }

            String code = mBinding.inputCode1.getText().toString() +
                    mBinding.inputCode2.getText().toString() +
                    mBinding.inputCode3.getText().toString() +
                    mBinding.inputCode4.getText().toString() +
                    mBinding.inputCode5.getText().toString() +
                    mBinding.inputCode6.getText().toString();

            if (verificationId != null){
                mBinding.progressBar.setVisibility(View.VISIBLE);
                mBinding.buttonVerify.setVisibility(View.INVISIBLE);
                PhoneAuthCredential phoneAuthCredential = PhoneAuthProvider.getCredential(
                        verificationId, code
                );
                FirebaseAuth.getInstance().signInWithCredential(phoneAuthCredential)
                        .addOnCompleteListener(task -> {
                            if (task.isSuccessful()){
                                Intent intent = new Intent(getApplicationContext(), SetInfoRegisAdminActivity.class);
                                intent.putExtra(Constants.KEY_PHONE, getIntent().getStringExtra(Constants.KEY_PHONE));
                                intent.putExtra(Constants.KEY_PASSWORD, password);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                Animatoo.animateSlideLeft(VerifyOTPActivity.this);
                                startActivity(intent);
                            } else{
                                Toast.makeText(VerifyOTPActivity.this, "Mã OTP không chính xác!", Toast.LENGTH_SHORT).show();
                            }

                        });
            }

        });

        mBinding.textResendOTP.setOnClickListener(view -> PhoneAuthProvider.getInstance().verifyPhoneNumber(
                "+84"
                        + getIntent().getStringExtra("mobile"), 60,
                TimeUnit.SECONDS,
                VerifyOTPActivity.this,
                new PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
                    @Override
                    public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {

                    }

                    @Override
                    public void onVerificationFailed(@NonNull FirebaseException e) {

                        Toast.makeText(VerifyOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCodeSent(@NonNull String newVerificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                        verificationId = newVerificationId;
                        Toast.makeText(VerifyOTPActivity.this, "OTP sent", Toast.LENGTH_SHORT).show();
                    }
                }
        ));
    }

    private void setupOTPInputs(){
        mBinding.inputCode1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty()){
                    mBinding.inputCode2.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBinding.inputCode2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty()){
                    mBinding.inputCode3.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBinding.inputCode3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty()){
                    mBinding.inputCode4.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBinding.inputCode4.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty()){
                    mBinding.inputCode5.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        mBinding.inputCode5.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {}

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {
                if (!s.toString().trim().isEmpty()){
                    mBinding.inputCode6.requestFocus();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }
}