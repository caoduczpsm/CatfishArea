package com.example.catfisharea.activities.admin;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.RequiresApi;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Toast;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityCreateSimpleAccountBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.EncryptHandler;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void setListeners(){
        mBinding.toolbarSimpleAccount.setNavigationOnClickListener(view -> onBackPressed());
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
        mBinding.spinnerTypeAccount.setOnItemClickListener((adapterView, view, i, l) -> typeAccount = listTypeAccount.get(i));

    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void regisAccount() {

        try {
            String encryptPassword = EncryptHandler.encryptPassword(Objects.requireNonNull(mBinding.edtPassword.getText()).toString());

            // Tạo các trường dữ liệu cho tài khoản mới
            HashMap<String, Object> user = new HashMap<>();
            user.put(Constants.KEY_PHONE, Objects.requireNonNull(mBinding.edtPhone.getText()).toString());
            user.put(Constants.KEY_PASSWORD, encryptPassword);
            user.put(Constants.KEY_IMAGE,encodedImage);
            user.put(Constants.KEY_NAME, Objects.requireNonNull(mBinding.edtName.getText()).toString());
            user.put(Constants.KEY_PERSONAL_ID, Objects.requireNonNull(mBinding.edtPersonalID.getText()).toString());
            user.put(Constants.KEY_DATEOFBIRTH, Objects.requireNonNull(mBinding.textDateOfBirth.getText()).toString());
            user.put(Constants.KEY_ADDRESS, Objects.requireNonNull(mBinding.edtAddress.getText()).toString());
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

            database.collection(Constants.KEY_COLLECTION_USER)
                    .add(user)
                    .addOnSuccessListener(task ->{
                        showToast("Tạo tài khoản thành công");
                        mBinding.edtPhone.setText("");
                        mBinding.edtPassword.setText("");
                        mBinding.edtName.setText("");
                        mBinding.edtPersonalID.setText("");
                        mBinding.textDateOfBirth.setText("");
                        mBinding.edtAddress.setText("");
                    }).addOnFailureListener(e -> showToast("Lỗi trong quá trench tạo tài khoản!"));
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }


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
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
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
        } else if(Objects.requireNonNull(mBinding.edtPhone.getText()).toString().trim().isEmpty()){
            showToast("Hãy nhập số điện thoại!");
            return false;
        } else if(Objects.requireNonNull(mBinding.edtPassword.getText()).toString().trim().isEmpty()){
            showToast("Hãy nhập mật khẩu!");
            return false;
        } else if(Objects.requireNonNull(mBinding.edtName.getText()).toString().trim().isEmpty()){
            showToast("Hãy nhập họ và tên!");
            return false;
        } else if(Objects.requireNonNull(mBinding.edtPersonalID.getText()).toString().trim().isEmpty()){
            showToast("Hãy nhập CMND/CCCD");
            return false;
        } else if(Objects.requireNonNull(mBinding.textDateOfBirth.getText()).toString().trim().isEmpty()){
            showToast("Hãy chọn ngày tháng năm sinh!");
            return false;
        } else if(Objects.requireNonNull(mBinding.edtAddress.getText()).toString().trim().isEmpty()){
            showToast("Hãy nhập địa chỉ!");
            return false;
        } else{
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}