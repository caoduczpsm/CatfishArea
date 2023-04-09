package com.example.catfisharea.activities.alluser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivitySendOtpactivityBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;

import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.concurrent.TimeUnit;

public class SendOTPActivity extends AppCompatActivity {
    private ActivitySendOtpactivityBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivitySendOtpactivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        Animatoo.animateSlideLeft(this);
        String mobile = getIntent().getStringExtra(Constants.KEY_PHONE);
        mBinding.inputMobile.setText(mobile);
        setListener();
    }

    private void setListener() {
        mBinding.buttonGetOTP.setOnClickListener(view -> {
            if (mBinding.inputMobile.getText().toString().trim().isEmpty()) {
                Toast.makeText(SendOTPActivity.this, getString(R.string.Phone), Toast.LENGTH_SHORT).show();
                return;
            }
            if (mBinding.inputMobile.getText().toString().trim().length() != 10) {
                Toast.makeText(SendOTPActivity.this, getString(R.string.Phone_9_numbers), Toast.LENGTH_SHORT).show();
                return;
            }
            String newUser = "0" + mBinding.inputMobile.getText().toString().trim();
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            database.collection(Constants.KEY_COLLECTION_USER)
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                if (newUser.equals(queryDocumentSnapshot.getString(Constants.KEY_PHONE))) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.User_already), Toast.LENGTH_SHORT).show();
                                    mBinding.progressBar.setVisibility(View.INVISIBLE);
                                    mBinding.buttonGetOTP.setVisibility(View.VISIBLE);
                                    return;
                                }
                            }
                            mBinding.progressBar.setVisibility(View.VISIBLE);
                            mBinding.buttonGetOTP.setVisibility(View.INVISIBLE);

                            PhoneAuthProvider.getInstance().verifyPhoneNumber(
                                    "+84" +
                                            mBinding.inputMobile.getText().toString().substring(1),
                                    60,
                                    TimeUnit.SECONDS,
                                    SendOTPActivity.this,
                                    new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                                        @Override
                                        public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                                            mBinding.progressBar.setVisibility(View.GONE);
                                            mBinding.buttonGetOTP.setVisibility(View.VISIBLE);

                                        }

                                        @Override
                                        public void onVerificationFailed(@NonNull FirebaseException e) {
                                            mBinding.progressBar.setVisibility(View.GONE);
                                            mBinding.buttonGetOTP.setVisibility(View.VISIBLE);
                                            mBinding.txtMessage.setText(e.getMessage());
                                            Toast.makeText(SendOTPActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onCodeSent(@NonNull String verificationId, @NonNull PhoneAuthProvider.ForceResendingToken forceResendingToken) {
                                            mBinding.progressBar.setVisibility(View.GONE);
                                            mBinding.buttonGetOTP.setVisibility(View.VISIBLE);
                                            Intent intent = new Intent(getApplicationContext(), VerifyOTPActivity.class);
                                            intent.putExtra(Constants.KEY_PHONE, mBinding.inputMobile.getText().toString());
                                            intent.putExtra(Constants.KEY_PASSWORD, getIntent().getStringExtra(Constants.KEY_PASSWORD));
                                            intent.putExtra("verificationId", verificationId);
                                            startActivity(intent);
                                            Animatoo.animateSlideLeft(SendOTPActivity.this);
                                        }
                                    }
                            );
                        }
                    });

        });
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