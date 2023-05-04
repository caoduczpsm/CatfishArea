package com.example.catfisharea.activities.admin;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWarehouseBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.SpinnerAdapter;
import com.example.catfisharea.adapter.WarehouseAdapter;
import com.example.catfisharea.listeners.WarehouseListener;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemWarehouse;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.models.Warehouse;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WarehouseActivity extends BaseActivity implements WarehouseListener {
    private ActivityWarehouseBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private WarehouseAdapter warehouseAdapter;
    private List<ItemWarehouse> mWarehouses;
    private AutoCompleteTextView spinnerPond, spinnerType;
    private TextInputEditText edtNameWarehouse, edtAceage, edtDescription, edtNameCategory, edtNameSupplier, edtUnit, edtEffect;
    private TextInputLayout textInputNameCategory, textInputNameSupplier, textInputUnit, textInputEffect;
    private Campus campus;
    private Pond pondSelected;
    private String typeCategory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWarehouseBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setUpActivity();
    }

    private void setUpActivity() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        mWarehouses = new ArrayList<>();
        warehouseAdapter = new WarehouseAdapter(mWarehouses, this, this);
        mBinding.recyclerViewWarehouse.setAdapter(warehouseAdapter);

        mBinding.toolbarWarehouse.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.layoutCreateWarehouse.setOnClickListener(view -> openCreateWarehouseDialog());
        mBinding.layoutCategory.setOnClickListener(view -> openCreateCategoryDialog());
        getDataWarehouse();

    }

    private void openCreateWarehouseDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_create_warehouse);
        assert dialog != null;

        TextInputLayout textInputCampus;
        AutoCompleteTextView spinnerCampus;
        AppCompatButton btnClose, btnCreate;
        ImageView imageCampus;

        edtNameWarehouse = dialog.findViewById(R.id.edtNameWarehouse);
        edtAceage = dialog.findViewById(R.id.edtAceage);
        edtDescription = dialog.findViewById(R.id.edtDescription);
        spinnerCampus = dialog.findViewById(R.id.spinnerCampus);
        spinnerPond = dialog.findViewById(R.id.spinnerPond);

        imageCampus = dialog.findViewById(R.id.imageViewCampus);

        textInputCampus = dialog.findViewById(R.id.textInputCampus);

        btnClose = dialog.findViewById(R.id.btnClose);
        btnCreate = dialog.findViewById(R.id.btnCreate);

        ArrayList<RegionModel> mCampus = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, R.layout.layout_spinner_item, mCampus);
        spinnerCampus.setAdapter(spinnerAdapter);

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
            textInputCampus.setVisibility(View.GONE);
            imageCampus.setVisibility(View.GONE);
            getDataPond(preferenceManager.getString(Constants.KEY_CAMPUS_ID));
        }

        spinnerCampus.setOnItemClickListener((parent, view, position, id) -> {
            campus = (Campus) parent.getItemAtPosition(position);
            getDataPond(campus.getId());
        });

        btnCreate.setOnClickListener(view -> {
            createWarehouse(dialog);
//            dialog.dismiss();
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    public void getDataPond(String campusId) {
        ArrayList<RegionModel> mPond = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, R.layout.layout_spinner_item, mPond);
        spinnerPond.setAdapter(spinnerAdapter);

        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                .get().addOnSuccessListener(pondQuery -> {
                    for (DocumentSnapshot doc : pondQuery.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        String id = doc.getId();
                        assert data != null;
                        Pond pond = new Pond(id, Objects.requireNonNull(data.get(Constants.KEY_NAME)).toString());
                        mPond.add(pond);
                    }
                    spinnerAdapter.notifyDataSetChanged();
                });

        spinnerPond.setOnItemClickListener((parent, view, position, id) ->
                pondSelected = (Pond) parent.getItemAtPosition(position));
    }

    private void createWarehouse(Dialog dialog) {
        String name = Objects.requireNonNull(edtNameWarehouse.getText()).toString();
        String acreage = Objects.requireNonNull(edtAceage.getText()).toString();
        String description = Objects.requireNonNull(edtDescription.getText()).toString();

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
                                        .addOnSuccessListener(task -> {
                                            getDataWarehouse();
                                        });
                            });
                    dialog.dismiss();
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
                                        .document().set(data).addOnSuccessListener(task -> {
                                            getDataWarehouse();
                                        });
                            });
                    dialog.dismiss();
                }

            }

        } else {
            Toast.makeText(this, "Nhập thông tin", Toast.LENGTH_SHORT).show();
        }
    }

    private void openCreateCategoryDialog() {
        Dialog dialog = openDialog(R.layout.layout_dialog_create_category);
        assert dialog != null;

        AppCompatButton btnClose, btnCreate;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnCreate = dialog.findViewById(R.id.btnCreate);

        edtNameCategory = dialog.findViewById(R.id.edtNameCategory);
        edtNameSupplier = dialog.findViewById(R.id.edtNameSupplier);
        edtUnit = dialog.findViewById(R.id.edtUnit);
        edtEffect = dialog.findViewById(R.id.edtEffect);

        textInputNameCategory = dialog.findViewById(R.id.textInputNameCategory);
        textInputNameSupplier = dialog.findViewById(R.id.textInputNameSupplier);
        textInputUnit = dialog.findViewById(R.id.textInputUnit);
        textInputEffect = dialog.findViewById(R.id.textInputEffect);
        spinnerType = dialog.findViewById(R.id.spinnerTypeCategory);

        List<String> listTypeCategory = new ArrayList<>();
        listTypeCategory.add("Thuốc");
        listTypeCategory.add("Thức ăn");
        listTypeCategory.add("Khác");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, listTypeCategory);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerType.setAdapter(adapter);

        spinnerType.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    typeCategory = Constants.KEY_CATEGORY_TYPE_MEDICINE;
                } else if (position == 1) {
                    typeCategory = Constants.KEY_CATEGORY_TYPE_FOOD;
                } else {
                    typeCategory = "other";
                }

            }
        });

        checkCategoryError();

        btnCreate.setOnClickListener(view -> {
            saveCategory(dialog);
//            dialog.dismiss();
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private void saveCategory(Dialog dialog) {
        String name = Objects.requireNonNull(edtNameCategory.getText()).toString().trim();
        String producer = Objects.requireNonNull(edtNameSupplier.getText()).toString().trim();
        String unit = Objects.requireNonNull(edtUnit.getText()).toString().trim();
        String effect = Objects.requireNonNull(edtEffect.getText()).toString().trim();
        String companyID = preferenceManager.getString(Constants.KEY_COMPANY_ID);
        String areaID = preferenceManager.getString(Constants.KEY_AREA_ID);

        assert areaID != null;

        if (!name.isEmpty() && !producer.isEmpty() && !unit.isEmpty() && !effect.isEmpty() && typeCategory != null) {
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.KEY_NAME, name);
            data.put(Constants.KEY_PRODUCER, producer);
            data.put(Constants.KEY_UNIT, unit);
            data.put(Constants.KEY_EFFECT, effect);
            data.put(Constants.KEY_COMPANY_ID, companyID);
            data.put(Constants.KEY_AREA_ID, areaID);
            data.put(Constants.KEY_CATEGORY_TYPE, typeCategory);
            database.collection(Constants.KEY_COLLECTION_CATEGORY).document().set(data).addOnSuccessListener(command -> {
                edtNameCategory.setText("");
                edtUnit.setText("");
                edtNameSupplier.setText("");
                edtEffect.setText("");
                textInputNameCategory.setErrorEnabled(false);
                textInputEffect.setErrorEnabled(false);
                textInputUnit.setErrorEnabled(false);
                textInputNameSupplier.setErrorEnabled(false);
                Toast.makeText(this, "Tạo thành công", Toast.LENGTH_SHORT).show();
            });
            dialog.dismiss();
        } else {
            Toast.makeText(this, "Nhập thông tin", Toast.LENGTH_SHORT).show();
        }

    }

    private void checkCategoryError() {
        edtNameCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    textInputNameCategory.setError("Bắt buộc");
                    textInputNameCategory.setErrorEnabled(true);
                } else {
                    textInputNameCategory.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtNameSupplier.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    textInputNameSupplier.setError("Bắt buộc");
                    textInputNameSupplier.setErrorEnabled(true);
                } else {
                    textInputNameSupplier.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtUnit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    textInputUnit.setError("Bắt buộc");
                    textInputUnit.setErrorEnabled(true);
                } else {
                    textInputUnit.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        edtEffect.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() <= 0) {
                    textInputEffect.setError("Bắt buộc");
                    textInputEffect.setErrorEnabled(true);
                } else {
                    textInputEffect.setErrorEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

    }


    private void getDataWarehouse() {
        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        switch (type) {
            case Constants.KEY_DIRECTOR:
                mBinding.layoutCreateWarehouse.setVisibility(View.VISIBLE);
                mBinding.layoutCategory.setVisibility(View.GONE);
                getDataWarehouseForDirector();
                break;
            case Constants.KEY_REGIONAL_CHIEF:
                mBinding.layoutCreateWarehouse.setVisibility(View.VISIBLE);
                mBinding.layoutCategory.setVisibility(View.VISIBLE);
                getDataWarehouseForRegional();
                break;
            case Constants.KEY_ACCOUNTANT:
                mBinding.layoutCreateWarehouse.setVisibility(View.GONE);
                mBinding.layoutCategory.setVisibility(View.GONE);
                getDataWarehouseForRegional();
                break;
            case Constants.KEY_ADMIN:
                mBinding.layoutCreateWarehouse.setVisibility(View.GONE);
                mBinding.layoutCategory.setVisibility(View.GONE);
                getDataWarehouseForAdmin();
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataWarehouseForRegional() {
        mWarehouses.clear();
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get().addOnSuccessListener(campusQuery -> {
                    for (DocumentSnapshot campusDoc : campusQuery.getDocuments()) {
                        ItemWarehouse itemWarehouse = new ItemWarehouse();
                        List<Warehouse> warehouseList = new ArrayList<>();
                        mWarehouses.add(itemWarehouse);
                        Campus campus = new Campus(campusDoc.getId(), campusDoc.getString(Constants.KEY_NAME));
                        itemWarehouse.setRegionModel(campus);

                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusDoc.getId())
                                .get().addOnSuccessListener(warehouseQuery -> {

                                    for (DocumentSnapshot warehouseDoc : warehouseQuery.getDocuments()) {
                                        String name = warehouseDoc.getString(Constants.KEY_NAME);
                                        String id = warehouseDoc.getId();
                                        String campusId = warehouseDoc.getString(Constants.KEY_CAMPUS_ID);
                                        String pondId = warehouseDoc.getString(Constants.KEY_POND_ID);
                                        String areaId = warehouseDoc.getString(Constants.KEY_AREA_ID);
                                        String acreage = warehouseDoc.getString(Constants.KEY_ACREAGE);
                                        String description = warehouseDoc.getString(Constants.KEY_DESCRIPTION);

                                        Warehouse warehouse = new Warehouse(id, name, areaId, campusId, acreage, description);
                                        warehouse.setPondId(pondId);

                                        warehouseList.add(warehouse);
                                        itemWarehouse.setWarehouseList(warehouseList);
                                        Collections.sort(mWarehouses,
                                                (o1, o2) -> (o1.getRegionModel().getName().compareToIgnoreCase(o2.getRegionModel().getName())));

                                        Collections.sort(itemWarehouse.getWarehouseList(),
                                                (o1, o2) -> (o1.getName().compareToIgnoreCase(o2.getName())));
                                        warehouseAdapter.notifyDataSetChanged();
                                    }

                                });
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataWarehouseForDirector() {
        mWarehouses.clear();
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ItemWarehouse itemWarehouse = new ItemWarehouse();
                    List<Warehouse> warehouseList = new ArrayList<>();
                    database.collection(Constants.KEY_COLLECTION_CAMPUS)
                            .document(preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                            .get().addOnSuccessListener(documentSnapshot -> {
                                Campus campus = new Campus(documentSnapshot.getId(),
                                        documentSnapshot.getString(Constants.KEY_NAME));
                                itemWarehouse.setRegionModel(campus);
                                mWarehouses.add(itemWarehouse);
                                Collections.sort(mWarehouses,
                                        (o1, o2) -> (o1.getRegionModel().getName().compareToIgnoreCase(o2.getRegionModel().getName())));
                                warehouseAdapter.notifyDataSetChanged();
                            });
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String name = documentSnapshot.getString(Constants.KEY_NAME);
                        String id = documentSnapshot.getId();
                        String campusId = documentSnapshot.getString(Constants.KEY_CAMPUS_ID);
                        String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                        String acreage = documentSnapshot.getString(Constants.KEY_ACREAGE);
                        String description = documentSnapshot.getString(Constants.KEY_DESCRIPTION);
                        String pondId = documentSnapshot.getString(Constants.KEY_POND_ID);
                        Warehouse warehouse = new Warehouse(id, name, areaId, campusId, acreage, description);
                        warehouse.setPondId(pondId);
                        warehouseList.add(warehouse);

                        itemWarehouse.setWarehouseList(warehouseList);
                        Collections.sort(itemWarehouse.getWarehouseList(),
                                (o1, o2) -> (o1.getName().compareToIgnoreCase(o2.getName())));
                        warehouseAdapter.notifyDataSetChanged();
//                        mWarehouses.add(warehouse);

                    }

//                    warehouseAdapter.notifyDataSetChanged();
//                    warehouseAdapter.notifyDataSetChanged();
                });
    }

    private void getDataWarehouseForAdmin() {
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get().addOnSuccessListener(campusQuery -> {
                    for (DocumentSnapshot campusDoc : campusQuery.getDocuments()) {
                        ItemWarehouse itemWarehouse = new ItemWarehouse();
                        List<Warehouse> warehouseList = new ArrayList<>();
                        mWarehouses.add(itemWarehouse);
                        Campus campus = new Campus(campusDoc.getId(), campusDoc.getString(Constants.KEY_NAME));
                        itemWarehouse.setRegionModel(campus);

                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusDoc.getId())
                                .get().addOnSuccessListener(warehouseQuery -> {
                                    for (DocumentSnapshot warehouseDoc : warehouseQuery.getDocuments()) {
                                        String name = warehouseDoc.getString(Constants.KEY_NAME);
                                        String id = warehouseDoc.getId();
                                        String campusId = warehouseDoc.getString(Constants.KEY_CAMPUS_ID);
                                        String pondId = warehouseDoc.getString(Constants.KEY_POND_ID);
                                        String areaId = warehouseDoc.getString(Constants.KEY_AREA_ID);
                                        String acreage = warehouseDoc.getString(Constants.KEY_ACREAGE);
                                        String description = warehouseDoc.getString(Constants.KEY_DESCRIPTION);

                                        Warehouse warehouse = new Warehouse(id, name, areaId, campusId, acreage, description);
                                        warehouse.setPondId(pondId);

                                        warehouseList.add(warehouse);
                                        itemWarehouse.setWarehouseList(warehouseList);

                                        warehouseAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(this);
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

    @Override
    public void openWarehouse(Warehouse warehouse) {
        Intent intent = new Intent(this, WarehouseDetailActivity.class);
        intent.putExtra(Constants.KEY_WAREHOUSE_ID, warehouse.getId());
        intent.putExtra(Constants.KEY_AREA_ID, warehouse.getAreaId());
        startActivity(intent);
    }
}