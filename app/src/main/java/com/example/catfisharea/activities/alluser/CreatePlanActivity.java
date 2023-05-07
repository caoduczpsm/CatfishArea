package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityCreatePlanActivityBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.SpinnerAdapter;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class CreatePlanActivity extends BaseActivity {
    private ActivityCreatePlanActivityBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Calendar myCal;
    private Campus mCampusSelected;
    private Pond mPondSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCreatePlanActivityBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    @SuppressLint("SetTextI18n")
    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);

        mBinding.toolbarCreatPlan.setNavigationOnClickListener(view -> onBackPressed());
        myCal = Calendar.getInstance();
        mBinding.edtDate.setText(myCal.get( Calendar.DAY_OF_MONTH) + "-" +
                (myCal.get(Calendar.MONTH) + 1) + "-" +
                myCal.get(Calendar.YEAR)
        );
        mBinding.edtDate.setOnClickListener(view -> openDatePicker());
        setCampusSpinner();
        setEditText();

        mBinding.savePlanBtn.setOnClickListener(view -> {
            try {
                savePlan();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void savePlan() throws ParseException {
        String acreage = Objects.requireNonNull(mBinding.edtAcreage.getText()).toString().trim();
        String consistence = Objects.requireNonNull(mBinding.edtConsistence.getText()).toString().trim();
        String fingerlingSamples = Objects.requireNonNull(mBinding.edtFingerlingSamples.getText()).toString().trim();
        String numberOfFish = Objects.requireNonNull(mBinding.edtNumberOfFish.getText()).toString().trim();
        String price = Objects.requireNonNull(mBinding.edtPrice.getText()).toString().trim();
        String total = Objects.requireNonNull(mBinding.edtTotal.getText()).toString().trim();
        Timestamp timestamp = new Timestamp(myCal.getTime());
        String preparationCost = Objects.requireNonNull(mBinding.edtExpense.getText()).toString().trim();


        if (mCampusSelected != null && mPondSelected != null && !acreage.isEmpty()
                && !consistence.isEmpty() && !fingerlingSamples.isEmpty() && !numberOfFish.isEmpty()
                && !price.isEmpty() && !total.isEmpty()) {
            Map<String, Object> data = new HashMap<>();
            data.put(Constants.KEY_CAMPUS_ID, mCampusSelected.getId());
            data.put(Constants.KEY_POND_ID, mPondSelected.getId());
            data.put(Constants.KEY_DATE_OF_PLAN, timestamp);
            data.put(Constants.KEY_ACREAGE, DecimalHelper.parseText(acreage));
            data.put(Constants.KEY_CONSISTENCE, Integer.parseInt(consistence));
            data.put(Constants.KEY_NUMBER_OF_FISH, DecimalHelper.parseText(numberOfFish));
            data.put(Constants.KEY_FINGERLING_SAMPLES, DecimalHelper.parseText(fingerlingSamples));
            data.put(Constants.KEY_PRICE, DecimalHelper.parseText(price)    );
            data.put(Constants.KEY_PREPARATION_COST, DecimalHelper.parseText(preparationCost));
            data.put(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID));

            database.collection(Constants.KEY_COLLECTION_PLAN).document().set(data).addOnSuccessListener(command -> {
                mBinding.edtPrice.setText("");
                mBinding.edtTotal.setText("");
                mBinding.edtConsistence.setText("");
                mBinding.edtFingerlingSamples.setText("");
                mBinding.edtExpense.setText("");
                mBinding.edtNumberOfFish.setText("");
                Toast.makeText(this, "Tạo vụ nuôi thành công", Toast.LENGTH_SHORT).show();
            });

        } else {
            Toast.makeText(this, "Nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
        }
    }


    private void setEditText() {
        mBinding.edtConsistence.setOnFocusChangeListener((v, hasFocus) -> updateEditText());

        mBinding.edtAcreage.setOnFocusChangeListener((v, hasFocus) -> updateEditText());

        mBinding.edtFingerlingSamples.setOnFocusChangeListener((v, hasFocus) -> updateEditText());

        mBinding.edtNumberOfFish.setOnFocusChangeListener((v, hasFocus) -> updateEditText());

        mBinding.edtTotal.setOnFocusChangeListener((v, hasFocus) -> updateEditText());

        mBinding.edtPrice.setOnFocusChangeListener((v, hasFocus) -> updateEditText());
    }

    private void updateEditText() {
        if (!Objects.requireNonNull(mBinding.edtAcreage.getText()).toString().isEmpty() &&
                !Objects.requireNonNull(mBinding.edtConsistence.getText()).toString().isEmpty()) {
            try {
                int acreage = DecimalHelper.parseText(mBinding.edtAcreage.getText().toString()).intValue();
                int consistence = DecimalHelper.parseText(mBinding.edtConsistence.getText().toString()).intValue();
//                int acreage = Integer.parseInt((mBinding.edtAcreage.getText().toString()));
//                int consistence = Integer.parseInt((mBinding.edtConsistence.getText().toString()));
                long numberOfFish = (long) acreage * consistence;
                mBinding.edtNumberOfFish.setText(DecimalHelper.formatText(numberOfFish));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!Objects.requireNonNull(mBinding.edtPrice.getText()).toString().isEmpty() &&
                !Objects.requireNonNull(mBinding.edtNumberOfFish.getText()).toString().isEmpty()
        && !Objects.requireNonNull(mBinding.edtConsistence.getText().toString().isEmpty())) {
            try {
                int numberOfFish = DecimalHelper.parseText(mBinding.edtNumberOfFish.getText().toString()).intValue();
                double price = DecimalHelper.parseText(mBinding.edtPrice.getText().toString()).doubleValue();
                int consistence = DecimalHelper.parseText(mBinding.edtConsistence.getText().toString()).intValue();
                int total = (int) (numberOfFish/consistence * price);

                mBinding.edtTotal.setText(DecimalHelper.formatText(total));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    private void openDatePicker() {
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH, month);
            myCal.set(Calendar.DAY_OF_MONTH, day);

            mBinding.edtDate.setText(day + "-" + (month + 1) + "-" + year);
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

    private void setCampusSpinner() {
        ArrayList<RegionModel> mCampus = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, R.layout.layout_spinner_item, mCampus);
        mBinding.campusName.setAdapter(spinnerAdapter);

        database.collection(Constants.KEY_COLLECTION_CAMPUS)
                .whereEqualTo(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID))
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String id = doc.getId();
                        String areaid = doc.getString(Constants.KEY_AREA_ID);
                        String name = doc.getString(Constants.KEY_NAME);

                        Campus campus = new Campus(id, name, null, areaid);

                        mCampus.add(campus);

                    }
                    spinnerAdapter.notifyDataSetChanged();
                });

        mBinding.campusName.setOnItemClickListener((parent, view, position, id) -> {
            Campus campus = (Campus) parent.getItemAtPosition(position);
            mCampusSelected = campus;
            setPondSpinner(campus.getId());

        });
    }

    private void setPondSpinner(String campusId) {
        ArrayList<RegionModel> mPonds = new ArrayList<>();
        SpinnerAdapter spinnerAdapter = new SpinnerAdapter(this, R.layout.layout_spinner_item, mPonds);
        mBinding.pondName.setAdapter(spinnerAdapter);

        database.collection(Constants.KEY_COLLECTION_POND)
                .whereEqualTo(Constants.KEY_CAMPUS_ID, campusId)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot doc : queryDocumentSnapshots.getDocuments()) {
                        String id = doc.getId();
                        String name = doc.getString(Constants.KEY_NAME);
                        String acrege = doc.getString(Constants.KEY_ACREAGE);

                        Pond pond = new Pond(id, name);
                        pond.setId(id);
                        pond.setName(name);
                        pond.setAcreage(acrege);

                        database.collection(Constants.KEY_COLLECTION_PLAN)
                                .whereEqualTo(Constants.KEY_POND_ID, id)
                                .get().addOnSuccessListener(query -> {
                                    if (query.isEmpty() || query.getDocuments().isEmpty()) {
                                        mPonds.add(pond);
                                        spinnerAdapter.notifyDataSetChanged();
                                    }
                                });
                    }

                });

        mBinding.pondName.setOnItemClickListener((parent, view, position, id) -> {
            Pond pond = (Pond) parent.getItemAtPosition(position);
            mBinding.textInputAcreage.setVisibility(View.VISIBLE);
            mPondSelected = pond;
            try {
                mBinding.edtAcreage.setText(DecimalHelper.formatText(Integer.parseInt(pond.getAcreage())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

}