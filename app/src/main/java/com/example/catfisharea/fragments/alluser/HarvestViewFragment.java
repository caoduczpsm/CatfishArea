package com.example.catfisharea.fragments.alluser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentHarvestViewBinding;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class HarvestViewFragment extends Fragment {
    private FragmentHarvestViewBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String planId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentHarvestViewBinding.inflate(inflater, container, false);
        planId = getArguments().getString(Constants.KEY_ID_PLAN);
        setListener();
        return mBinding.getRoot();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());

        getDataRevenue();
        getDataExpend();
    }

    private void getDataExpend() {
        database.collection(Constants.KEY_COLLECTION_DIARY)
                .document(planId).collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                .get().addOnSuccessListener(feedQuery -> {
                    double total = 0;
                    for (DocumentSnapshot feedDoc : feedQuery.getDocuments()) {
                        double price = feedDoc.getDouble(Constants.KEY_PRICE).doubleValue();
                        total += price;
                    }
                    mBinding.food.setText(DecimalHelper.formatText(total));
                    setTotal();
                });
        database.collection(Constants.KEY_COLLECTION_DIARY)
                .document(planId).get().addOnSuccessListener(documentSnapshot -> {
                    long cost = documentSnapshot.getLong(Constants.KEY_PREPARATION_COST).longValue();
                    double price = documentSnapshot.getLong(Constants.KEY_NUMBER_OF_FISH).longValue()
                            / documentSnapshot.getLong(Constants.KEY_FINGERLING_SAMPLES).longValue()
                            * documentSnapshot.getDouble(Constants.KEY_PRICE).doubleValue();
                    mBinding.cost.setText(DecimalHelper.formatText(cost));
                    mBinding.stocking.setText(DecimalHelper.formatText(price));
                    setTotal();
                });

        database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planId)
                .get().addOnSuccessListener(releaseQuery -> {
                    for (DocumentSnapshot releaseDoc : releaseQuery.getDocuments()) {
                        String price = releaseDoc.getString(Constants.KEY_RELEASE_FISH_PRICE);
                        String amount = releaseDoc.getString(Constants.KEY_AMOUNT);
                        String model = releaseDoc.getString(Constants.KEY_RELEASE_FISH_MODEL);
                        double total = Long.parseLong(amount) / Long.parseLong(model) * Double.parseDouble(price);
                        double old = DecimalHelper.parseText(mBinding.stocking.getText().toString()).doubleValue();
                        mBinding.stocking.setText(String.valueOf(old + total));
                    }
                    setTotal();
                });

        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planId)
                .get().addOnSuccessListener(treatmentQuery -> {
                    for (DocumentSnapshot documentSnapshot : treatmentQuery.getDocuments()) {
                        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                .document(documentSnapshot.getId())
                                .collection("medicinePrices")
                                .get().addOnSuccessListener(priceQuery -> {
                                    for (DocumentSnapshot priceDoc : priceQuery.getDocuments()) {
                                        String price = priceDoc.getString(Constants.KEY_PRICE);
                                        Double old = DecimalHelper.parseText(mBinding.treatment.getText().toString()).doubleValue();
                                        mBinding.treatment.setText(
                                                DecimalHelper.formatText(
                                                        old + Double.parseDouble(price)
                                                )
                                        );
                                    }
                                });
                    }
                });

    }

    private void setTotal() {
        double food = DecimalHelper.parseText(mBinding.food.getText().toString()).doubleValue();
        double stocking = DecimalHelper.parseText(mBinding.stocking.getText().toString()).doubleValue();
        double cost = DecimalHelper.parseText(mBinding.cost.getText().toString()).doubleValue();
        double treatment = DecimalHelper.parseText(mBinding.treatment.getText().toString()).doubleValue();

        mBinding.totalExpend.setText(DecimalHelper.formatText(food + stocking + cost + treatment));
    }

    private void getDataRevenue() {
        database.collection(Constants.KEY_COLLECTION_DIARY)
                .document(planId).collection(Constants.KEY_COLLECTION_HARVEST)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(0);
                    Timestamp timestamp = documentSnapshot.getTimestamp(Constants.KEY_DATE_OF_PLAN);
                    String num = documentSnapshot.getString(Constants.KEY_NUMBER_OF_FISH);
                    String price = documentSnapshot.getString(Constants.KEY_PRICE);
                    String weigh = documentSnapshot.getString(Constants.KEY_FISH_WEIGH_WEIGHT);
                    String quantity = documentSnapshot.getString(Constants.KEY_QUANTITY);
                    String total = documentSnapshot.getString(Constants.KEY_TOTAL_MONEY);

                    mBinding.date.setText(DecimalHelper.formatDate(timestamp.toDate()));
                    mBinding.number.setText(DecimalHelper.formatText(Long.parseLong(num)) + " con");
                    mBinding.price.setText(DecimalHelper.formatText(Double.parseDouble(price)) + " VNĐ/kg");
                    mBinding.quantity.setText(quantity + " tấn");
                    mBinding.weigh.setText(weigh + " gram");
                    mBinding.total.setText(DecimalHelper.formatText(Long.parseLong(total)));

                });
    }
}