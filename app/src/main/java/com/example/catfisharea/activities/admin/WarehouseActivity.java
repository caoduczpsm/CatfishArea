package com.example.catfisharea.activities.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWarehouseBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseAdapter;
import com.example.catfisharea.models.Warehouse;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class WarehouseActivity extends BaseActivity {
    private ActivityWarehouseBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private WarehouseAdapter warehouseAdapter;
    private List<Warehouse> mWarehouses;

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
        warehouseAdapter = new WarehouseAdapter(mWarehouses);
        mBinding.recyclerViewWarehouse.setAdapter(warehouseAdapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mBinding.recyclerViewWarehouse.addItemDecoration(itemDecoration);
        mBinding.toolbarWarehouse.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.createWarehouse.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), WearhouseCreateActivity.class));
        });
        getDataWarehouse();
    }

    private void getDataWarehouse() {
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


}