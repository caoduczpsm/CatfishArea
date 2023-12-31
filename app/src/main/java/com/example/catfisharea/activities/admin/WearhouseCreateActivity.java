package com.example.catfisharea.activities.admin;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWearhouseCreateBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.SpinnerAdapter;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class WearhouseCreateActivity extends BaseActivity {
    private ActivityWearhouseCreateBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Campus campus;
    private Pond pondSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWearhouseCreateBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        mBinding.toolbarWarehouseCreate.setNavigationOnClickListener(view -> onBackPressed());
        getDataSpinner();
        mBinding.edtNameWarehouse.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mBinding.textInputName.setError("Bắt buộc");
                    mBinding.textInputName.setErrorEnabled(true);
                } else {
                    mBinding.textInputName.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.edtAceage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    mBinding.textInputAceage.setError("Bắt buộc");
                    mBinding.textInputAceage.setErrorEnabled(true);
                } else {
                    mBinding.textInputAceage.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mBinding.createWarehouse.setOnClickListener(view -> createWarehouse());
    }

    private void getDataSpinner() {
        ArrayList<RegionModel> mCampus = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, R.layout.layout_spinner_item, mCampus);
        mBinding.spinnerCampus.setAdapter(spinnerAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)
                && preferenceManager.getString(Constants.KEY_AREA_ID) != null) {
            database.collection(Constants.KEY_COLLECTION_CAMPUS)
                    .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String id = doc.getId();
                            String name = doc.getString(Constants.KEY_NAME);
                            ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                            Campus campus = new Campus(id, name, geoList, preferenceManager.getString(Constants.KEY_AREA_ID));
                            mCampus.add(campus);
                        }
                        spinnerAdapter.notifyDataSetChanged();
                    });
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_ADMIN)) {
            database.collection(Constants.KEY_COLLECTION_CAMPUS)
                    .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                    .get().addOnSuccessListener(queryDocumentSnapshots -> {
                        for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                            String id = doc.getId();
                            String name = doc.getString(Constants.KEY_NAME);
                            String areaId = doc.getString(Constants.KEY_AREA_ID);
                            ArrayList<GeoPoint> geoList = (ArrayList<GeoPoint>) doc.get(Constants.KEY_MAP);
                            Campus campus = new Campus(id, name, geoList, areaId);
                            mCampus.add(campus);
                        }
                        spinnerAdapter.notifyDataSetChanged();
                    });
        } else {
            mBinding.textInputCampus.setVisibility(View.GONE);
            getDataPond(preferenceManager.getString(Constants.KEY_CAMPUS_ID));
        }

        mBinding.spinnerCampus.setOnItemClickListener((parent, view, position, id) -> {
            campus = (Campus) parent.getItemAtPosition(position);
            getDataPond(campus.getId());
        });
    }

    public void getDataPond(String campusId) {
        ArrayList<RegionModel> mPond = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, R.layout.layout_spinner_item, mPond);
        mBinding.spinnerPond.setAdapter(spinnerAdapter);

        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                .get().addOnSuccessListener(pondQuery -> {
                    for (DocumentSnapshot doc: pondQuery.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        String id = doc.getId();
                        assert data != null;
                        Pond pond = new Pond(id, Objects.requireNonNull(data.get(Constants.KEY_NAME)).toString());
                        mPond.add(pond);
                    }
                    spinnerAdapter.notifyDataSetChanged();
                });

        mBinding.spinnerPond.setOnItemClickListener((parent, view, position, id) ->
                pondSelected = (Pond) parent.getItemAtPosition(position));
    }

    private void createWarehouse() {
        String name = Objects.requireNonNull(mBinding.edtNameWarehouse.getText()).toString();
        String acreage = Objects.requireNonNull(mBinding.edtAceage.getText()).toString();
        String description = Objects.requireNonNull(mBinding.edtDescription.getText()).toString();

        if (!name.isEmpty() && !acreage.isEmpty()) {
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_ADMIN)
                    || preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
                if (campus != null && pondSelected != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(Constants.KEY_NAME, name);
                    data.put(Constants.KEY_ACREAGE, acreage);
                    data.put(Constants.KEY_DESCRIPTION, description);
                    data.put(Constants.KEY_CAMPUS_ID, campus.getId());
                    data.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                    data.put(Constants.KEY_POND_ID, pondSelected.getId());
                    database.collection(Constants.KEY_CAMPUS).document(campus.getId()).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                                data.put(Constants.KEY_AREA_ID, areaId);
                                database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                        .document()
                                        .set(data)
                                        .addOnSuccessListener(task -> onBackPressed());
                            });
                }
            } else {
                if (preferenceManager.getString(Constants.KEY_CAMPUS_ID) != null && pondSelected != null) {
                    Map<String, Object> data = new HashMap<>();
                    data.put(Constants.KEY_NAME, name);
                    data.put(Constants.KEY_ACREAGE, acreage);
                    data.put(Constants.KEY_DESCRIPTION, description);
                    data.put(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID));
                    data.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                    data.put(Constants.KEY_POND_ID, pondSelected.getId());
                    database.collection(Constants.KEY_CAMPUS).document(preferenceManager.getString(Constants.KEY_CAMPUS_ID)).get()
                            .addOnSuccessListener(documentSnapshot -> {
                                String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                                data.put(Constants.KEY_AREA_ID, areaId);
                                database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                        .document().set(data).addOnSuccessListener(task -> onBackPressed());
                            });
                }

            }

        }
    }

}