package com.example.catfisharea.fragments.alluser;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.R;
import com.example.catfisharea.activities.admin.AdminHomeActivity;
import com.example.catfisharea.databinding.FragmentCompanyRegisBinding;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;

public class CompanyRegisFragment extends Fragment {
    private FragmentCompanyRegisBinding mBinding;
    private FirebaseFirestore database;
    private String encodedPersonalImage, encodedCompanyImage;
    private String phone, password, name, personalID, dateOfBirth, address;
    private PreferenceManager preferenceManager;

    public CompanyRegisFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBinding = FragmentCompanyRegisBinding.inflate(inflater, container, false);

        //PreferenceManager
        preferenceManager = new PreferenceManager(getContext().getApplicationContext());

        //FireStore
        database = FirebaseFirestore.getInstance();

        setListeners();

        return mBinding.getRoot();
    }

    private void setListeners(){

        // Chọn hình ảnh
        mBinding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        mBinding.btnRegis.setOnClickListener(view -> {
            if (isValidSignUpDetails())
                regisAccount();
        });

    }

    private void regisAccount() {

        // Giả lập loading, hiển thị progress bar trong thời gian chờ đăng ký tài khoản
        loading(true);

        // Lấy các thông tin admin mà người dùng nhập từ tab trước
        phone = preferenceManager.getString(Constants.KEY_PHONE);
        password = preferenceManager.getString(Constants.KEY_PASSWORD);
        name = preferenceManager.getString(Constants.KEY_NAME);
        personalID = preferenceManager.getString(Constants.KEY_PERSONAL_ID);
        dateOfBirth = preferenceManager.getString(Constants.KEY_DATEOFBIRTH);
        address = preferenceManager.getString(Constants.KEY_ADDRESS);
        encodedPersonalImage = preferenceManager.getString(Constants.KEY_IMAGE);

        // Tạo các trường trong bảng users
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_PHONE, phone);
        user.put(Constants.KEY_PASSWORD, password);
        user.put(Constants.KEY_IMAGE,encodedPersonalImage);
        user.put(Constants.KEY_NAME, name);
        user.put(Constants.KEY_PERSONAL_ID, personalID);
        user.put(Constants.KEY_DATEOFBIRTH, dateOfBirth);
        user.put(Constants.KEY_ADDRESS,address);
        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ADMIN);

        // Tạo các trường dữ liệu trong bảng companies
        HashMap<String, Object> company = new HashMap<>();
        company.put(Constants.KEY_COMPANY_NAME, mBinding.edtCompanyName.getText().toString());
        company.put(Constants.KEY_COMPANY_ADDRESS, mBinding.edtCompanyAddress.getText().toString());
        company.put(Constants.KEY_COMPANY_IMAGE, encodedCompanyImage);
        company.put(Constants.KEY_COMPANY_PHONE, mBinding.edtCompanyPhone.getText().toString());
        company.put(Constants.KEY_COMPANY_CODE, mBinding.edtCompanyCode.getText().toString());
        company.put(Constants.KEY_COMPANY_TOTAL_ACCOUNT, "1");
        company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, "1");
        company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, "0");
        company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, "0");
        company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, "0");
        company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, "0");

        // Đẩy thông tin công ty lên Firebase

        database.collection(Constants.KEY_COLLECTION_COMPANY)
                .add(company)
                .addOnSuccessListener(documentReference -> {

                    // Lưu thông tin công ty vào bộ nhớ tạm
                    preferenceManager.putString(Constants.KEY_COMPANY_NAME, mBinding.edtCompanyName.getText().toString());
                    preferenceManager.putString(Constants.KEY_COMPANY_ADDRESS, mBinding.edtCompanyAddress.getText().toString());
                    preferenceManager.putString(Constants.KEY_COMPANY_IMAGE, encodedCompanyImage);
                    preferenceManager.putString(Constants.KEY_COMPANY_PHONE, mBinding.edtCompanyPhone.getText().toString());
                    preferenceManager.putString(Constants.KEY_COMPANY_CODE, mBinding.edtCompanyCode.getText().toString());
                    preferenceManager.putString(Constants.KEY_COMPANY_ID, documentReference.getId());

                    // Lưu id công ty vào bảng của user
                    user.put(Constants.KEY_COMPANY_ID, documentReference.getId());

                    // Đẩy dữ liệu lên Firebase
                    database.collection(Constants.KEY_COLLECTION_USER)
                            .add(user)
                            .addOnSuccessListener(documentReference1 -> {
                                // Ẩn progress bar khi xử lý xong
                                loading(false);

                                // Lưu dữ liệu vào bộ nhớ tạm
                                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                                preferenceManager.putString(Constants.KEY_USER_ID, documentReference1.getId());
                                preferenceManager.putString(Constants.KEY_PHONE, phone);
                                preferenceManager.putString(Constants.KEY_PASSWORD, password);
                                preferenceManager.putString(Constants.KEY_IMAGE, encodedPersonalImage);
                                preferenceManager.putString(Constants.KEY_NAME, name);
                                preferenceManager.putString(Constants.KEY_PERSONAL_ID, personalID);
                                preferenceManager.putString(Constants.KEY_DATEOFBIRTH, dateOfBirth);
                                preferenceManager.putString(Constants.KEY_ADDRESS, address);

                                // Chuyển màn hình sang màn hình chính của Admin
                                Intent intent = new Intent(getContext(), AdminHomeActivity.class);
                                Animatoo.animateSlideLeft(getContext());
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);

                            })
                            .addOnFailureListener(exception ->{
                                // Ẩn progress bar khi xử lý lỗi
                                loading(false);
                                // Hiển thị lỗi nếu thất bại
                                showToast(exception.getMessage());
                            });
                });

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

    // Lấy ảnh từ người dùng
    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getActivity().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mBinding.imageProfile.setImageBitmap(bitmap);
                            mBinding.textAddImage.setVisibility(View.GONE);
                            encodedCompanyImage = encodeImage(bitmap);
                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails(){
        if(encodedCompanyImage == null){
            showToast("Hãy chọn hình ảnh!");
            return false;
        } else if(mBinding.edtCompanyName.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập họ và tên!");
            return false;
        } else if(mBinding.edtCompanyCode.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập mã công ty!");
            return false;
        } else if(mBinding.edtCompanyPhone.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập số điện thoại công ty!");
            return false;
        } else if(mBinding.edtCompanyAddress.getText().toString().trim().isEmpty()){
            showToast("Hãy nhập địa chỉ!");
            return false;
        } else if(mBinding.edtCompanyPhone.getText().toString().length() != 10){
            showToast("Sai định dạng số điện thoại!");
            return false;
        } else{
            return true;
        }
    }

    private void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
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