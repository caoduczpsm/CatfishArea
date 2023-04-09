package com.example.catfisharea.activities.admin;

import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.os.Build.VERSION.SDK_INT;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import com.android.app.catfisharea.databinding.ActivityCreateMultipleAccountBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.ExcelHandler;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class CreateMultipleAccountActivity extends BaseActivity {
    private ActivityCreateMultipleAccountBinding mBinding;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private List<String> availableUserPhoneList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityCreateMultipleAccountBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        getWindow().setStatusBarColor(Color.parseColor("#FFFFFF"));
        init();
        setListeners();
        availableUserPhoneList = getAvailableUserPhone();

    }

    private void init() {
        //FireStore
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        availableUserPhoneList = new ArrayList<>();
    }

    private void setListeners() {

        // Trở lại màn hình trước
        mBinding.toolbarMultipleAccount.setNavigationOnClickListener(view -> onBackPressed());

        mBinding.btnCreate.setOnClickListener(view -> openFileChooser());

    }

    private List<String> getAvailableUserPhone(){
        List<String> result = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        result.add(queryDocumentSnapshot.getString(Constants.KEY_PHONE));
                    }
                });
        return result;
    }

    private void openFileChooser() {
        if (SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {

                Intent intent = new Intent();
                intent.setType("*/*");
                intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Excel File "), 101);
            } else {

                Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                //Uri uri = Uri.fromParts("package", getPackageName(), null);
                startActivity(intent);
            }
        } else {

            Intent intent = new Intent();
            intent.setType("*/*");
            if (SDK_INT >= Build.VERSION_CODES.Q) {
                intent.putExtra(Intent.EXTRA_AUTO_LAUNCH_SINGLE_CHOICE, true);
            }
            intent.setAction(Intent.ACTION_GET_CONTENT);

            ActivityCompat.requestPermissions(CreateMultipleAccountActivity.this, new String[]{WRITE_EXTERNAL_STORAGE}, 102);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 101 && data != null) {
            Uri fileUri = data.getData();
            String filePath = ExcelHandler.getFilePathFromUri(fileUri);

//              Tạo excel Handler để thao tác file Excel
            ExcelHandler excelHandler = new ExcelHandler(filePath);
            List<List<String>> resultData = excelHandler.readDataSheet(excelHandler.getSheetAtIndex(0));
            boolean isCorrectData = true;
            if (resultData.size() == 0) {
                showToast("Tập tin vừa chọn không có nội dung!");
            } else {
                for (List<String> row : resultData){

                    if (Objects.equals(row.get(0).trim(), "")){
                        isCorrectData = false;
                    }

                    if (!(row.get(1).trim().length() == 10)){
                        isCorrectData = false;
                    }

                    if (availableUserPhoneList.contains(row.get(1))){
                        isCorrectData = false;
                    }

                    if (Objects.equals(row.get(2).trim(), "") || row.get(2) == null) {
                        isCorrectData = false;
                    }

                    if (Objects.equals(row.get(3).trim(), "") || row.get(3) == null){
                        isCorrectData = false;
                    }

                    if (Objects.equals(row.get(4).trim(), "") || row.get(4) == null){
                        isCorrectData = false;
                    }

                    if (Objects.equals(row.get(5).trim(), "") || row.get(5) == null) {
                        isCorrectData = false;
                    }

                    if (Objects.equals(row.get(6).trim(), "") || row.get(6) == null){
                        isCorrectData = false;
                    }

                }
            }

            if (!isCorrectData){
                showToast("Dữ liệu trong tập tin đã chọn không đúng định dạng!");
            } else {
                loading(true);
                for (List<String> row : resultData){
                    HashMap<String, Object> newAccount = new HashMap<>();
                    newAccount.put(Constants.KEY_NAME, row.get(0));
                    newAccount.put(Constants.KEY_PHONE, row.get(1));
                    newAccount.put(Constants.KEY_PERSONAL_ID, row.get(2));
                    newAccount.put(Constants.KEY_DATEOFBIRTH, row.get(3));
                    newAccount.put(Constants.KEY_ADDRESS, row.get(4));
                    newAccount.put(Constants.KEY_PASSWORD, row.get(5));
                    switch (row.get(6)) {
                        case "Trưởng vùng":
                        case "Trưởng Vùng":
                            newAccount.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_REGIONAL_CHIEF);
                            break;
                        case "Trưởng khu":
                        case "Trưởng Khu":
                            newAccount.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_DIRECTOR);
                            break;
                        case "Công nhân":
                        case "Trưởng Công":
                            newAccount.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER);
                            break;
                        case "Kế toán":
                        case "Kế Toán":
                            newAccount.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ACCOUNTANT);
                            break;
                        case "Admin":
                        case "admin":
                            newAccount.put(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_ADMIN);
                            break;
                    }
                    availableUserPhoneList.add(row.get(1));
                    newAccount.put(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID));
                    database.collection(Constants.KEY_COLLECTION_USER)
                            .add(newAccount);
                }
                loading(false);
                showToast("Tạo các tài khoản thành công!");
            }

        }
    }

    // Hàm giả lập trạng thái loading và ẩn lúc tạo
    private void loading(Boolean isLoading) {
        if (isLoading) {
            mBinding.btnCreate.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else {
            mBinding.btnCreate.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}