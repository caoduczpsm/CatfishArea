package com.example.catfisharea.activities.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityCreateSimpleAccountBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class CreateSimpleAccountActivity extends BaseActivity {
    private ActivityCreateSimpleAccountBinding mBinding;
    private Calendar myCal;
    private String encodedImage;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String typeAccount;
    private ArrayList<String> listTypeAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCreateSimpleAccountBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        init();
        setListeners();
    }

    private void init(){

        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        //FireStore
        database = FirebaseFirestore.getInstance();

        //Calendar
        myCal = Calendar.getInstance();

        //ArrayList
        listTypeAccount = new ArrayList<>();
        listTypeAccount.add("Admin");
        listTypeAccount.add("Kế Toán");
        listTypeAccount.add("Trưởng Vùng");
        listTypeAccount.add("Trưởng Khu");
        listTypeAccount.add("Công Nhân");

        //ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, listTypeAccount);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spinnerTypeAccount.setAdapter(adapter);
        typeAccount = listTypeAccount.get(0);

    }

    private void setListeners(){
        mBinding.toolbarSimpleAccount.setNavigationOnClickListener(view -> {
            onBackPressed();
        });
        // Chọn hình ảnh
        mBinding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            Animatoo.animateSlideLeft(CreateSimpleAccountActivity.this);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        // Chọn ngày tháng năm sinh
        mBinding.textDateOfBirth.setOnClickListener(view -> openDatePicker());

        mBinding.btnRegis.setOnClickListener(view -> {
            if (isValidSignUpDetails()){
                regisAccount();
            }

        });

        // Chọn loại tài khoản cần tạo
        mBinding.spinnerTypeAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                typeAccount = listTypeAccount.get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeAccount = listTypeAccount.get(0);
            }
        });

    }

    private void regisAccount() {

        // Tạo các trường dữ liệu cho tài khoản mới
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_PHONE, mBinding.edtPhone.getText().toString());
        user.put(Constants.KEY_PASSWORD, mBinding.edtPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,encodedImage);
        user.put(Constants.KEY_NAME, mBinding.edtName.getText().toString());
        user.put(Constants.KEY_PERSONAL_ID, mBinding.edtPersonalID.getText().toString());
        user.put(Constants.KEY_DATEOFBIRTH, mBinding.textDateOfBirth.getText().toString());
        user.put(Constants.KEY_ADDRESS, mBinding.edtAddress.getText().toString());
        user.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));

        if (typeAccount.equals(listTypeAccount.get(0))){
            user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ADMIN);
        } else if (typeAccount.equals(listTypeAccount.get(1))){
            user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ACCOUNTANT);
        } else if (typeAccount.equals(listTypeAccount.get(2))){
            user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF);
        } else if (typeAccount.equals(listTypeAccount.get(3))){
            user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR);
        } else {
            user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER);
        }

        // Giả lập trạng thái loading và ẩn nút đăng kí
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USER)
                .add(user)
                .addOnSuccessListener(task ->{
                    loading(false);
                    database.collection(Constants.KEY_COLLECTION_COMPANY)
                            .document(preferenceManager.getString(Constants.KEY_COMPANY_ID))
                            .get()
                            .addOnCompleteListener(task1 -> {

                                DocumentSnapshot documentSnapshot = task1.getResult();

                                // Lấy số tài khoản công ty hiện tại cộng thêm một tài khoản vừa tạo
                                int currentTotalAccount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_TOTAL_ACCOUNT))) + 1;

                                // Tạo các trưởng dữ liệu cần cập nhật
                                HashMap<String, Object> company = new HashMap<>();
                                company.put(Constants.KEY_COMPANY_TOTAL_ACCOUNT, currentTotalAccount + "");

                                int currentTypeAmount;
                                if (typeAccount.equals(listTypeAccount.get(0))){
                                    currentTypeAmount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_ADMIN))) + 1;
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, currentTypeAmount + "");
                                } else if (typeAccount.equals(listTypeAccount.get(1))){
                                    currentTypeAmount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT))) + 1;
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, currentTypeAmount + "");
                                } else if (typeAccount.equals(listTypeAccount.get(2))){
                                    currentTypeAmount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF))) + 1;
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, currentTypeAmount + "");
                                } else if (typeAccount.equals(listTypeAccount.get(3))){
                                    currentTypeAmount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_DIRECTOR))) + 1;
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, currentTypeAmount + "");
                                } else {
                                    currentTypeAmount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_WORKER))) + 1;
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, currentTypeAmount + "");
                                }

                                database.collection(Constants.KEY_COLLECTION_COMPANY)
                                        .document(preferenceManager.getString(Constants.KEY_COMPANY_ID))
                                        .update(company)
                                        .addOnSuccessListener(task2 -> {
                                            showToast("Tạo tài khoản thành công");
                                            mBinding.edtPhone.setText("");
                                            mBinding.edtPassword.setText("");
                                            mBinding.edtName.setText("");
                                            mBinding.edtPersonalID.setText("");
                                            mBinding.textDateOfBirth.setText("");
                                            mBinding.edtAddress.setText("");
                                        });


                            });

                }).addOnFailureListener(e -> showToast("Lỗi trong quá trench tạo tài khoản!"));


    }

    private void openDatePicker() {

        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH,month);
            myCal.set(Calendar.DAY_OF_MONTH,day);
            mBinding.textDateOfBirth.setText(myCal.get(Calendar.DAY_OF_MONTH) + "/"
                    + (myCal.get(Calendar.MONTH) + 1) + "/"
                    + myCal.get(Calendar.YEAR));
        };

        DatePickerDialog dialog = new DatePickerDialog(
                this, dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private String encodeImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
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
                            mBinding.imageProfile.setImageBitmap(bitmap);
                            mBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(encodedImage == null){
            showToast("Hãy chọn hình ảnh!");
            return false;
        } else if(mBinding.edtPhone.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập số điện thoại!");
            return false;
        } else if(mBinding.edtPassword.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập mật khẩu!");
            return false;
        } else if(mBinding.edtName.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập họ và tên!");
            return false;
        } else if(mBinding.edtPersonalID.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập CMND/CCCD");
            return false;
        } else if(mBinding.textDateOfBirth.getText().toString().trim().isEmpty()){
            showToast("Hãy chọn ngày tháng năm sinh!");
            return false;
        } else if(mBinding.edtAddress.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập địa chỉ!");
            return false;
        } else{
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading){
        if(isLoading){
            mBinding.btnRegis.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else{
            mBinding.btnRegis.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }
}