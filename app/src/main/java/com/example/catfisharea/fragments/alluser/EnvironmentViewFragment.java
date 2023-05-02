package com.example.catfisharea.fragments.alluser;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentEnvironmentViewBinding;
import com.example.catfisharea.adapter.EnvironmentViewAdapter;
import com.example.catfisharea.adapter.ViewFoodAdapter;
import com.example.catfisharea.models.Environment;
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

public class EnvironmentViewFragment extends Fragment {
    private FragmentEnvironmentViewBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private String planId;
    private List<Environment> environmentList;
    private EnvironmentViewAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentEnvironmentViewBinding.inflate(inflater, container, false);
        Bundle bundle = getArguments();
        pond = (Pond) bundle.getSerializable(Constants.KEY_POND);
        planId = bundle.getString(Constants.KEY_ID_PLAN, null);
        setListener();

        return mBinding.getRoot();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getContext());
        environmentList = new ArrayList<>();
        adapter = new EnvironmentViewAdapter(environmentList);

        mBinding.recyclerviewDetailPlan.setAdapter(adapter);
        mBinding.arrange.setOnClickListener(view -> {
            if (mBinding.arrange.getText().equals("Tăng dần")) {
                mBinding.arrange.setText("Giảm dần");
                mBinding.imageArrange.setImageResource(R.drawable.ic_down);
            } else {
                mBinding.arrange.setText("Tăng dần");
                mBinding.imageArrange.setImageResource(R.drawable.ic_up);
            }
            Collections.reverse(environmentList);
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
                        retrieveWaterDiary(documentSnapshot.getId(),
                                documentSnapshot.getTimestamp(Constants.KEY_DATE_OF_PLAN));
                    }
                });
    }

    private void retrieveWaterDiary(String id, Timestamp timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");
        String dateTime = format2.format(timestamp.toDate());

        database.collection(Constants.KEY_COLLECTION_DIARY).document(id)
                .collection(Constants.KEY_DIARY_COLLECTION_WATER)
                .get().addOnSuccessListener(waterQuery -> {
                    for (DocumentSnapshot documentSnapshot : waterQuery.getDocuments()) {
                        String date = documentSnapshot.getId();
                        Map<String, String> parameter = (Map<String, String>) documentSnapshot.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                        try {
                            Date datetime = format.parse(date);
                            Date pondDate = format2.parse(dateTime);
                            long diff = datetime.getTime() - pondDate.getTime();
                            long diffDays = diff / (24 * 60 * 60 * 1000);

                            Environment environment = new Environment(date, id, datetime, parameter, diffDays);

                            environmentList.add(environment);
                        } catch (Exception e) {

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
                            retrieveWater(documentSnapshot.getId(),
                                    documentSnapshot.getTimestamp(Constants.KEY_DATE_OF_PLAN));
                        }
                    }

                });
    }

    private void retrieveWater(String id, Timestamp timestamp) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat format2 = new SimpleDateFormat("dd-MM-yyyy");
        String dateTime = format2.format(timestamp.toDate());

        database.collection(Constants.KEY_COLLECTION_PLAN).document(id)
                .collection(Constants.KEY_DIARY_COLLECTION_WATER)
                .get().addOnSuccessListener(waterQuery -> {
                    for (DocumentSnapshot documentSnapshot : waterQuery.getDocuments()) {
                        String date = documentSnapshot.getId();
                        Map<String, String> parameter = (Map<String, String>) documentSnapshot.get(Constants.KEY_SPECIFICATIONS_MEASURED);
                        try {
                            Date datetime = format.parse(date);
                            Date pondDate = format2.parse(dateTime);
                            long diff = datetime.getTime() - pondDate.getTime();
                            long diffDays = diff / (24 * 60 * 60 * 1000);

                            Environment environment = new Environment(date, id, datetime, parameter, diffDays);

                            environmentList.add(environment);
                        } catch (Exception e) {

                        }
                        adapter.notifyDataSetChanged();
                    }
                });
    }
}