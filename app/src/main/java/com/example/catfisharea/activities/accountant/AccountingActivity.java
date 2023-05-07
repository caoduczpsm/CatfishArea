package com.example.catfisharea.activities.accountant;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.android.app.catfisharea.databinding.ActivityAccountingBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.PondDetailsActivity;
import com.example.catfisharea.adapter.HomeAdapter;
import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

public class AccountingActivity extends BaseActivity implements CampusListener {
    private ActivityAccountingBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private PieDataSet pieDataSet;
    private HomeAdapter homeAdapter;
    private List<ItemHome> itemHomes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityAccountingBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        mBinding.toolbarAccounting.setNavigationOnClickListener(view -> onBackPressed());
        createPieChart();

        itemHomes = new ArrayList<>();
        homeAdapter = new HomeAdapter(this, itemHomes, this);
        mBinding.recyclerViewAccounting.setAdapter(homeAdapter);
        mBinding.overview.setOnClickListener(view -> {
            startActivity(new Intent(this, OverviewAccountingActivity.class));
        });
        getDataHome();
    }

    @SuppressLint("SetTextI18n")
    private void createPieChart() {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get().addOnSuccessListener(planQuery -> {
                    AtomicLong expenditure = new AtomicLong();
                    for (DocumentSnapshot planDoc : planQuery.getDocuments()) {
                        long preparationCost = planDoc.getLong(Constants.KEY_PREPARATION_COST);
                        expenditure.addAndGet(preparationCost);
                        long stocking = planDoc.getLong(Constants.KEY_PRICE) * planDoc.getLong(Constants.KEY_NUMBER_OF_FISH);
                        expenditure.addAndGet(stocking);
                        database.collection(Constants.KEY_COLLECTION_PLAN).document(planDoc.getId())
                                .collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                                .get().addOnSuccessListener(feedQuery -> {
                                    for (DocumentSnapshot feedDoc : feedQuery.getDocuments()) {
                                        long price = feedDoc.getLong(Constants.KEY_PRICE);
                                        expenditure.addAndGet(price);
                                    }
                                    mBinding.expenditure.setText(expenditure + "");
                                    setDataChart();
                                });
                        database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                                .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planDoc.getId())
                                .get().addOnSuccessListener(releaseQuery -> {
                                    for (DocumentSnapshot releaseDoc : releaseQuery.getDocuments()) {
                                        String amount = releaseDoc.getString(Constants.KEY_AMOUNT);
                                        String price = releaseDoc.getString(Constants.KEY_RELEASE_FISH_PRICE);
                                        long total = Long.parseLong(amount) * Long.parseLong(price);
                                        mBinding.expenditure.setText(
                                                (
                                                        Long.parseLong(mBinding.expenditure.getText().toString())
                                                                + total
                                                ) + ""
                                        );
                                    }
                                    setDataChart();
                                });
                        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planDoc.getId())
                                .get().addOnSuccessListener(treatmentQuery -> {
                                    for (DocumentSnapshot treatmentDoc : treatmentQuery.getDocuments()) {
                                        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                                .document(treatmentDoc.getId())
                                                .collection("medicinePrices")
                                                .get().addOnSuccessListener(priceQuery -> {
                                                    for (DocumentSnapshot documentSnapshot : priceQuery.getDocuments()) {
                                                        String price = documentSnapshot.getString(Constants.KEY_PRICE);
                                                        String arr = price.substring(0, price.lastIndexOf("."));

                                                        mBinding.expenditure.setText(
                                                                (
                                                                        Long.parseLong(mBinding.expenditure.getText().toString()) +
                                                                                Long.parseLong(arr)
                                                                ) + ""
                                                        );
                                                    }
                                                    setDataChart();
                                                });
                                    }
                                });
                    }

                });


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
                                        String total = harvestQuery.getDocuments().get(0).getString(Constants.KEY_TOTAL_MONEY);
                                        revenue.addAndGet(Long.parseLong(total));
                                        mBinding.revenue.setText(revenue + "");
                                        setDataChart();
                                    });
                        }
                    }
                });
    }

    private void setDataChart() {
        ArrayList<PieEntry> dataVal = new ArrayList<>();
        dataVal.add(new PieEntry(Long.parseLong(mBinding.expenditure.getText().toString()), "Chi"));
        dataVal.add(new PieEntry(Long.parseLong(mBinding.revenue.getText().toString()), "Thu"));
        pieDataSet = new PieDataSet(dataVal, "");

        List<Integer> colors = new ArrayList<>();
        colors.add(Color.rgb(0, 129, 201));
        colors.add(Color.rgb(239, 163, 157));

        pieDataSet.setColors(colors);

        pieDataSet.setValueTextSize(18f);
        pieDataSet.setValueTextColor(Color.WHITE);

        PieData pieData = new PieData(pieDataSet);

        mBinding.pieChart.setDrawEntryLabels(false);
        mBinding.pieChart.getDescription().setText("Các khoản chi tiêu ");
        mBinding.pieChart.getDescription().setTextSize(16f);
        mBinding.pieChart.setUsePercentValues(false);
        mBinding.pieChart.setEntryLabelTextSize(18f);
        mBinding.pieChart.setCenterTextRadiusPercent(50);
        mBinding.pieChart.setHoleRadius(30);
        mBinding.pieChart.setTransparentCircleRadius(40);
        mBinding.pieChart.setTransparentCircleColor(Color.RED);
        mBinding.pieChart.setTransparentCircleAlpha(50);
        mBinding.pieChart.setData(pieData);
        mBinding.pieChart.invalidate();
        mBinding.pieChart.getDescription().setEnabled(false);
        mBinding.pieChart.animateX(1000);
    }

    private void getDataHome() {
        String areaId = preferenceManager.getString(Constants.KEY_AREA_ID);
        if (areaId == null) return;

        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, areaId)
                .get().addOnSuccessListener(campusQuery -> {
                    for (DocumentSnapshot campusDoc : campusQuery.getDocuments()) {
                        ItemHome itemHome = new ItemHome();
                        List<RegionModel> regionModels = new ArrayList<>();
                        String campusId = campusDoc.getId();
                        String campusName = campusDoc.getString(Constants.KEY_NAME);
                        Campus campus = new Campus(campusId, campusName);
                        itemHome.setRegionModel(campus);
                        database.collection(Constants.KEY_COLLECTION_POND)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                                .get().addOnSuccessListener(pondQuery -> {
                                    for (DocumentSnapshot pondDoc : pondQuery.getDocuments()) {
                                        String pondId = pondDoc.getId();
                                        database.collection(Constants.KEY_COLLECTION_PLAN)
                                                .whereEqualTo(Constants.KEY_POND_ID, pondId)
                                                .get().addOnSuccessListener(planQuery -> {
                                                    for (DocumentSnapshot documentSnapshot : planQuery.getDocuments()) {
                                                        if (documentSnapshot.exists()) {
                                                            String pondName = pondDoc.getString(Constants.KEY_NAME);
                                                            String acreage = pondDoc.getString(Constants.KEY_ACREAGE);
                                                            List<String> numOfFeedingList = (List<String>) pondDoc.get(Constants.KEY_NUM_OF_FEEDING_LIST);
                                                            List<String> amountFedList = (List<String>) pondDoc.get(Constants.KEY_AMOUNT_FED);
                                                            List<String> specificationsToMeasureList = (List<String>) pondDoc.get(Constants.KEY_SPECIFICATIONS_TO_MEASURE);
                                                            HashMap<String, Object> parameters = (HashMap<String, Object>) pondDoc.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                                                            int numOfFeeding = Integer.parseInt(Objects.requireNonNull(pondDoc.getString(Constants.KEY_NUM_OF_FEEDING)));
                                                            Pond pond = new Pond(pondId, pondName, null, campusId, acreage, numOfFeeding, numOfFeedingList, amountFedList, specificationsToMeasureList, parameters);
                                                            regionModels.add(pond);
                                                            itemHome.setReginonList(regionModels);
                                                            itemHomes.add(itemHome);
                                                            Collections.sort(itemHome.getReginonList(),
                                                                    (o1, o2) -> (o1.getName()
                                                                            .compareToIgnoreCase(o2.getName())));
                                                            Collections.sort(itemHomes,
                                                                    (o1, o2) -> (o1.getRegionModel().getName()
                                                                            .compareToIgnoreCase(o2.getRegionModel().getName())));
                                                            homeAdapter.notifyDataSetChanged();
                                                        }
                                                    }
                                                });

                                    }


                                });
                    }
                });
    }


    @Override
    public void OnCampusClicker(RegionModel regionModel) {

    }

    @Override
    public void OnPondClicker(RegionModel regionModel) {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, regionModel.getId())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (queryDocumentSnapshot.getString(Constants.KEY_POND_ID) != null) {
                                Intent intent = new Intent(this, AccountingPondActivity.class);
                                intent.putExtra(Constants.KEY_POND, regionModel);
                                intent.putExtra(Constants.KEY_ID_PLAN, queryDocumentSnapshot.getId());
                                startActivity(intent);
                            }
                        }
                    }
                });
    }

    @Override
    public void onCreatePlan(RegionModel regionModel) {

    }
}