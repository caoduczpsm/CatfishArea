package com.example.catfisharea.activities.director;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.EditText;
import android.widget.Toast;
import com.android.app.catfisharea.databinding.ActivityRequestLeaveBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class RequestLeaveActivity extends BaseActivity {
    private ActivityRequestLeaveBinding mBinding;
    private Calendar myCal;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private String encodedImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityRequestLeaveBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        myCal = Calendar.getInstance();
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(this);
        Animatoo.animateSlideLeft(RequestLeaveActivity.this);
        setListener();
    }

    private void setListener() {
        mBinding.toolbarRequestLeave.setNavigationOnClickListener(view -> onBackPressed());

        mBinding.edtDateStart.setOnClickListener(view -> openDatePicker(mBinding.edtDateStart));

        mBinding.edtDateEnd.setOnClickListener(view -> openDatePicker(mBinding.edtDateEnd));

        mBinding.imageReason.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Animatoo.animateSlideLeft(this);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        mBinding.sendReportBtn.setOnClickListener(view -> sendLeaveRequest());


    }

    private void sendLeaveRequest() {

        String dateStart = Objects.requireNonNull(mBinding.edtDateStart.getText()).toString();
        String dateEnd = Objects.requireNonNull(mBinding.edtDateEnd.getText()).toString();
        String reason = Objects.requireNonNull(mBinding.edtReason.getText()).toString().trim();
        String note = Objects.requireNonNull(mBinding.edtNote.getText()).toString().trim();
        boolean done = !dateStart.isEmpty();
        if (dateEnd.isEmpty()) {
            done = false;
            Toast.makeText(this, "Chọn ngày kết thúc", Toast.LENGTH_SHORT).show();
        } else if (reason.isEmpty()) {
            done = false;
            Toast.makeText(this, "Nhập lý do", Toast.LENGTH_SHORT).show();
        } else if (dateStart.isEmpty()) {
            done = false;
            Toast.makeText(this, "Chọn ngày hoàn thành", Toast.LENGTH_SHORT).show();
        }
        if (done) {
            DateTime dateTime = DateTime.now();
            String dateCreated = dateTime.getDayOfMonth() + "/" + dateTime.getMonthOfYear() + "/" + dateTime.getYear();
            HashMap<String, Object> data = new HashMap<>();
            data.put(Constants.KEY_NAME, "Yêu cầu nghĩ phép");
            data.put(Constants.KEY_TYPE_REQUEST, Constants.KEY_LEAVE_REQUEST);
            data.put(Constants.KEY_DATE_CREATED_REQUEST, dateCreated);
            data.put(Constants.KEY_DATESTART_REQUESET, dateStart);
            data.put(Constants.KEY_DATEEND_REQUESET, dateEnd);
            data.put(Constants.KEY_REQUESTER, preferenceManager.getString(Constants.KEY_USER_ID));
            data.put(Constants.KEY_STATUS_TASK, Constants.KEY_PENDING);
            data.put(Constants.KEY_REASON_REQUESET, reason);
            data.put(Constants.KEY_NOTE_REQUESET, note);
            if (encodedImage != null && encodedImage.isEmpty()) {
                data.put(Constants.KEY_IMAGE, encodedImage);
            }
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_WORKER)) {
                if (preferenceManager.getString(Constants.KEY_POND_ID) == null) return;
                database.collection(Constants.KEY_COLLECTION_POND).document(preferenceManager.getString(Constants.KEY_POND_ID))
                        .get().addOnSuccessListener(documentSnapshot -> {
                            String campusId = documentSnapshot.getString(Constants.KEY_CAMPUS_ID);
                            data.put(Constants.KEY_RECEIVER_ID, campusId);
                            database.collection(Constants.KEY_COLLECTION_REQUEST).document()
                                    .set(data).addOnSuccessListener(unused -> {
                                        Toast.makeText(RequestLeaveActivity.this, "Gửi thành công", Toast.LENGTH_SHORT).show();
                                        mBinding.edtDateStart.setText("");
                                        mBinding.edtDateEnd.setText("");
                                        mBinding.edtReason.setText("");
                                    });
                        });
            } else if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
                if (preferenceManager.getString(Constants.KEY_CAMPUS_ID) == null) return;
                database.collection(Constants.KEY_COLLECTION_CAMPUS).document(preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                        .get().addOnSuccessListener(documentSnapshot -> {
                            String areaId = documentSnapshot.getString(Constants.KEY_AREA_ID);
                            data.put(Constants.KEY_RECEIVER_ID, areaId);
                            database.collection(Constants.KEY_COLLECTION_REQUEST).document()
                                    .set(data).addOnSuccessListener(unused -> {
                                        Toast.makeText(RequestLeaveActivity.this, "Gửi thành công", Toast.LENGTH_SHORT).show();
                                        mBinding.edtDateStart.setText("");
                                        mBinding.edtDateEnd.setText("");
                                        mBinding.edtReason.setText("");
                                    });
                        });
            }
        }
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = this.getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mBinding.imageReason.setImageBitmap(bitmap);
                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void openDatePicker(EditText edit) {
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH, month);
            myCal.set(Calendar.DAY_OF_MONTH, day);
            edit.setText(day + "/" + (month + 1) + "/" + year);

        };

        DatePickerDialog dialog = new DatePickerDialog(
                this, dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.getDatePicker().setMinDate(Calendar.getInstance().getTimeInMillis());

        if (edit.equals(mBinding.edtDateEnd)) {
            if (!Objects.requireNonNull(mBinding.edtDateStart.getText()).toString().isEmpty()) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Calendar dateStart = Calendar.getInstance();
                    dateStart.setTime(Objects.requireNonNull(format.parse(mBinding.edtDateStart.getText().toString())));
                    dialog.getDatePicker().setMinDate(dateStart.getTimeInMillis());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        } else if (edit.equals(mBinding.edtDateStart)) {
            if (!Objects.requireNonNull(mBinding.edtDateEnd.getText()).toString().isEmpty()) {
                @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                try {
                    Calendar dateEnd = Calendar.getInstance();
                    dateEnd.setTime(Objects.requireNonNull(format.parse(mBinding.edtDateEnd.getText().toString())));
                    dialog.getDatePicker().setMaxDate(dateEnd.getTimeInMillis());
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        dialog.show();
    }

}