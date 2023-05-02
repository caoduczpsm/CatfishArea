package com.example.catfisharea.activities.regional_chief;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityHarvestFishBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.DecimalHelper;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HarvestFishActivity extends BaseActivity {
    private ActivityHarvestFishBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private Pond pond;
    private Calendar myCal;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityHarvestFishBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        setListener();
    }

    private void setListener() {
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        myCal = Calendar.getInstance();
        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);
        mBinding.toolbarHarvest.setNavigationOnClickListener(view -> onBackPressed());
        mBinding.radioInstruct.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    getDataPond();
                } else {
                    mBinding.edtBQFish.setText("");
                    mBinding.edtNumOfFish.setText("");
                    mBinding.edtQuantity.setText("");
                }
            }
        });

        mBinding.edtDate.setText(myCal.get(Calendar.DAY_OF_MONTH) + "-" +
                (myCal.get(Calendar.MONTH) + 1) + "-" +
                myCal.get(Calendar.YEAR)
        );

        mBinding.edtDate.setOnClickListener(view -> {
            openDatePicker();
        });

        setUpEditText();

        mBinding.saveHarvest.setOnClickListener(view -> {
            try {
                saveHarvest();
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void saveHarvest() throws ParseException {
        String bq = mBinding.edtBQFish.getText().toString();
        String quantity = mBinding.edtQuantity.getText().toString();
        String price = mBinding.edtPrice.getText().toString();
        String number = mBinding.edtNumOfFish.getText().toString();
        String total = mBinding.edtTotal.getText().toString();

        if (bq.isEmpty() && quantity.isEmpty() && quantity.isEmpty() && price.isEmpty() && number.isEmpty() && total.isEmpty()) {
            Toast.makeText(this, "Hãy nhập đủ thông tin", Toast.LENGTH_SHORT).show();
        } else {

            Map<String, Object> data = new HashMap<>();
            data.put(Constants.KEY_FISH_WEIGH_DATE, myCal.getTime());
            data.put(Constants.KEY_FISH_WEIGH_WEIGHT, bq);
            data.put(Constants.KEY_QUANTITY, quantity);
            data.put(Constants.KEY_PRICE, price);
            data.put(Constants.KEY_NUMBER_OF_FISH, DecimalHelper.parseText(number).toString());
            data.put(Constants.KEY_TOTAL_MONEY, DecimalHelper.parseText(total).toString());

            database.collection(Constants.KEY_COLLECTION_PLAN)
                    .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                    .get().addOnSuccessListener(planQuery -> {
                        database.collection(Constants.KEY_COLLECTION_PLAN).document(planQuery.getDocuments().get(0).getId())
                                .update(Constants.KEY_DATE_HARVEST, myCal.getTime())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        database.collection(Constants.KEY_COLLECTION_PLAN).document(planQuery.getDocuments().get(0).getId())
                                                .collection(Constants.KEY_COLLECTION_HARVEST)
                                                .document().set(data).addOnSuccessListener(harvestDoc -> {
//                                                    copyDocument(planQuery.getDocuments().get(0).getData(),
//                                                            planQuery);
                                                });
                                    }
                                });


                    });
        }

    }

    private void copyDocument(Map<String, Object> data, QuerySnapshot planQuery) {

        database.collection(Constants.KEY_COLLECTION_DIARY)
                .document().set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {

                        database.collection(Constants.KEY_COLLECTION_PLAN)
                                .document(planQuery.getDocuments().get(0).getId())
                                .delete();
                        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                .whereEqualTo(Constants.KEY_ID_PLAN, planQuery.getDocuments().get(0).getId())
                                .get().addOnSuccessListener(taskQuery -> {
                                    for (DocumentSnapshot documentSnapshot : taskQuery.getDocuments()) {
                                        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                                                .document(documentSnapshot.getId()).delete();
                                    }
                                });
                    }
                });
    }

    private void setUpEditText() {
        mBinding.edtPrice.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                try {
                    updateData();
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void updateData() throws ParseException {
        String bq = mBinding.edtBQFish.getText().toString();
        String quantity = mBinding.edtQuantity.getText().toString();
        String price = mBinding.edtPrice.getText().toString();
        String number = DecimalHelper.parseText(mBinding.edtNumOfFish.getText().toString()).toString();
        String total = DecimalHelper.parseText(mBinding.edtTotal.getText().toString()).toString();

        if (!price.isEmpty() && !quantity.isEmpty()) {
            total = String.valueOf(new BigDecimal((Double.parseDouble(quantity) * 1000000 * Double.parseDouble(price))));
        }

        if (!bq.isEmpty() && !quantity.isEmpty()) {
            number = String.valueOf(Double.parseDouble(quantity) * 1000000 / Long.parseLong(bq));
        }

        if (!number.isEmpty() && !bq.isEmpty()) {
            quantity = String.valueOf(((double) (Double.parseDouble(number) * Double.parseDouble(bq) / 1000000.000)));
        }

        mBinding.edtQuantity.setText(quantity);
        mBinding.edtNumOfFish.setText(DecimalHelper.formatText(Double.parseDouble(number)));
        mBinding.edtTotal.setText(DecimalHelper.formatText(Double.parseDouble(total)));
    }

    private void getDataPond() {
        try {
            database.collection(Constants.KEY_COLLECTION_PLAN)
                    .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                    .get().addOnSuccessListener(planQuery -> {
                        for (DocumentSnapshot planDoc : planQuery.getDocuments()) {
                            mBinding.edtNumOfFish.setText(planDoc.get(Constants.KEY_NUMBER_OF_FISH).toString());
                            database.collection(Constants.KEY_COLLECTION_PLAN).document(planDoc.getId())
                                    .collection(Constants.KEY_COLLECTION_FISH_WEIGH)
                                    .orderBy(Constants.KEY_FISH_WEIGH_WEIGHT, Query.Direction.DESCENDING)
                                    .get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @SuppressLint("SetTextI18n")
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            if (!queryDocumentSnapshots.isEmpty()) {
                                                DocumentSnapshot document = queryDocumentSnapshots.getDocuments().get(0);
                                                String weigh = document.getString(Constants.KEY_FISH_WEIGH_WEIGHT);
                                                mBinding.edtBQFish.setText(weigh);
                                                long totalLoss = 0;
                                                for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots.getDocuments()) {
                                                    totalLoss += Long.parseLong(documentSnapshot.getString(Constants.KEY_FISH_WEIGH_LOSS));
                                                }
                                                if (!mBinding.edtNumOfFish.getText().toString().isEmpty()) {
                                                    String num = mBinding.edtNumOfFish.getText().toString();

                                                        mBinding.edtNumOfFish.setText(
                                                                (DecimalHelper.parseText(num).longValue() - totalLoss) + ""
                                                        );

                                                }

                                            }
                                        }
                                    });
                            database.collection(Constants.KEY_COLLECTION_RELEASE_FISH)
                                    .whereEqualTo(Constants.KEY_RELEASE_FISH_PLAN_ID, planDoc.getId())
                                    .get().addOnSuccessListener(releaseQuery -> {
                                        for (DocumentSnapshot releaseDoc : releaseQuery.getDocuments()) {
                                            String amount = releaseDoc.getString(Constants.KEY_AMOUNT);
                                            String num = mBinding.edtNumOfFish.getText().toString();
                                            try {
                                                mBinding.edtNumOfFish.setText(
                                                        (DecimalHelper.parseText(num).longValue() + +Long.parseLong(amount)) + ""
                                                );

                                                updateData();
                                            } catch (Exception exception) {

                                            }


                                        }
                                    });

                        }
                    });
        } catch (Exception e) {
            e.printStackTrace();
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
}