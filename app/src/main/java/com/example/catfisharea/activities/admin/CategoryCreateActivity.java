package com.example.catfisharea.activities.admin;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityCategoryCreateBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CategoryCreateActivity extends BaseActivity {
    private ActivityCategoryCreateBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCategoryCreateBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        setListener();
    }

    private void setListener() {
        checkError();
        mBinding.saveBtn.setOnClickListener(view -> {
            saveCategory();
        });
    }

    private void saveCategory() {
        String name = mBinding.edtNameCategory.getText().toString().trim();
        String producer = mBinding.edtNameSupplier.getText().toString().trim();
        String unit = mBinding.edtUnit.getText().toString().trim();
        String effect = mBinding.edtEffect.getText().toString().trim();
        String companyID = preferenceManager.getString(Constants.KEY_COMPANY_ID);
        String areaID = preferenceManager.getString(Constants.KEY_AREA_ID);

        assert areaID != null;

        if (!name.isEmpty() && !producer.isEmpty() && !unit.isEmpty() && effect.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.KEY_NAME, name);
            data.put(Constants.KEY_PRODUCER, producer);
            data.put(Constants.KEY_UNIT, unit);
            data.put(Constants.KEY_EFFECT, effect);
            data.put(Constants.KEY_COMPANY_ID, companyID);
            data.put(Constants.KEY_AREA_ID, areaID);
            database.collection(Constants.KEY_COLLECTION_CATEGORY).document().set(data);
        }

    }

    private void checkError() {
        mBinding.edtNameCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mBinding.textInputNameCategory.setError("Bắt buộc");
                    mBinding.textInputNameCategory.setErrorEnabled(true);
                } else {
                    mBinding.textInputNameCategory.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.edtNameSupplier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mBinding.textInputNameSupplier.setError("Bắt buộc");
                    mBinding.textInputNameSupplier.setErrorEnabled(true);
                } else {
                    mBinding.textInputNameSupplier.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.edtUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mBinding.textInputUnit.setError("Bắt buộc");
                    mBinding.textInputUnit.setErrorEnabled(true);
                } else {
                    mBinding.textInputUnit.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.edtEffect.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mBinding.textInputEffect.setError("Bắt buộc");
                    mBinding.textInputEffect.setErrorEnabled(true);
                } else {
                    mBinding.textInputEffect.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }

}