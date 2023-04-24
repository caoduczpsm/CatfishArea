package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityDetailPlanBinding;
import com.android.app.catfisharea.databinding.ActivityImportWarehouseBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.PlanAdapter;
import com.example.catfisharea.models.Plan;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DetailPlanActivity extends BaseActivity {
    private ActivityDetailPlanBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private List<Plan> planList;
    private PlanAdapter planAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityDetailPlanBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);
        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        planList = new ArrayList<>();
        planAdapter = new PlanAdapter(planList);
        mBinding.recyclerviewDetailPlan.setAdapter(planAdapter);
        mBinding.toolbarViewPlan.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.toolbarViewPlan.setTitle("Vụ nuôi " + pond.getName());
        getDataDetail();

    }

    private void getDataPlan(Long numberfish, Long pondConsistence) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");
        database.collection(Constants.KEY_COLLECTION_PLAN).document(preferenceManager.getString(Constants.KEY_ID_PLAN))
                .collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                .get().addOnSuccessListener(planQuery -> {
                    for (DocumentSnapshot doc : planQuery.getDocuments()) {
                        Plan plan = new Plan();

                        String date = doc.getId();
                        List<String> amountFeed = (List<String>) doc.get(Constants.KEY_AMOUNT_FED);
                        long total = 0;
                        for (String item : amountFeed) {
                            total += Long.parseLong(item);
                        }
                        long totalFood = Long.parseLong(preferenceManager.getString(Constants.KEY_TOTAL_MONEY)) + total;

                        try {
                            Date datetime = format.parse(date);
                            Date pondDate = format2.parse(preferenceManager.getString(Constants.KEY_DATE_OF_PLAN));
                            long diff = datetime.getTime() - pondDate.getTime();
                            long diffDays = diff / (24 * 60 * 60 * 1000);
                            plan.setDate(datetime);
                            plan.setOld(diffDays);
                            plan.setFood(total);
                            plan.setTotalFood(totalFood);
                            plan.setNumberOfFish(numberfish);
                            plan.setFishWeight(numberfish / pondConsistence);
                            preferenceManager.putString(Constants.KEY_TOTAL_MONEY, String.valueOf(totalFood));
                            planList.add(plan);

                            database.collection(Constants.KEY_COLLECTION_PLAN).document(preferenceManager.getString(Constants.KEY_ID_PLAN))
                                    .collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                    .document(date).get().addOnSuccessListener(fishDoc -> {
                                        if (fishDoc.exists()) {
                                            String loss = fishDoc.getString(Constants.KEY_FISH_WEIGH_LOSS);
                                            String weigh = fishDoc.getString(Constants.KEY_FISH_WEIGH_WEIGHT);
                                            plan.setAVG(Long.parseLong(weigh));
                                            plan.setNumberOfDeadFish(Long.parseLong(loss));
                                            long totalLoss = Long.parseLong(preferenceManager.getString(Constants.KEY_FISH_WEIGH_LOSS)) + Long.parseLong(loss);
                                            preferenceManager.putString(Constants.KEY_FINGERLING_SAMPLES, weigh);
                                            preferenceManager.putString(Constants.KEY_FISH_WEIGH_LOSS, String.valueOf(totalLoss));
                                            plan.setLKnumberOfDeadFish(totalLoss);
                                            plan.setSurvivalRate( ((float) totalLoss) / ((float) numberfish) * 100);
                                            plan.setNumberOfFishAlive(numberfish - totalLoss);
                                        } else {
                                            plan.setAVG(Long.parseLong(preferenceManager.getString(Constants.KEY_FINGERLING_SAMPLES)));
                                            plan.setNumberOfFishAlive(numberfish - Long.parseLong(preferenceManager.getString(Constants.KEY_FISH_WEIGH_LOSS)));
                                        }
                                        planAdapter.notifyDataSetChanged();
                                    });

                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }

                });
    }

    private void getDataDetail() {
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
        mBinding.pondName.setText(pond.getName());
        mBinding.pondAcreage.setText(pond.getAcreage());
//        Log.d("FATAL EXCEPTION: main", pond.getName());


        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get().addOnSuccessListener(planQuery -> {
                    for (DocumentSnapshot doc : planQuery.getDocuments()) {
                        Timestamp time = doc.getTimestamp(Constants.KEY_DATE_OF_PLAN);
                        String pondDate = format.format(time.toDate());
                        long pondConsistence = doc.getLong(Constants.KEY_CONSISTENCE);
                        long pondNumberOfFish = doc.getLong(Constants.KEY_NUMBER_OF_FISH);
                        long fingerlingSamples = doc.getLong(Constants.KEY_FINGERLING_SAMPLES);
                        long price = doc.getLong(Constants.KEY_PRICE);
                        long preparationCost = doc.getLong(Constants.KEY_PREPARATION_COST);

                        preferenceManager.putString(Constants.KEY_ID_PLAN, doc.getId());
                        preferenceManager.putString(Constants.KEY_DATE_OF_PLAN, pondDate);
                        preferenceManager.putString(Constants.KEY_TOTAL_MONEY, "0");
                        preferenceManager.putString(Constants.KEY_FISH_WEIGH_LOSS, "0");
                        preferenceManager.putString(Constants.KEY_FINGERLING_SAMPLES, String.valueOf(fingerlingSamples));

                        mBinding.pondDate.setText((pondDate));
                        mBinding.pondConsistence.setText(DecimalHelper.formatText(pondConsistence));
                        mBinding.pondNumberOfFish.setText(DecimalHelper.formatText(pondNumberOfFish));
                        mBinding.fingerlingSamples.setText(DecimalHelper.formatText(fingerlingSamples));
                        mBinding.price.setText(DecimalHelper.formatText(price));
                        mBinding.preparationCost.setText(DecimalHelper.formatText(preparationCost));
                        getDataPlan(pondNumberOfFish, pondConsistence);
                    }

                });

    }


}