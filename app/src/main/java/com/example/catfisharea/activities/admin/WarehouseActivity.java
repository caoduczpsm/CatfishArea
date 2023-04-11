package com.example.catfisharea.activities.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWarehouseBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseAdapter;
import com.example.catfisharea.listeners.WarehouseListener;
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
    private List<Warehouse> mWarehouses;
    private boolean isFABOpen = false;


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
        warehouseAdapter = new WarehouseAdapter(mWarehouses, this);
        mBinding.recyclerViewWarehouse.setAdapter(warehouseAdapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mBinding.recyclerViewWarehouse.addItemDecoration(itemDecoration);
        mBinding.toolbarWarehouse.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.createWarehouse.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), WearhouseCreateActivity.class));
        });
        getDataWarehouse();

        mBinding.fabNew.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        mBinding.fabNewCategory.setOnClickListener(view -> {
            Intent intent = new Intent(this, CategoryCreateActivity.class);
            startActivity(intent);
        });
    }

    private void getDataWarehouse() {
        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        if (type.equals(Constants.KEY_DIRECTOR)) {
            getDataWarehouseForDirector();
        } else if (type.equals(Constants.KEY_REGIONAL_CHIEF)) {
            getDataWarehouseForRegional();
        }
    }

    private void getDataWarehouseForRegional() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String name = documentSnapshot.getString(Constants.KEY_NAME);
                        String id = documentSnapshot.getId();
                        String campusId = documentSnapshot.getString(Constants.KEY_CAMPUS_ID);
                        String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                        String acreage = documentSnapshot.getString(Constants.KEY_ACREAGE);
                        String description = documentSnapshot.getString(Constants.KEY_DESCRIPTION);

                        Warehouse warehouse = new Warehouse(id, name, areaId, campusId, acreage, description);
                        mWarehouses.add(warehouse);
                    }
                    warehouseAdapter.notifyDataSetChanged();
                });
    }

    private void getDataWarehouseForDirector() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        String name = documentSnapshot.getString(Constants.KEY_NAME);
                        String id = documentSnapshot.getId();
                        String campusId = documentSnapshot.getString(Constants.KEY_CAMPUS_ID);
                        String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                        String acreage = documentSnapshot.getString(Constants.KEY_ACREAGE);
                        String description = documentSnapshot.getString(Constants.KEY_DESCRIPTION);

                        Warehouse warehouse = new Warehouse(id, name, areaId, campusId, acreage, description);
                        mWarehouses.add(warehouse);
                    }
                    warehouseAdapter.notifyDataSetChanged();
                });
    }

    private void showFABMenu() {
        isFABOpen = true;
        mBinding.fabNewCategory.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mBinding.textLeave.setVisibility(View.VISIBLE);
        mBinding.textLeave.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        mBinding.fabNewCategory.animate().translationY(0);
        mBinding.textLeave.animate().translationY(0);
        mBinding.textLeave.setVisibility(View.GONE);
    }

    @Override
    public void openWarehouse(Warehouse warehouse) {
        Intent intent = new Intent(this, WarehouseDetailActivity.class);
        intent.putExtra(Constants.KEY_WAREHOUSE_ID, warehouse.getId());
        intent.putExtra(Constants.KEY_AREA_ID, warehouse.getAreaId());
        startActivity(intent);
    }
}