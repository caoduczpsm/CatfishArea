package com.example.catfisharea.activities.director;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityRequestManagementBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.RequestAdapter;
import com.example.catfisharea.listeners.RequestListener;
import com.example.catfisharea.models.ImportRequest;
import com.example.catfisharea.models.Materials;
import com.example.catfisharea.models.Request;
import com.example.catfisharea.models.RequestLeave;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.github.jhonnyx2012.horizontalpicker.DatePickerListener;
import com.github.jhonnyx2012.horizontalpicker.HorizontalPicker;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RequestManagementActivity extends BaseActivity implements DatePickerListener, RequestListener {
    private ActivityRequestManagementBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private RequestAdapter adapter;
    private List<Object> mRequests;
    private boolean isFABOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRequestManagementBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();

    }

    @SuppressLint("NewApi")
    private void init() {

        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        setDataRecycler();
        setDatePicker();
        String typeAccount = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
        if (typeAccount.equals(Constants.KEY_REGIONAL_CHIEF)) {
            mBinding.fabnewRequest.setVisibility(View.GONE);
            mBinding.fabnewLeaveRequest.setVisibility(View.GONE);
            mBinding.fabnewImportRequest.setVisibility(View.GONE);
        } else if (typeAccount.equals(Constants.KEY_WORKER)){
            mBinding.fabnewImportRequest.setVisibility(View.GONE);
        }

        mBinding.monthHistory.setOnClickListener(v ->
                openDatePicker()
        );
        mBinding.yearHistory.setOnClickListener(view -> {
            openDatePicker();
        });
        mBinding.toolbarReqest.setNavigationOnClickListener(view -> {
            onBackPressed();
        });

        mBinding.fabnewRequest.setOnClickListener(view -> {
            if (!isFABOpen) {
                showFABMenu();
            } else {
                closeFABMenu();
            }
        });

        mBinding.fabnewLeaveRequest.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RequestLeaveActivity.class));
        });

        mBinding.fabnewImportRequest.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), RequestImportActivity.class));
        });
    }

    private void setDataRecycler() {
        mRequests = new ArrayList<>();
        adapter = new RequestAdapter(this, mRequests, this);
        mBinding.recyclerViewRequest.setAdapter(adapter);
    }

    private void getDataInDate(String date) {
        mRequests.clear();
        database.collection(Constants.KEY_COLLECTION_REQUEST)
                .whereEqualTo(Constants.KEY_REQUESTER, preferenceManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_DATE_CREATED_REQUEST, date)
                .addSnapshotListener(eventListener);
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_WORKER)) {

        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
            database.collection(Constants.KEY_COLLECTION_REQUEST)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                    .whereEqualTo(Constants.KEY_DATE_CREATED_REQUEST, date)
                    .addSnapshotListener(eventListener);
        } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
            database.collection(Constants.KEY_COLLECTION_REQUEST)
                    .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                    .whereEqualTo(Constants.KEY_DATE_CREATED_REQUEST, date)
                    .addSnapshotListener(eventListener);
        }
    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String id = documentChange.getDocument().getId();
                    String type = documentChange.getDocument().getString(Constants.KEY_TYPE_REQUEST);
                    String name = documentChange.getDocument().getString(Constants.KEY_NAME);
                    String dateCreated = documentChange.getDocument().getString(Constants.KEY_DATE_CREATED_REQUEST);
                    String status = documentChange.getDocument().getString(Constants.KEY_STATUS_TASK);
                    String userId = documentChange.getDocument().getString(Constants.KEY_REQUESTER);

                    database.collection(Constants.KEY_COLLECTION_USER).document(userId)
                            .get().addOnSuccessListener(documentSnapshot -> {
                                String nameUser = documentSnapshot.getString(Constants.KEY_NAME);
                                String image = documentSnapshot.getString(Constants.KEY_IMAGE);
                                String position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                                User user = new User();
                                user.name = nameUser;
                                user.id = userId;
                                user.image = image;
                                user.position = position;
                                if (type.equals(Constants.KEY_LEAVE_REQUEST)) {
                                    String note = documentChange.getDocument().getString(Constants.KEY_NOTE_REQUESET);
                                    String reason = documentChange.getDocument().getString(Constants.KEY_REASON_REQUESET);
                                    String dateStart = documentChange.getDocument().getString(Constants.KEY_DATESTART_REQUESET);
                                    String dateEnd = documentChange.getDocument().getString(Constants.KEY_DATEEND_REQUESET);
                                    RequestLeave requestLeave = new RequestLeave(id, name, note, user, dateCreated, type, dateStart, dateEnd, reason);
                                    requestLeave.setStatus(status);
                                    if (status.equals(Constants.KEY_PENDING)) {
                                        mRequests.add(0, requestLeave);
                                    } else
                                        mRequests.add(requestLeave);
                                    adapter.notifyDataSetChanged();
                                } else {
                                    Map<String, ArrayList<String>> materialsList = new HashMap<>();
                                    materialsList = (Map<String, ArrayList<String>>) documentChange.getDocument().get(Constants.KEY_MATERIALSLIST);
                                    ArrayList<Materials> materials = new ArrayList<>();
                                    for (String key : materialsList.keySet()) {
                                        int amount = Integer.parseInt(materialsList.get(key).get(0).toString());
                                        String decription = materialsList.get(key).get(1).toString();
                                        Materials mtr = new Materials(key, amount);
                                        mtr.setDecription(decription);
                                        materials.add(mtr);
                                    }
                                    ImportRequest request = new ImportRequest(id, name, "", user, dateCreated, type, materials);
                                    request.setStatus(status);
                                    if (status.equals(Constants.KEY_PENDING)) {
                                        mRequests.add(0, request);
                                    } else
                                        mRequests.add(request);
                                    adapter.notifyDataSetChanged();
                                }

                            });

                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (Object obj : mRequests) {
                        if (obj instanceof RequestLeave) {
                            if (documentChange.getDocument().getId().equals(((RequestLeave) obj).getId())) {
                                ((RequestLeave) obj).setStatus(documentChange.getDocument().getString(Constants.KEY_STATUS_TASK));
                            }
                        } else if (obj instanceof ImportRequest) {
                            if (documentChange.getDocument().getId().equals(((ImportRequest) obj).getId())) {
                                ((ImportRequest) obj).setStatus(documentChange.getDocument().getString(Constants.KEY_STATUS_TASK));
                            }
                        }
                    }

                }
            }
            adapter.notifyDataSetChanged();
            if (mRequests.isEmpty()) {
                mBinding.noData.setVisibility(View.VISIBLE);
            } else {
                mBinding.noData.setVisibility(View.GONE);
            }
        }
    };

    private void openDatePicker() {
        Calendar myCal = Calendar.getInstance();

        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH, month);
            myCal.set(Calendar.DAY_OF_MONTH, day);
            DateTime date = new DateTime(year, month + 1, day, myCal.get(Calendar.HOUR_OF_DAY), myCal.get(Calendar.MINUTE));
            date.withDate(year, month + 1, day);
            mBinding.datePicker.setDate(date);
        };

        DatePickerDialog dialog = new DatePickerDialog(
                this, dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );
        //String[] weekdays = new DateFormatSymbols().getWeekdays();
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void setDatePicker() {
        HorizontalPicker picker = mBinding.datePicker;

        picker
                .setListener(this)
                .setDateSelectedColor(Color.parseColor("#eea39d"))
                .setDateSelectedTextColor(Color.WHITE)
                .setMonthAndYearTextColor(Color.TRANSPARENT)
                .setTodayDateBackgroundColor(Color.GRAY)
                .setUnselectedDayTextColor(Color.DKGRAY)
                .setDayOfWeekTextColor(Color.DKGRAY)
                .setTodayButtonTextColor(Color.BLACK)
                .showTodayButton(true)
                .init();
        // or on the View directly after init was completed
        picker.setBackgroundColor(getColor(R.color.trans));
        picker.setDate(new DateTime());
    }

    private void showFABMenu() {
        isFABOpen = true;
        mBinding.fabnewLeaveRequest.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mBinding.fabnewImportRequest.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        mBinding.textLeave.setVisibility(View.VISIBLE);
        if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
            mBinding.textImport.setVisibility(View.VISIBLE);
        }

        mBinding.textLeave.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        mBinding.textImport.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu() {
        isFABOpen = false;
        mBinding.fabnewImportRequest.animate().translationY(0);
        mBinding.fabnewLeaveRequest.animate().translationY(0);
        mBinding.textLeave.animate().translationY(0);
        mBinding.textImport.animate().translationY(0);
        mBinding.textImport.setVisibility(View.GONE);
        mBinding.textLeave.setVisibility(View.GONE);
    }

    @Override
    public void onDateSelected(@NonNull final DateTime dateSelected) {
        // log it for demo
        String month = dateSelected.getMonthOfYear() + "";
        String year = dateSelected.getYear() + "";
        String day = dateSelected.getDayOfMonth() + "";
        mBinding.monthHistory.setText("Tháng " + month);
        mBinding.yearHistory.setText(year);
        String date = day + "/" + month + "/" + year;
        getDataInDate(date);
    }

    @Override
    public void accept(Request request) {
        database.collection(Constants.KEY_COLLECTION_REQUEST).document(request.getId())
                .update(Constants.KEY_STATUS_TASK, Constants.KEY_ACCEPT);
    }

    @Override
    public void refush(Request request) {
        database.collection(Constants.KEY_COLLECTION_REQUEST).document(request.getId())
                .update(Constants.KEY_STATUS_TASK, Constants.KEY_REFUSE);
    }

    @Override
    public void delete(Request request) {

    }
}