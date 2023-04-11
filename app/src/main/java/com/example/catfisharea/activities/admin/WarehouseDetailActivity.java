package com.example.catfisharea.activities.admin;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.ActivityWarehouseDetailBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseDetailAdapter;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
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
        getData();
    }

    @SuppressLint("NewApi")
    private void getData() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .document(warehouseID).get().addOnSuccessListener(warehouseDoc -> {
                    Map<String, Integer> categoryMap = (Map<String, Integer>) warehouseDoc.get(Constants.KEY_CATEGORY_OF_WAREHOUSE);
                    categoryMap.forEach((key, value) -> {
                        database.collection(Constants.KEY_CATEGORY_OF_WAREHOUSE).document(key)
                                .get().addOnSuccessListener(categoryDoc -> {
                                    String name = categoryDoc.getString(Constants.KEY_NAME);
                                    String producer = categoryDoc.getString(Constants.KEY_PRODUCER);
                                    String unit = categoryDoc.getString(Constants.KEY_UNIT);
                                    Category category = new Category(categoryDoc.getId(), name);
                                    category.setAmount(value.intValue());
                                    category.setUnit(unit);
                                    category.setProducer(producer);
                                    mCategories.add(category);
                                    adapter.notifyDataSetChanged();
                                });
                    });
                });
    }


}