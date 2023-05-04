package com.example.catfisharea.activities.admin;


import android.annotation.SuppressLint;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityWarehouseHistoryBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseHistoryAdapter;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.models.WarehouseHistory;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class WarehouseHistoryActivity extends BaseActivity {
    private ActivityWarehouseHistoryBinding mBinding;
    private FirebaseFirestore database;
    private List<WarehouseHistory> warehouseHistoryList;
    private WarehouseHistoryAdapter adapter;
    private String warehouseId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWarehouseHistoryBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        warehouseId = getIntent().getStringExtra(Constants.KEY_WAREHOUSE_ID);
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        warehouseHistoryList = new ArrayList<>();
        mBinding.imageBack.setOnClickListener(view -> onBackPressed());
        adapter = new WarehouseHistoryAdapter(warehouseHistoryList);
        mBinding.recyclervewHistory.setAdapter(adapter);
        getNameWarehouse();
        getData();
    }

    private void getNameWarehouse() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .document(warehouseId).get().addOnSuccessListener(nameDoc -> {
                    String name = nameDoc.getString(Constants.KEY_NAME);
                    mBinding.textName.setText("Lịch sử nhập " + name);
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getData() {
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE_HISTORY)
                .whereEqualTo(Constants.KEY_WAREHOUSE_ID, warehouseId)
                .get().addOnSuccessListener(warehouseQuery -> {
                    for (DocumentSnapshot doc : warehouseQuery.getDocuments()) {
                        String id = doc.getId();
                        String total = doc.getString(Constants.KEY_TOTAL_MONEY);
                        Date time = Objects.requireNonNull(doc.getTimestamp(Constants.KEY_TIMESTAMP)).toDate();
                        List<Map<String, Object>> product = (List<Map<String, Object>>) doc.get(Constants.KEY_MATERIALSLIST);
                        WarehouseHistory warehouseHistory = new WarehouseHistory(id, warehouseId);
                        warehouseHistory.setTotal(total);
                        warehouseHistory.setDate(time);
                        List<Category> categories = new ArrayList<>();
                        assert product != null;
                        for (Map<String, Object> item : product) {
                            Category category = new Category();
                            category.setName(Objects.requireNonNull(item.get(Constants.KEY_NAME)).toString());
                            category.setUnit(Objects.requireNonNull(item.get(Constants.KEY_UNIT)).toString());
                            category.setAmount(Objects.requireNonNull(item.get(Constants.KEY_AMOUNT)).toString());
                            categories.add(category);
                        }
                        warehouseHistory.setmCategory(categories);
                        warehouseHistoryList.add(warehouseHistory);
                    }
                    warehouseHistoryList.sort((o1, o2) -> (o2.getDate().compareTo(o1.getDate())));
                    adapter.notifyDataSetChanged();
                });
    }
}