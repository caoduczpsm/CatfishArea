package com.example.catfisharea.activities.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import com.android.app.catfisharea.databinding.ActivityWarehouseBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseAdapter;
import com.example.catfisharea.listeners.WarehouseListener;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemWarehouse;
import com.example.catfisharea.models.Warehouse;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WarehouseActivity extends BaseActivity implements WarehouseListener {
    private ActivityWarehouseBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private WarehouseAdapter warehouseAdapter;
    private List<ItemWarehouse> mWarehouses;

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
        mBinding.layoutCreateWarehouse.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), WearhouseCreateActivity.class)));
        mBinding.layoutCategory.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), CategoryCreateActivity.class)));
        getDataWarehouse();

    }

    private void getDataWarehouse() {
        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        switch (type) {
            case Constants.KEY_DIRECTOR:
                getDataWarehouseForDirector();
                break;
            case Constants.KEY_REGIONAL_CHIEF:
            case Constants.KEY_ACCOUNTANT:
                getDataWarehouseForRegional();
                break;
            case Constants.KEY_ADMIN:
                getDataWarehouseForAdmin();
                break;
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataWarehouseForRegional() {
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

                                        warehouseAdapter.notifyDataSetChanged();
                                    }
                                });
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataWarehouseForDirector() {
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
                        warehouseAdapter.notifyDataSetChanged();
//                        mWarehouses.add(warehouse);

                    }
//                    warehouseAdapter.notifyDataSetChanged();
                });
    }

    private void getDataWarehouseForAdmin() {
//        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
//                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
//                .get().addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
//                        String name = documentSnapshot.getString(Constants.KEY_NAME);
//                        String id = documentSnapshot.getId();
//                        String campusId = documentSnapshot.getString(Constants.KEY_CAMPUS_ID);
//                        String pondId = documentSnapshot.getString(Constants.KEY_POND_ID);
//                        String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
//                        String acreage = documentSnapshot.getString(Constants.KEY_ACREAGE);
//                        String description = documentSnapshot.getString(Constants.KEY_DESCRIPTION);
//
//                        Warehouse warehouse = new Warehouse(id, name, areaId, campusId, acreage, description);
//                        warehouse.setPondId(pondId);
//                        mWarehouses.add(warehouse);
//                        database.collection(Constants.KEY_COLLECTION_POND).document(pondId)
//                                .get().addOnSuccessListener(documentSnapshot1 -> {
//                                    warehouse.setPondName(documentSnapshot1.getString(Constants.KEY_NAME));
//                                    database.collection(Constants.KEY_COLLECTION_CAMPUS).document(campusId)
//                                                    .get().addOnSuccessListener(campus -> {
//                                                warehouse.setPondName(campus.getString(Constants.KEY_NAME) + warehouse.getPondName());
//                                                warehouseAdapter.notifyDataSetChanged();
//                                            });
//
//                                    warehouseAdapter.notifyDataSetChanged();
//                                });
//
////                        mWarehouses.add(warehouse);
//                    }
////                    warehouseAdapter.notifyDataSetChanged();
//                });
    }

    @Override
    public void openWarehouse(Warehouse warehouse) {
        Intent intent = new Intent(this, WarehouseDetailActivity.class);
        intent.putExtra(Constants.KEY_WAREHOUSE_ID, warehouse.getId());
        intent.putExtra(Constants.KEY_AREA_ID, warehouse.getAreaId());
        startActivity(intent);
    }
}