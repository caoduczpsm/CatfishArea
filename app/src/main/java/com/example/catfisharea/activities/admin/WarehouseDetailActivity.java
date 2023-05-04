package com.example.catfisharea.activities.admin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityWarehouseDetailBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.WarehouseAnotherDetailAdapter;
import com.example.catfisharea.adapter.WarehouseFoodDetailAdapter;
import com.example.catfisharea.adapter.WarehouseMedicineDetailAdapter;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class WarehouseDetailActivity extends BaseActivity {
    private ActivityWarehouseDetailBinding mBinding;
    private FirebaseFirestore database;
    private String warehouseID;
    private String areaID;
    private List<Category> foods, medicines, anothers;
    private WarehouseFoodDetailAdapter foodDetailAdapter;
    private WarehouseMedicineDetailAdapter medicineDetailAdapter;
    private WarehouseAnotherDetailAdapter anotherDetailAdapter;
    private PreferenceManager preferenceManager;

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
        foods = new ArrayList<>();
        medicines = new ArrayList<>();
        anothers = new ArrayList<>();

        String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        switch (type) {
            case Constants.KEY_ACCOUNTANT:
                mBinding.layoutGoods.setVisibility(View.VISIBLE);
                break;
        }

        foodDetailAdapter = new WarehouseFoodDetailAdapter(foods);
        medicineDetailAdapter = new WarehouseMedicineDetailAdapter(medicines);
        anotherDetailAdapter = new WarehouseAnotherDetailAdapter(anothers);

        mBinding.recyclerViewFood.setAdapter(foodDetailAdapter);
        mBinding.recyclerViewMedicine.setAdapter(medicineDetailAdapter);
        mBinding.recyclerViewAnother.setAdapter(anotherDetailAdapter);

        RecyclerView.ItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        mBinding.recyclerViewFood.addItemDecoration(itemDecoration);
        mBinding.recyclerViewMedicine.addItemDecoration(itemDecoration);
        mBinding.recyclerViewAnother.addItemDecoration(itemDecoration);

        mBinding.imageBack.setOnClickListener(view -> onBackPressed());

        mBinding.layoutGoods.setOnClickListener(view -> {
            Intent intent = new Intent(this, ImportWarehouseActivity.class);
            intent.putExtra(Constants.KEY_WAREHOUSE_ID, warehouseID);
            intent.putExtra(Constants.KEY_AREA_ID, areaID);
            startActivity(intent);
        });

        mBinding.layoutHistory.setOnClickListener(view -> {
            Intent intent = new Intent(this, WarehouseHistoryActivity.class);
            intent.putExtra(Constants.KEY_WAREHOUSE_ID, warehouseID);
            intent.putExtra(Constants.KEY_AREA_ID, areaID);
            startActivity(intent);
        });

        mBinding.textFoodTitle.setOnClickListener(view -> {
            if (mBinding.recyclerViewFood.getVisibility() == View.GONE){
                mBinding.imageFoodTitle.setImageResource(R.drawable.ic_down);
                mBinding.recyclerViewFood.setVisibility(View.VISIBLE);
            } else {
                mBinding.imageFoodTitle.setImageResource(R.drawable.ic_up);
                mBinding.recyclerViewFood.setVisibility(View.GONE);
            }
        });

        mBinding.imageFoodTitle.setOnClickListener(view -> {
            if (mBinding.recyclerViewFood.getVisibility() == View.GONE){
                mBinding.imageFoodTitle.setImageResource(R.drawable.ic_down);
                mBinding.recyclerViewFood.setVisibility(View.VISIBLE);
            } else {
                mBinding.imageFoodTitle.setImageResource(R.drawable.ic_up);
                mBinding.recyclerViewFood.setVisibility(View.GONE);
            }
        });

        mBinding.textMedicineTitle.setOnClickListener(view -> {
            if (mBinding.recyclerViewMedicine.getVisibility() == View.GONE){
                mBinding.imageMedicineTitle.setImageResource(R.drawable.ic_down);
                mBinding.recyclerViewMedicine.setVisibility(View.VISIBLE);
            } else {
                mBinding.imageMedicineTitle.setImageResource(R.drawable.ic_up);
                mBinding.recyclerViewMedicine.setVisibility(View.GONE);
            }
        });

        mBinding.imageMedicineTitle.setOnClickListener(view -> {
            if (mBinding.recyclerViewMedicine.getVisibility() == View.GONE){
                mBinding.imageMedicineTitle.setImageResource(R.drawable.ic_down);
                mBinding.recyclerViewMedicine.setVisibility(View.VISIBLE);
            } else {
                mBinding.imageMedicineTitle.setImageResource(R.drawable.ic_up);
                mBinding.recyclerViewMedicine.setVisibility(View.GONE);
            }
        });

        mBinding.textAnotherTitle.setOnClickListener(view -> {
            if (anothers.size() > 0){
                if (mBinding.recyclerViewAnother.getVisibility() == View.GONE){
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_down);
                    mBinding.recyclerViewAnother.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_up);
                    mBinding.recyclerViewAnother.setVisibility(View.GONE);
                }
            } else {
                if (mBinding.textAnotherMessage.getVisibility() == View.GONE){
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_down);
                    mBinding.textAnotherMessage.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_up);
                    mBinding.textAnotherMessage.setVisibility(View.GONE);
                }
            }
        });

        mBinding.imageAnotherTitle.setOnClickListener(view -> {
            if (anothers.size() > 0){
                if (mBinding.recyclerViewAnother.getVisibility() == View.GONE){
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_down);
                    mBinding.recyclerViewAnother.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_up);
                    mBinding.recyclerViewAnother.setVisibility(View.GONE);
                }
            } else {
                if (mBinding.textAnotherMessage.getVisibility() == View.GONE){
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_down);
                    mBinding.textAnotherMessage.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageAnotherTitle.setImageResource(R.drawable.ic_up);
                    mBinding.textAnotherMessage.setVisibility(View.GONE);
                }
            }
        });

        getTotalData();
    }

    private void getTotalData() {
        AtomicLong total = new AtomicLong();
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE).document(warehouseID)
                .collection(Constants.KEY_CATEGORY_OF_WAREHOUSE).get().addOnSuccessListener(categoryQuery -> {
                    for (DocumentSnapshot documentSnapshot : categoryQuery.getDocuments()) {
                        String price = documentSnapshot.getString(Constants.KEY_PRICE);
                        String amount = documentSnapshot.getString(Constants.KEY_AMOUNT);
                        if (price == null) price = "0";
                        assert amount != null;
                        total.addAndGet((long) (Double.parseDouble(price) * Long.parseLong(amount)));
                    }
                    mBinding.total.setText(DecimalHelper.formatText(total));
                });
    }

    @Override
    protected void onStart() {
        super.onStart();
        getData();
    }

    @SuppressLint({"NewApi", "NotifyDataSetChanged"})
    private void getData() {
        foods.clear();
        medicines.clear();
        anothers.clear();
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                .document(warehouseID).get().addOnSuccessListener(warehouseDoc -> {
                    String name = warehouseDoc.getString(Constants.KEY_NAME);
                    mBinding.textName.setText(name);
                });
        database.collection(Constants.KEY_COLLECTION_WAREHOUSE).document(warehouseID)
                .collection(Constants.KEY_CATEGORY_OF_WAREHOUSE)
                .get().addOnSuccessListener(warehouseQuery -> {
                    for (DocumentSnapshot doc : warehouseQuery) {
                        if (Objects.equals(doc.getString(Constants.KEY_CATEGORY_TYPE), Constants.KEY_CATEGORY_TYPE_FOOD)){
                            String name = doc.getString(Constants.KEY_NAME);
                            String producer = doc.getString(Constants.KEY_PRODUCER);
                            String amount = doc.getString(Constants.KEY_AMOUNT);
                            String unit = doc.getString(Constants.KEY_UNIT);
                            Category category = new Category(doc.getId(), name);
                            category.setProducer(producer);
                            category.setAmount(amount);
                            category.setUnit(unit);
                            foods.add(category);
                            foodDetailAdapter.notifyDataSetChanged();
                        } else if (Objects.equals(doc.getString(Constants.KEY_CATEGORY_TYPE), Constants.KEY_CATEGORY_TYPE_MEDICINE)){
                            String name = doc.getString(Constants.KEY_NAME);
                            String producer = doc.getString(Constants.KEY_PRODUCER);
                            String amount = doc.getString(Constants.KEY_AMOUNT);
                            String unit = doc.getString(Constants.KEY_UNIT);
                            Category category = new Category(doc.getId(), name);
                            category.setProducer(producer);
                            category.setAmount(amount);
                            category.setUnit(unit);
                            medicines.add(category);
                            medicineDetailAdapter.notifyDataSetChanged();
                        } else {
                            String name = doc.getString(Constants.KEY_NAME);
                            String producer = doc.getString(Constants.KEY_PRODUCER);
                            String amount = doc.getString(Constants.KEY_AMOUNT);
                            String unit = doc.getString(Constants.KEY_UNIT);
                            Category category = new Category(doc.getId(), name);
                            category.setProducer(producer);
                            category.setAmount(amount);
                            category.setUnit(unit);
                            anothers.add(category);
                            anotherDetailAdapter.notifyDataSetChanged();
                        }
                        if (anothers.size() > 0) {
                            mBinding.recyclerViewAnother.setVisibility(View.VISIBLE);
                            mBinding.textAnotherMessage.setVisibility(View.GONE);
                        } else {
                            mBinding.recyclerViewAnother.setVisibility(View.GONE);
                            mBinding.textAnotherMessage.setVisibility(View.VISIBLE);
                        }
                    }

                });
    }


}