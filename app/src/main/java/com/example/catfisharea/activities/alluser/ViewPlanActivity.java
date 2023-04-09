package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.android.app.catfisharea.databinding.ActivityViewPlanBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.PlanAdapter;

import com.example.catfisharea.models.Plan;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ViewPlanActivity extends BaseActivity {
    private ActivityViewPlanBinding mBinding;
    private FirebaseFirestore database;
    private List<Plan> mPlans;
    private PreferenceManager preferenceManager;
    private PlanAdapter planAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityViewPlanBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        mBinding.toolbarViewPlan.setNavigationOnClickListener(view -> onBackPressed());
        getDataPlan();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getDataPlan() {
        mPlans = new ArrayList<>();
        planAdapter = new PlanAdapter(mPlans);
        mBinding.recyclerViewPlan.setAdapter(planAdapter);
        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get().addOnSuccessListener(snapShotCampus -> {
                    for (DocumentSnapshot documentSnapshot : snapShotCampus.getDocuments()) {
                        database.collection(Constants.KEY_COLLECTION_PLAN)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, documentSnapshot.getId())
                                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                                    Plan plan = new Plan();
                                    plan.setPlanId(documentSnapshot.getId());
                                    plan.setPondName(documentSnapshot.getString(Constants.KEY_NAME));
                                    plan.setPondId(documentSnapshot.getString(Constants.KEY_POND_ID));

                                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                                        String acreage = doc.getString(Constants.KEY_ACREAGE);
                                        int consistence = Objects.requireNonNull(doc.getLong(Constants.KEY_CONSISTENCE)).intValue();
                                        int numberOfFish = Objects.requireNonNull(doc.getLong(Constants.KEY_NUMBER_OF_FISH)).intValue();
                                        float survivalRate = Objects.requireNonNull(doc.getDouble(Constants.KEY_SURVIVAL_RATE)).floatValue();
                                        int numberOfFishAlive = Objects.requireNonNull(doc.getLong(Constants.KEY_NUMBER_OF_FISH_ALIVE)).intValue();
                                        float harvestSize = Objects.requireNonNull(doc.getDouble(Constants.KEY_HARVEST_SIZE)).floatValue();
                                        int harvestYield = Objects.requireNonNull(doc.getLong(Constants.KEY_HARVEST_YIELD)).intValue();
                                        float fcr = Objects.requireNonNull(doc.getDouble(Constants.KEY_FCR)).floatValue();
                                        int food = Objects.requireNonNull(doc.getLong(Constants.KEY_FOOD)).intValue();
                                        int fingerlingSamples = Objects.requireNonNull(doc.getLong(Constants.KEY_FINGERLING_SAMPLES)).intValue();
                                        assert acreage != null;
                                        plan.setAcreage(Integer.parseInt(plan.getAcreage()) + Integer.parseInt(acreage) + "");
                                        plan.setConsistence(plan.getConsistence() + consistence);
                                        plan.setNumberOfFish(plan.getNumberOfFish() + numberOfFish);
                                        plan.setSurvivalRate(plan.getSurvivalRate() + survivalRate);
                                        plan.setNumberOfFishAlive(plan.getNumberOfFishAlive() + numberOfFishAlive);
                                        plan.setHarvestYield(plan.getHarvestYield() + harvestYield);
                                        plan.setHarvestSize(plan.getHarvestSize() + harvestSize);
                                        plan.setFcr(plan.getFcr() + fcr);
                                        plan.setFood(plan.getFood() + food);
                                        plan.setFingerlingSamples(plan.getFingerlingSamples() + fingerlingSamples);
                                    }
                                    if (plan.getNumberOfFish() != 0) {
                                        mPlans.add(plan);
                                    }
                                    planAdapter.notifyDataSetChanged();
                                });
                    }
                });

    }

}