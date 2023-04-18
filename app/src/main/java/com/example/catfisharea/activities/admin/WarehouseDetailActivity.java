package com.example.catfisharea.activities.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.ActivityWarehouseDetailBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseDetailAdapter;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WarehouseDetailActivity extends BaseActivity {
    private ActivityWarehouseDetailBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String warehouseID;
    private String areaID;
    private List<Category> mCategories;
    private WarehouseDetailAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWarehouseDetailBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        warehouseID = getIntent().getStringExtra(Constants.KEY_WAREHOUSE_ID);
        areaID = getIntent().getStringExtra(Constants.KEY_AREA_ID);
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        mCategories = new ArrayList<>();
        adapter = new WarehouseDetailAdapter(mCategories);
        mBinding.recyclerViewWarehouseDetail.setAdapter(adapter);
        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mBinding.recyclerViewWarehouseDetail.addItemDecoration(itemDecoration);

        mBinding.toolbarWarehouseDetail.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.createWarehouse.setOnClickListener(view -> {
            Intent intent = new Intent(this, ImportWarehouseActivity.class);
            intent.putExtra(Constants.KEY_WAREHOUSE_ID, warehouseID);
            intent.putExtra(Constants.KEY_AREA_ID, areaID);
            startActivity(intent);
        });
        getData();


    }

    @SuppressLint("NewApi")
    private void getData() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .document(warehouseID).get().addOnSuccessListener(warehouseDoc -> {
                    String name = warehouseDoc.getString(Constants.KEY_NAME);
                    mBinding.toolbarWarehouseDetail.setTitle(name);
                });
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE).document(warehouseID)
                .collection(Constants.KEY_CATEGORY_OF_WAREHOUSE)
                .get().addOnSuccessListener(warehouseQuery -> {
                    for (DocumentSnapshot doc: warehouseQuery) {
                        String name = doc.getString(Constants.KEY_NAME);
                        String producer = doc.getString(Constants.KEY_PRODUCER);
                        String amount = doc.getString(Constants.KEY_AMOUNT);
                        String unit = doc.getString(Constants.KEY_UNIT);
                        Category category = new Category(doc.getId(), name);
                        category.setProducer(producer);
                        category.setAmount(Integer.parseInt(amount));
                        category.setUnit(unit);
                        mCategories.add(category);
                    }
                    adapter.notifyDataSetChanged();
                });
    }


}