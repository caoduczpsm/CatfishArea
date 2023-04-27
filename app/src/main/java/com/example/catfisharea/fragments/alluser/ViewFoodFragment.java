package com.example.catfisharea.fragments.alluser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

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
import java.util.Map;

public class ViewFoodFragment extends Fragment {
    private FragmentViewFoodBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private List<Feed> feedList;
    private ViewFoodAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentViewFoodBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        pond = (Pond) bundle.getSerializable(Constants.KEY_POND);
        setListener();
        return mBinding.getRoot();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        feedList = new ArrayList<>();
        adapter = new ViewFoodAdapter(feedList);

        mBinding.recyclerviewDetailPlan.setAdapter(adapter);
        mBinding.arrange.setOnClickListener(view -> {
            if (mBinding.arrange.getText().equals("Tăng dần")) {
                mBinding.arrange.setText("Giảm dần");
                mBinding.imageArrange.setImageResource(R.drawable.ic_down);
                Collections.reverse(feedList);
                adapter.notifyDataSetChanged();
            } else {
                mBinding.arrange.setText("Tăng dần");
                mBinding.imageArrange.setImageResource(R.drawable.ic_up);
                Collections.reverse(feedList);
                adapter.notifyDataSetChanged();
            }
        });

        getData();
    }

    private void getData() {
        database.collection(Constants.KEY_COLLECTION_PLAN)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                        if (documentSnapshot.exists()) {
                            retrieveFood(documentSnapshot.getId(),
                                    documentSnapshot.getTimestamp(Constants.KEY_DATE_OF_PLAN));
                        }
                    }

                });

    }

    private void retrieveFood(String id, Timestamp datePlan) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");
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

                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}