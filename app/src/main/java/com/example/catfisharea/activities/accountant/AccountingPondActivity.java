package com.example.catfisharea.activities.accountant;

import android.graphics.Color;
import android.os.Bundle;

import com.android.app.catfisharea.databinding.ActivityAccountingPondBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.AccountingAdapter;
import com.example.catfisharea.models.AccountingItem;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class AccountingPondActivity extends BaseActivity {
    private ActivityAccountingPondBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private String planId;
    private List<AccountingItem> accountingItems;
    private AccountingAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAccountingPondBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);
        planId = getIntent().getStringExtra(Constants.KEY_ID_PLAN);

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        mBinding.toolbarAccountingPond.setTitle("Báo cáo thu chi " + pond.getName());
        mBinding.toolbarAccountingPond.setNavigationOnClickListener(view -> onBackPressed());
        accountingItems = new ArrayList<>();
        adapter = new AccountingAdapter(accountingItems);
        mBinding.recyclerViewAccounting.setAdapter(adapter);

        getData();
    }

    private void getData() {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .document(planId).collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                .get().addOnSuccessListener(feedQuery -> {
                    long total = 0;
                    for (DocumentSnapshot feedDoc : feedQuery.getDocuments()) {
                        long price = feedDoc.getLong(Constants.KEY_PRICE);
                        total += price;
                    }
                    if (total > 0) {
                        mBinding.food.setText(total +"");
                        accountingItems.add(new AccountingItem("Thức ăn", DecimalHelper.formatText(total), ColorTemplate.PASTEL_COLORS[0]));
                    }
                    adapter.notifyDataSetChanged();
                    updateChart();
                });

        database.collection(Constants.KEY_COLLECTION_PLAN)
                .document(planId).get().addOnSuccessListener(planDoc -> {

                    long cost = planDoc.getLong(Constants.KEY_PREPARATION_COST).longValue();
                    mBinding.cost.setText(cost + "");

                    AtomicLong total = new AtomicLong((long) (planDoc.getLong(Constants.KEY_NUMBER_OF_FISH).longValue() * planDoc.getDouble(Constants.KEY_PRICE).doubleValue()));
                    database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                            .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planId)
                            .get().addOnSuccessListener(releaseQuery -> {
                                for (DocumentSnapshot releaseDoc : releaseQuery.getDocuments()) {
                                    String price = releaseDoc.getString(Constants.KEY_RELEASE_FISH_PRICE);
                                    String amount = releaseDoc.getString(Constants.KEY_AMOUNT);
                                    String model = releaseDoc.getString(Constants.KEY_RELEASE_FISH_MODEL);

                                    total.addAndGet((long) (Long.parseLong(amount) / Long.parseLong(model) * Double.parseDouble(price)));
                                }
                                mBinding.stocking.setText(total +"");
                                accountingItems.add(new AccountingItem("Thả giống", DecimalHelper.formatText(total), ColorTemplate.PASTEL_COLORS[1]));
                                adapter.notifyDataSetChanged();
                                updateChart();
                            });
                });

        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planId)
                .get().addOnSuccessListener(treatmentQuery -> {
                    AtomicLong total = new AtomicLong();
                    for (DocumentSnapshot treatmentDoc : treatmentQuery.getDocuments()) {
                        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                .document(treatmentDoc.getId())
                                .collection("medicinePrices")
                                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                        total.addAndGet((long) Double.parseDouble(documentSnapshot.getString(Constants.KEY_PRICE)));
                                    }
                                    mBinding.treatment.setText(total + "");
                                    updateChart();
                                });
                    }
                });

    }

    private void updateChart() {
        ArrayList<BarEntry> barEntries = new ArrayList<>();
        barEntries.add(new BarEntry(1, Float.parseFloat(mBinding.cost.getText().toString())));
        barEntries.add(new BarEntry(2, Float.parseFloat(mBinding.food.getText().toString())));
        barEntries.add(new BarEntry(3, Float.parseFloat(mBinding.treatment.getText().toString())));
        barEntries.add(new BarEntry(4, Float.parseFloat(mBinding.stocking.getText().toString())));

        accountingItems.clear();
        accountingItems.add(new AccountingItem("Phí cải tạo ao", DecimalHelper.formatText(Float.parseFloat(mBinding.cost.getText().toString())),
                ColorTemplate.PASTEL_COLORS[0]));
        accountingItems.add(new AccountingItem("Thức ăn", DecimalHelper.formatText(Float.parseFloat(mBinding.food.getText().toString())),
                ColorTemplate.PASTEL_COLORS[1]));
        accountingItems.add(new AccountingItem("Thuốc", DecimalHelper.formatText(Float.parseFloat(mBinding.treatment.getText().toString())),
                ColorTemplate.PASTEL_COLORS[2]));
        accountingItems.add(new AccountingItem("Thả giống", DecimalHelper.formatText(Float.parseFloat(mBinding.stocking.getText().toString())),
                ColorTemplate.PASTEL_COLORS[3]));
        adapter.notifyDataSetChanged();

        BarDataSet barDataSet = new BarDataSet(barEntries, "");
        barDataSet.setColors(ColorTemplate.PASTEL_COLORS);
        barDataSet.setValueTextColor(Color.BLACK);
        barDataSet.setValueTextSize(16f);

        BarData barData = new BarData(barDataSet);

        mBinding.barchart.setFitBars(true);
        mBinding.barchart.setData(barData);
        mBinding.barchart.getDescription().setEnabled(false);
        mBinding.barchart.animateY(1000);
    }
}