package com.example.catfisharea.fragments.alluser;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.app.catfisharea.databinding.FragmentTreatmentRejectedBinding;
import com.example.catfisharea.adapter.TreatmentRequestAdapter;
import com.example.catfisharea.listeners.TreatmentListener;
import com.example.catfisharea.models.Treatment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TreatmentRejectedFragment extends Fragment implements TreatmentListener {

    private FragmentTreatmentRejectedBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private TreatmentRequestAdapter treatmentRequestAdapter;
    private List<Treatment> treatments;
    String daySelected, monthSelected, yearSelected;

    public TreatmentRejectedFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTreatmentRejectedBinding.inflate(inflater, container, false);
        init();
        getRequest();

        return mBinding.getRoot();
    }

    @SuppressLint("SetTextI18n")
    private void init(){

        //FirebaseFirestore
        database = FirebaseFirestore.getInstance();

        //PreferenceManager
        preferenceManager = new PreferenceManager(requireContext());

        //List
        treatments = new ArrayList<>();

        //Adapter
        treatmentRequestAdapter = new TreatmentRequestAdapter(getContext(), treatments, this);
        mBinding.requestRecyclerView.setAdapter(treatmentRequestAdapter);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF))
            mBinding.textMessage.setText("Hôm nay, chưa có yêu cầu nào.");

    }

    public void getRequest(){

        daySelected = preferenceManager.getString(Constants.KEY_DAY_SELECTED);
        monthSelected = preferenceManager.getString(Constants.KEY_MONTH_SELECTED);
        yearSelected = preferenceManager.getString(Constants.KEY_YEAR_SELECTED);

        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
            getTreatmentRejectedForRegional();
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
            getTreatmentRejectedForDirector();
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getTreatmentRejectedForRegional(){
        treatments.clear();
        String dateSelected = yearSelected + "-" + monthSelected + "-" + daySelected;
        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE), dateSelected)){
                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS), Constants.KEY_TREATMENT_REJECT)){
                                Treatment treatment = new Treatment();
                                treatment.id = queryDocumentSnapshot.getId();
                                treatment.pondId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_POND_ID);
                                treatment.campusId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CAMPUS_ID);
                                treatment.creatorId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_ID);
                                treatment.creatorName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_NAME);
                                treatment.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_IMAGE);
                                treatment.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_PHONE);
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER) != null){
                                    treatment.replaceWater = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD) != null){
                                    treatment.noFood = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD) != null){
                                    treatment.suckMud = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE) != null){
                                    treatment.note = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE);
                                }
                                treatment.date = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE);
                                treatment.sickName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SICK_NAME);
                                treatment.status = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS);
                                treatment.medicines = (HashMap<String, Object>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_MEDICINE);
                                treatments.add(treatment);
                                treatmentRequestAdapter.notifyDataSetChanged();
                            }
                        }

                        if (treatments.size() == 0){
                            treatmentRequestAdapter.notifyDataSetChanged();
                        }

                        showTreatmentMessage();

                        preferenceManager.remove(Constants.KEY_DAY_SELECTED);
                        preferenceManager.remove(Constants.KEY_MONTH_SELECTED);
                        preferenceManager.remove(Constants.KEY_YEAR_SELECTED);

                    }
                });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void getTreatmentRejectedForDirector(){
        treatments.clear();
        String dateSelected = yearSelected + "-" + monthSelected + "-" + daySelected;
        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_TREATMENT_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE), dateSelected)){
                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS), Constants.KEY_TREATMENT_REJECT)){
                                Treatment treatment = new Treatment();
                                treatment.id = queryDocumentSnapshot.getId();
                                treatment.pondId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_POND_ID);
                                treatment.campusId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CAMPUS_ID);
                                treatment.creatorId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_ID);
                                treatment.creatorName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_NAME);
                                treatment.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_IMAGE);
                                treatment.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_PHONE);
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER) != null){
                                    treatment.replaceWater = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD) != null){
                                    treatment.noFood = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD) != null){
                                    treatment.suckMud = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE) != null){
                                    treatment.note = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE);
                                }
                                treatment.date = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE);
                                treatment.sickName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SICK_NAME);
                                treatment.status = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS);
                                treatment.medicines = (HashMap<String, Object>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_MEDICINE);
                                treatments.add(treatment);
                                treatmentRequestAdapter.notifyDataSetChanged();
                            }
                        }

                        if (treatments.size() == 0){
                            treatmentRequestAdapter.notifyDataSetChanged();
                        }

                        showTreatmentMessage();

                        preferenceManager.remove(Constants.KEY_DAY_SELECTED);
                        preferenceManager.remove(Constants.KEY_MONTH_SELECTED);
                        preferenceManager.remove(Constants.KEY_YEAR_SELECTED);

                    }
                });
    }

    private void showToast(String message){
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void showTreatmentMessage(){
        if (treatments.size() == 0){
            mBinding.requestRecyclerView.setVisibility(View.GONE);
            mBinding.textMessage.setVisibility(View.VISIBLE);
        } else {
            mBinding.requestRecyclerView.setVisibility(View.VISIBLE);
            mBinding.textMessage.setVisibility(View.GONE);
        }
    }

    @Override
    public void onSelectWorker(Treatment treatment) {

    }
}