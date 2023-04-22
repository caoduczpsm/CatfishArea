package com.example.catfisharea.fragments.alluser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentTreatmentAcceptedBinding;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.TreatmentRequestAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.listeners.TreatmentListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.Treatment;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CompletedTreatmentFragment extends Fragment implements TreatmentListener, MultipleListener {

    private FragmentTreatmentAcceptedBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private TreatmentRequestAdapter treatmentRequestAdapter;
    private List<Treatment> treatments;
    String daySelected, monthSelected, yearSelected;

    public CompletedTreatmentFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentTreatmentAcceptedBinding.inflate(inflater, container, false);
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

        //ListƯ
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
            getTreatmentAcceptedForRegional();
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
            getTreatmentAcceptedForDirector();
        }

    }

    @SuppressLint("NotifyDataSetChanged")
    private void getTreatmentAcceptedForRegional(){
        treatments.clear();
        String dateSelected = yearSelected + "-" + monthSelected + "-" + daySelected;
        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE), dateSelected)){
                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS), Constants.KEY_TREATMENT_ACCEPT)){
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
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_RECEIVER_ID) != null){
                                    treatment.receiverIds = (List<String>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_RECEIVER_ID);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_RECEIVER_IMAGE) != null){
                                    treatment.receiverImages = (List<String>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_RECEIVER_IMAGE);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_RECEIVER_NAME) != null){
                                    treatment.receiverNames = (List<String>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_RECEIVER_NAME);
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_RECEIVER_PHONE) != null){
                                    treatment.receiverPhones = (List<String>) queryDocumentSnapshot.get(Constants.KEY_TREATMENT_RECEIVER_PHONE);
                                }
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
    private void getTreatmentAcceptedForDirector(){
        treatments.clear();
        String dateSelected = yearSelected + "-" + monthSelected + "-" + daySelected;
        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                .whereEqualTo(Constants.KEY_TREATMENT_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                .get()
                .addOnCompleteListener(task -> {

                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE), dateSelected)){
                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS), Constants.KEY_TREATMENT_ACCEPT)){
                                Treatment treatment = new Treatment();
                                treatment.id = queryDocumentSnapshot.getId();
                                treatment.pondId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_POND_ID);
                                treatment.campusId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CAMPUS_ID);
                                treatment.creatorId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_ID);
                                treatment.creatorName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_NAME);
                                treatment.creatorImage = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_IMAGE);
                                treatment.creatorPhone = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_CREATOR_PHONE);
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER) != null){
                                    if (!Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER), "")){
                                        treatment.replaceWater = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPLACE_WATER);
                                    } else {
                                        treatment.replaceWater = "";
                                    }
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD) != null){
                                    if (!Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD), "")){
                                        treatment.noFood = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NO_FOOD);
                                    } else {
                                        treatment.noFood = "";
                                    }
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD) != null){
                                    if (!Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD), "")){
                                        treatment.suckMud = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SUCK_MUD);
                                    } else {
                                        treatment.suckMud = "";
                                    }
                                }
                                if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE) != null){
                                    treatment.note = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_NOTE);
                                }
                                treatment.date = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_DATE);
                                treatment.sickName = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_SICK_NAME);
                                treatment.status = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_STATUS);
                                treatment.reportFishId = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_REPORT_FISH_ID);
                                treatment.assignmentStatus = queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_ASSIGNMENT_STATUS);
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
    private void openSelectWorkerDialog(Treatment treatment){
        Dialog dialog = openDialog(R.layout.layout_dialog_select_worker_for_treatment);
        assert dialog != null;

        Button btnClose, btnSelect;
        RecyclerView userRecyclerView;

        btnClose = dialog.findViewById(R.id.btnClose);
        btnSelect = dialog.findViewById(R.id.btnSelect);
        userRecyclerView = dialog.findViewById(R.id.userRecyclerView);

        List<User> users = new ArrayList<>();
        MultipleUserSelectionAdapter adapter = new MultipleUserSelectionAdapter(users, this);
        userRecyclerView.setAdapter(adapter);

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_POND_ID, treatment.pondId)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        User user = new User();
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                        user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                        user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        user.id = queryDocumentSnapshot.getId();
                        if (queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_ASSIGNMENT) != null){
                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_TREATMENT_ASSIGNMENT), Constants.KEY_TREATMENT_IS_ASSIGNMENT))
                                user.isSelected = true;
                        }
                        users.add(user);
                        adapter.notifyDataSetChanged();

                    }
                });

        btnSelect.setOnClickListener(view -> {
            List<User> selectedUser = adapter.getSelectedUser();
            if (selectedUser.size() == 0) {
                showToast("Vui lòng chọn ít nhất một công nhân!");
            } else {
                List<String> receiverIds = new ArrayList<>();
                List<String> receiverNames = new ArrayList<>();
                List<String> receiverImages = new ArrayList<>();
                List<String> receiverPhones = new ArrayList<>();

                // Duyệt qua các trưởng vùng mà người dùng chọn để lấy tên và id
                for (User user : selectedUser) {
                    receiverIds.add(user.id);
                    receiverNames.add(user.name);
                    receiverImages.add(user.image);
                    receiverPhones.add(user.phone);
                }

                HashMap<String, Object> treatmentUpdate = new HashMap<>();
                treatmentUpdate.put(Constants.KEY_TREATMENT_RECEIVER_ID, receiverIds);
                treatmentUpdate.put(Constants.KEY_TREATMENT_RECEIVER_NAME, receiverNames);
                treatmentUpdate.put(Constants.KEY_TREATMENT_RECEIVER_IMAGE, receiverImages);
                treatmentUpdate.put(Constants.KEY_TREATMENT_RECEIVER_PHONE, receiverPhones);

                database.collection(Constants.KEY_COLLECTION_TREATMENT)
                        .document(treatment.id)
                        .update(treatmentUpdate)
                        .addOnSuccessListener(runnable -> {
                            dialog.dismiss();
                            showToast("Chuyển phát đồ cho công nhân phụ trách thành công!");

                            HashMap<String, Object> assignmentUser = new HashMap<>();
                            assignmentUser.put(Constants.KEY_TREATMENT_ASSIGNMENT, Constants.KEY_TREATMENT_IS_ASSIGNMENT);
                            assignmentUser.put(Constants.KEY_TREATMENT_ID, treatment.id);
                            for (String id : receiverIds) {
                                database.collection(Constants.KEY_COLLECTION_USER)
                                        .document(id)
                                        .update(assignmentUser);
                            }

                        })
                        .addOnFailureListener(runnable -> showToast("Chuyển phát đồ cho công nhân phụ trách thất bại!"));


            }
        });

        btnClose.setOnClickListener(view -> dialog.dismiss());

        dialog.show();
    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(requireContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(layout);
        dialog.setCancelable(true);
        Window window = dialog.getWindow();
        if (window == null) {
            return null;
        }
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes();
        windowAttributes.gravity = Gravity.CENTER;
        window.setAttributes(windowAttributes);

        return dialog;
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
        openSelectWorkerDialog(treatment);
    }

    @Override
    public void onMultipleUserSelection(Boolean isSelected) {

    }

    @Override
    public void onChangeTeamLeadClicker(User user) {

    }

    @Override
    public void onTaskClicker(Task task) {

    }

    @Override
    public void onTaskSelectedClicker(Boolean isSelected, Boolean isMultipleSelection) {

    }
}