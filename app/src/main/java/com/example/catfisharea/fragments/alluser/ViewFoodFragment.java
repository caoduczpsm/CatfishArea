package com.example.catfisharea.fragments.alluser;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentViewFoodBinding;
import com.example.catfisharea.adapter.ViewFoodAdapter;
import com.example.catfisharea.models.Feed;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

public class ViewFoodFragment extends Fragment {
    private FragmentViewFoodBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private String planId;
    private List<Feed> feedList;
    private ViewFoodAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentViewFoodBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        assert bundle != null;
        pond = (Pond) bundle.getSerializable(Constants.KEY_POND);
        planId = bundle.getString(Constants.KEY_ID_PLAN, null);
        setListener();
        return mBinding.getRoot();
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(requireContext());
        feedList = new ArrayList<>();
        adapter = new ViewFoodAdapter(feedList);

        mBinding.recyclerviewDetailPlan.setAdapter(adapter);
        mBinding.arrange.setOnClickListener(view -> {
            if (mBinding.arrange.getText().equals("Tăng dần")) {
                mBinding.arrange.setText("Giảm dần");
                mBinding.imageArrange.setImageResource(R.drawable.ic_down);
            } else {
                mBinding.arrange.setText("Tăng dần");
                mBinding.imageArrange.setImageResource(R.drawable.ic_up);
            }
            Collections.reverse(feedList);
            adapter.notifyDataSetChanged();
        });
        if (planId == null) {
            getData();
        } else {
            getDataDiary();
        }
    }

    private void getDataDiary() {
        database.collection(Constants.KEY_COLLECTION_DIARY)
                .document(planId)
                .get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        retrieveFoodDiary(documentSnapshot.getId(),
                                Objects.requireNonNull(documentSnapshot.getTimestamp(Constants.KEY_DATE_OF_PLAN)));
                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void retrieveFoodDiary(String id, Timestamp datePlan) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("--MM-dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");
        String dateTime = format2.format(datePlan.toDate());

        database.collection(Constants.KEY_COLLECTION_DIARY).document(id)
                .collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                .get().addOnSuccessListener(feedQuery -> {
                    long totalFood = 0;
                    for (DocumentSnapshot documentSnapshot : feedQuery.getDocuments()) {
                        String date = documentSnapshot.getId();
                        List<String> amountFeed = (List<String>) documentSnapshot.get(Constants.KEY_AMOUNT_FED);
                        try {
                            Date datetime = format.parse(date);
                            Date pondDate = format2.parse(dateTime);
                            assert datetime != null;
                            assert pondDate != null;
                            long diff = datetime.getTime() - pondDate.getTime();
                            long diffDays = diff / (24 * 60 * 60 * 1000);

                            Feed feed = new Feed(documentSnapshot.getId(), id, datetime, amountFeed);
                            feed.setOld(diffDays);
                            if (totalFood == 0) {
                                feed.setTotalFood(Long.parseLong(feed.sumFood()));
                            } else {
                                feed.setTotalFood(totalFood + Long.parseLong(feed.sumFood()));
                            }
                            totalFood += Long.parseLong(feed.sumFood());
                            feedList.add(feed);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }

    private void getData() {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        if (documentSnapshot.exists()) {
                            retrieveFood(documentSnapshot.getId(),
                                    Objects.requireNonNull(documentSnapshot.getTimestamp(Constants.KEY_DATE_OF_PLAN)));
                        }
                    }

                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void retrieveFood(String id, Timestamp datePlan) {
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        @SuppressLint("SimpleDateFormat") SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");
        String dateTime = format2.format(datePlan.toDate());

        database.collection(Constants.KEY_COLLECTION_PLAN).document(id)
                .collection(Constants.KEY_DIARY_COLLECTION_FEEDS)
                .get().addOnSuccessListener(feedQuery -> {
                    long totalFood = 0;
                    for (DocumentSnapshot documentSnapshot : feedQuery.getDocuments()) {
                        String date = documentSnapshot.getId();
                        List<String> amountFeed = (List<String>) documentSnapshot.get(Constants.KEY_AMOUNT_FED);
                        try {
                            Date datetime = format.parse(date);
                            Date pondDate = format2.parse(dateTime);
                            assert datetime != null;
                            assert pondDate != null;
                            long diff = datetime.getTime() - pondDate.getTime();
                            long diffDays = diff / (24 * 60 * 60 * 1000);

                            Feed feed = new Feed(documentSnapshot.getId(), id, datetime, amountFeed);
                            feed.setOld(diffDays);
                            if (totalFood == 0) {
                                feed.setTotalFood(Long.parseLong(feed.sumFood()));
                            } else {
                                feed.setTotalFood(totalFood + Long.parseLong(feed.sumFood()));
                            }
                            totalFood += Long.parseLong(feed.sumFood());
                            feedList.add(feed);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}