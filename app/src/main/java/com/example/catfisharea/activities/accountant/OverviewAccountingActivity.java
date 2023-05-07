package com.example.catfisharea.activities.accountant;

import android.graphics.Color;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityOverviewAccountingBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.AccountingAdapter;
import com.example.catfisharea.models.AccountingItem;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class OverviewAccountingActivity extends BaseActivity {
    private ActivityOverviewAccountingBinding mBinding;
    private List<AccountingItem> expenditures;
    private List<AccountingItem> revenues;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private AccountingAdapter expenditureAdapter;
    private AccountingAdapter revenueAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityOverviewAccountingBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        expenditures = new ArrayList<>();
        revenues = new ArrayList<>();
        expenditureAdapter = new AccountingAdapter(expenditures);
        revenueAdapter = new AccountingAdapter(revenues);

        mBinding.recyclerviewExpenditure.setAdapter(expenditureAdapter);
        mBinding.recyclerviewRevenue.setAdapter(revenueAdapter);

        getDataExpend();
        getDataRevenue();
    }

    private void getDataRevenue() {
        Timestamp current = Timestamp.now();
        Date date = new Date((current.toDate().getYear()), current.toDate().getMonth(), 1);
        Timestamp start = new Timestamp(date);

        database.collection(Constants.KEY_COLLECTION_DIARY)
//                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .whereLessThanOrEqualTo(Constants.KEY_DATE_HARVEST, current)
                .whereGreaterThanOrEqualTo(Constants.KEY_DATE_HARVEST, start)
                .get().addOnSuccessListener(diaryQuery -> {
                    AtomicLong revenue = new AtomicLong();
                    for (DocumentSnapshot diaryDoc : diaryQuery.getDocuments()) {
                        String areaId = diaryDoc.getString(Constants.KEY_AREA_ID);
                        if (areaId.equals(preferenceManager.getString(Constants.KEY_AREA_ID))) {
                            database.collection(Constants.KEY_COLLECTION_DIARY)
                                    .document(diaryDoc.getId()).collection(Constants.KEY_COLLECTION_HARVEST)
                                    .get().addOnSuccessListener(harvestQuery -> {

                                        String pondId = diaryDoc.getString(Constants.KEY_POND_ID);
                                        database.collection(Constants.KEY_COLLECTION_POND)
                                                .document(pondId).get().addOnSuccessListener(pondDoc -> {
                                                    revenues.add(new AccountingItem(
                                                            "Thu hoạch " + pondDoc.getString(Constants.KEY_NAME),
                                                            DecimalHelper.formatText(
                                                                Long.parseLong(harvestQuery.getDocuments().get(0).getString(Constants.KEY_TOTAL_MONEY))
                                                            ),
                                                            Color.TRANSPARENT
                                                    ));
                                                    revenueAdapter.notifyDataSetChanged();
                                                });

                                    });
                        }
                    }
                });
    }

    private void getDataExpend() {
        AtomicLong totalFish = new AtomicLong();
        AtomicLong totalCost = new AtomicLong();
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get().addOnSuccessListener(planQuery -> {
                    for (DocumentSnapshot planDoc : planQuery.getDocuments()) {
                        AtomicLong totalFood = new AtomicLong();
                        database.collection(Constants.KEY_COLLECTION_PLAN).document(planDoc.getId())
                                .collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                                .get().addOnSuccessListener(feedQuery -> {
                                    for (DocumentSnapshot feedDoc : feedQuery.getDocuments()) {
                                        totalFood.addAndGet((long) feedDoc.getDouble(Constants.KEY_PRICE).doubleValue());
                                    }
                                    mBinding.food.setText(totalFood + "");
                                    setDataExpend();
                                });
                        totalFish.addAndGet((long) (planDoc.getLong(Constants.KEY_NUMBER_OF_FISH) / planDoc.getLong(Constants.KEY_FINGERLING_SAMPLES) * planDoc.getDouble(Constants.KEY_PRICE)));
                        database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                                .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planDoc.getId())
                                .get().addOnSuccessListener(releaseQuery -> {
                                    for (DocumentSnapshot releaseDoc : releaseQuery.getDocuments()) {
                                        String amount = releaseDoc.getString(Constants.KEY_AMOUNT);
                                        String price = releaseDoc.getString(Constants.KEY_RELEASE_FISH_PRICE);
                                        String model = releaseDoc.getString(Constants.KEY_RELEASE_FISH_MODEL);
                                        totalFish.addAndGet((long) (Long.parseLong(amount) / Long.parseLong(model) * Double.parseDouble(price)));
                                    }
                                    mBinding.fish.setText(totalFish + "");
                                    setDataExpend();
                                });
                        totalCost.addAndGet(planDoc.getLong(Constants.KEY_PREPARATION_COST));
                        mBinding.cost.setText(totalCost + "");
                        setDataExpend();
                    }
                });
    }

    private void setDataExpend() {
        expenditures.clear();
        expenditures.add(new AccountingItem("Thức ăn",
                DecimalHelper.formatText(Double.parseDouble(mBinding.food.getText().toString())),
                Color.TRANSPARENT));
        expenditures.add(new AccountingItem("Thả giống",
                DecimalHelper.formatText(Double.parseDouble(mBinding.fish.getText().toString())),
                Color.TRANSPARENT));
        expenditures.add(new AccountingItem("Cải tạo ao",
                DecimalHelper.formatText(Double.parseDouble(mBinding.cost.getText().toString())),
                Color.TRANSPARENT));

        expenditureAdapter.notifyDataSetChanged();
    }


}