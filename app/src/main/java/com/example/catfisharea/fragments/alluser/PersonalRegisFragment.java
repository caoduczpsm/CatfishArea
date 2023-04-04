package com.example.catfisharea.fragments.alluser;

import static android.app.Activity.RESULT_OK;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.provider.MediaStore;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;


import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.FragmentPersonalRegisBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;

import com.example.catfisharea.activities.admin.AdminHomeActivity;
import com.example.catfisharea.activities.personal.PersonalUserHomeActivity;
import com.example.catfisharea.activities.worker.WorkerHomeActivity;

import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Objects;

public class PersonalRegisFragment extends Fragment {
    private FragmentPersonalRegisBinding mBinding;
    private Calendar myCal;
    private String encodedImage;
    private String phone, password;
    private PreferenceManager preferenceManager;
    FirebaseFirestore database;
    ViewPager viewPager;

    public PersonalRegisFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mBinding = FragmentPersonalRegisBinding.inflate(inflater, container, false);

        preferenceManager = new PreferenceManager(requireContext().getApplicationContext());
        //FireStore
        database = FirebaseFirestore.getInstance();

        // Lấy số điện thoại và mật khẩu từ màn hình đăng ký
        phone = requireActivity().getIntent().getStringExtra(Constants.KEY_PHONE);
        password = requireActivity().getIntent().getStringExtra(Constants.KEY_PASSWORD);
        //Calendar
        myCal = Calendar.getInstance();

// SetText cho nút đăng ký theo tùy chọn cá nhân hay công ty
        if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_DATA) == null)
            preferenceManager.putString(Constants.KEY_CHECK_AVAILABLE_DATA, "");
        if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_DATA).equals(Constants.KEY_CHECK_AVAILABLE_DATA)
                && !Objects.equals(preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_DATA), "")) {
            mBinding.btnRegis.setText("Hoàn Thành");
            preferenceManager.remove(Constants.KEY_CHECK_AVAILABLE_DATA);
        } else {
            if (preferenceManager.getString(Constants.KEY_TYPE_REGIS).equals(Constants.KEY_PERSONAL_REGIS)) {
                preferenceManager.remove(Constants.KEY_TYPE_REGIS);
                mBinding.btnRegis.setText("Đăng Ký");
            } else {
                preferenceManager.remove(Constants.KEY_TYPE_REGIS);
                mBinding.btnRegis.setText("Tiếp Tục");
            }
        }

        setListeners();

        return mBinding.getRoot();
    }

    private void setListeners() {

        // Chọn hình ảnh
        mBinding.layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        // Chọn ngày tháng năm sinh
        mBinding.textDateOfBirth.setOnClickListener(view -> openDatePicker());

        mBinding.btnRegis.setOnClickListener(view -> {
            if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_DATA).equals(Constants.KEY_CHECK_AVAILABLE_DATA)) {
                String address = mBinding.edtAddress.getText().toString();
                String dateOfBirth = mBinding.textDateOfBirth.getText().toString();
                String name = mBinding.edtName.getText().toString();
                String personalID = mBinding.edtPersonalID.getText().toString();

                // Cập nhật dữ liệu của người dùng còn thiếu lên firebase
                HashMap<String, Object> user = new HashMap<>();
                user.put(Constants.KEY_ADDRESS, address);
                user.put(Constants.KEY_DATEOFBIRTH, dateOfBirth);
                user.put(Constants.KEY_NAME, name);
                user.put(Constants.KEY_PERSONAL_ID, personalID);
                user.put(Constants.KEY_IMAGE, encodedImage);
                database.collection(Constants.KEY_COLLECTION_USER)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .update(user)
                        .addOnCompleteListener(task -> {
                            showToast("Cập nhật thông tin thành công!");
                            preferenceManager.remove(Constants.KEY_CHECK_AVAILABLE_DATA);
                            Intent intent = null;
                            if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_TYPE_ACCOUNT).equals(Constants.KEY_ADMIN)) {
                                intent = new Intent(getContext(), AdminHomeActivity.class);
                            } else if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_TYPE_ACCOUNT).equals(Constants.KEY_ACCOUNTANT)) {
//                                intent = new Intent(getContext(), AccountantHomeActivity.class);
                            } else if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)) {
//                                intent = new Intent(getContext(), RegionalChiefHomeActivity.class);
                            } else if (preferenceManager.getString(Constants.KEY_CHECK_AVAILABLE_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)) {
//                                intent = new Intent(getContext(), DirectorHomeActivity.class);
                            } else {
                                intent = new Intent(getContext(), WorkerHomeActivity.class);
                            }
                            preferenceManager.remove(Constants.KEY_CHECK_AVAILABLE_TYPE_ACCOUNT);
                            preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true);
                            preferenceManager.putString(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                            preferenceManager.putString(Constants.KEY_NAME, name);
                            preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                            preferenceManager.putString(Constants.KEY_PERSONAL_ID, personalID);
                            preferenceManager.putString(Constants.KEY_DATEOFBIRTH, dateOfBirth);
                            preferenceManager.putString(Constants.KEY_ADDRESS, address);
                            if (intent != null) {
                                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                Animatoo.animateSlideLeft(requireActivity());
                                startActivity(intent);
                            }

                        });
            } else {
                if (preferenceManager.getString(Constants.KEY_TYPE_REGIS).equals(Constants.KEY_PERSONAL_REGIS)) {
                    if (isValidSignUpDetails())
                        regisPersonalAccount();
                } else {
                    if (isValidSignUpDetails())
                        regisCompanyAccount();
                }
            }

        });

    }

    private void regisPersonalAccount() {

        // Giả lập loading, hiển thị progress bar trong thời gian chờ đăng ký tài khoản
        loading(true);

        // Tạo các trường trong bảng users
        HashMap<String, Object> user = new HashMap<>();
        user.put(Constants.KEY_PHONE, phone);
        user.put(Constants.KEY_PASSWORD, password);
        user.put(Constants.KEY_IMAGE, encodedImage);
        user.put(Constants.KEY_NAME, mBinding.edtName.getText().toString());
        user.put(Constants.KEY_PERSONAL_ID, mBinding.edtPersonalID.getText().toString());
        user.put(Constants.KEY_DATEOFBIRTH, mBinding.textDateOfBirth.getText().toString());
        user.put(Constants.KEY_ADDRESS, mBinding.edtAddress.getText().toString());
        user.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_USER);

        // Đẩy thông tin công ty lên Firebase
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
                    preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
                    preferenceManager.putString(Constants.KEY_NAME, mBinding.edtName.getText().toString());
                    preferenceManager.putString(Constants.KEY_PERSONAL_ID, mBinding.edtPersonalID.getText().toString());
                    preferenceManager.putString(Constants.KEY_DATEOFBIRTH, mBinding.textDateOfBirth.getText().toString());
                    preferenceManager.putString(Constants.KEY_ADDRESS, mBinding.edtAddress.getText().toString());

                    // Chuyển màn hình sang màn hình chính của Admin
                    Intent intent = new Intent(getActivity(), PersonalUserHomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    Animatoo.animateSlideLeft(requireActivity());
                    startActivity(intent);

                })
                .addOnFailureListener(exception -> {
                    // Ẩn progress bar khi xử lý lỗi
                    loading(false);
                    // Hiển thị lỗi nếu thất bại
                    showToast(exception.getMessage());
                });

    }

    private void regisCompanyAccount() {

        preferenceManager.putString(Constants.KEY_PHONE, phone);
        preferenceManager.putString(Constants.KEY_PASSWORD, password);
        preferenceManager.putString(Constants.KEY_IMAGE, encodedImage);
        preferenceManager.putString(Constants.KEY_NAME, mBinding.edtName.getText().toString());
        preferenceManager.putString(Constants.KEY_PERSONAL_ID, mBinding.edtPersonalID.getText().toString());
        preferenceManager.putString(Constants.KEY_DATEOFBIRTH, mBinding.textDateOfBirth.getText().toString());
        preferenceManager.putString(Constants.KEY_ADDRESS, mBinding.edtAddress.getText().toString());
        preferenceManager.putString(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ADMIN);

        viewPager = requireActivity().findViewById(R.id.viewPager);
        viewPager.setCurrentItem(2);

    }

    private void openDatePicker() {
        @SuppressLint("SetTextI18n") DatePickerDialog.OnDateSetListener dateListener = (view, year, month, day) -> {
            myCal.set(Calendar.YEAR, year);
            myCal.set(Calendar.MONTH, month);
            myCal.set(Calendar.DAY_OF_MONTH, day);
            mBinding.textDateOfBirth.setText(myCal.get(Calendar.DAY_OF_MONTH) + "/"
                    + (myCal.get(Calendar.MONTH) + 1) + "/"
                    + myCal.get(Calendar.YEAR));
        };

        DatePickerDialog dialog = new DatePickerDialog(
                getActivity(), dateListener,
                myCal.get(Calendar.YEAR),
                myCal.get(Calendar.MONTH),
                myCal.get(Calendar.DAY_OF_MONTH)
        );
        dialog.show();
    }

    private String encodeImage(Bitmap bitmap) {
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
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = requireActivity().getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            mBinding.imageProfile.setImageBitmap(bitmap);
                            mBinding.textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private Boolean isValidSignUpDetails() {
        if (encodedImage == null) {
            showToast("Hãy chọn hình ảnh!");
            return false;
        } else if (mBinding.edtName.getText().toString().trim().isEmpty()) {
            showToast("Hãy nhập họ và tên!");
            return false;
        } else if (mBinding.edtPersonalID.getText().toString().trim().isEmpty()) {
            showToast("Hãy nhập CMND/CCCD");
            return false;
        } else if (mBinding.textDateOfBirth.getText().toString().trim().isEmpty()) {
            showToast("Hãy chọn ngày tháng năm sinh!");
            return false;
        } else if (mBinding.edtAddress.getText().toString().trim().isEmpty()) {
            showToast("Hãy nhập địa chỉ!");
            return false;
        } else {
            return true;
        }
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            mBinding.btnRegis.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnRegis.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

}