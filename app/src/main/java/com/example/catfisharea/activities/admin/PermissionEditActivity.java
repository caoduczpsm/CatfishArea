package com.example.catfisharea.activities.admin;

import androidx.appcompat.widget.SearchView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityPermissionEditBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PermissionEditActivity extends BaseActivity implements MultipleListener{

    private ActivityPermissionEditBinding mBinding;
    private ArrayList<String> listTypeAccount;
    private PreferenceManager preferenceManager;
    private MultipleUserSelectionAdapter usersAdapter;
    private List<User> users;
    private FirebaseFirestore database;
    private String typeAccount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityPermissionEditBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());
        init();

        getUsers();
        setListeners();
    }

    private void init(){
        Animatoo.animateSlideLeft(this);
        //PreferenceManager
        preferenceManager = new PreferenceManager(getApplicationContext());

        //FireStore
        database = FirebaseFirestore.getInstance();

//        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT);
//        Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        if (image != null) {
//            imageProfile.setImageBitmap(image);
//        }


        //List
        users = new ArrayList<>();

        //Adapter
        usersAdapter = new MultipleUserSelectionAdapter(users, this);
        mBinding.userRecyclerview.setAdapter(usersAdapter);

        //ArrayList
        listTypeAccount = new ArrayList<>();
        listTypeAccount.add("Admin");
        listTypeAccount.add("Kế Toán");
        listTypeAccount.add("Trưởng Vùng");
        listTypeAccount.add("Trưởng Khu");
        listTypeAccount.add("Công Nhân");
        typeAccount = Constants.KEY_WORKER;

        //ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.custom_layout_spinner, listTypeAccount);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mBinding.spinnerTypeAccount.setAdapter(adapter);

    }

    @SuppressLint({"NotifyDataSetChanged", "SetTextI18n"})
    private void setListeners(){
        mBinding.toolbarPermisionEdit.setNavigationOnClickListener(view -> onBackPressed());

        mBinding.btnEdit.setOnClickListener(view -> {

            loading(true);
            List<User> selectedUsers = usersAdapter.getSelectedUser();

            if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Admin")) {
                typeAccount = Constants.KEY_ADMIN;
            } else if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Kế Toán")) {
                typeAccount = Constants.KEY_ACCOUNTANT;
            } else if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Trưởng Vùng")) {
                typeAccount = Constants.KEY_REGIONAL_CHIEF;
            } else if (mBinding.spinnerTypeAccount.getSelectedItem().toString().equals("Trưởng Khu")) {
                typeAccount = Constants.KEY_DIRECTOR;
            } else typeAccount = Constants.KEY_WORKER;

            // Biến kiểm tra nếu người dùng chỉ chọn đúng 1 loại account
            boolean isOneType = true;
            // Biến lưu loại account mà người dùng đã chọn nếu trong danh sách chọn chỉ có 1 loại account
            String typeOne = "";

            // Các biến đếm số tài khoản bị thay đổi quyền theo từng loại
            int countAdmin = 0, countAccountant = 0, countRegionalChief = 0, countDirector = 0, countWorker = 0;

            for (int i = 0; i < selectedUsers.size(); i++){

                // Nếu như trong danh sách tài khoản được chọn là khác nhau thì isOneType sẽ bằng false
                // Nếu isOneType bằng true thì gán kết quả đầu tiên trong danh sách được chọn cho typeOne
                if (!Objects.equals(selectedUsers.get(0).position, selectedUsers.get(i).position))
                    isOneType = false;
                else typeOne = selectedUsers.get(0).position;

                // Đếm số lượng từng loại tài khoản được người dùng chọn
                switch (selectedUsers.get(i).position) {
                    case Constants.KEY_ADMIN:
                        countAdmin++;
                        break;
                    case Constants.KEY_ACCOUNTANT:
                        countAccountant++;
                        break;
                    case Constants.KEY_REGIONAL_CHIEF:
                        countRegionalChief++;
                        break;
                    case Constants.KEY_DIRECTOR:
                        countDirector++;
                        break;
                    case Constants.KEY_WORKER:
                        countWorker++;
                        break;
                }

            }

            if (selectedUsers.size() == 0){
                showToast("Vui lòng chọn ít nhất một tài khoản cần chỉnh sửa quyền!");
                loading(false);
            } else {

                loading(false);

                // Đếm số account được tròn trùng với số account đã là loại account mà người dùng muốn đổi
                int countTypeAccountAvailable = 0;

                for (int i = 0; i < selectedUsers.size(); i++){

                    HashMap<String, Object> user = new HashMap<>();
                    user.put(Constants.KEY_TYPE_ACCOUNT, typeAccount);

                    if (selectedUsers.get(i).position.equals(typeAccount)){
                        countTypeAccountAvailable++;
                    }

                    if (selectedUsers.get(i).id.equals(users.get(i).id)){
                        // Gán lại kết quả đã đổi vào ArrayList users
                        if (!typeAccount.equals(users.get(i).position)){
                            users.get(i).position = typeAccount;
                        }
                    }

                    int finalI = i;
                    database.collection(Constants.KEY_COLLECTION_USER)
                            .document(selectedUsers.get(i).id)
                            .update(user)
                            .addOnSuccessListener(unused -> {
                                if (finalI ==0)
                                    showToast("Cập nhật quyền cho các tài khoản thành công!");
                            })
                            .addOnFailureListener(e -> {
                                if (finalI ==0)
                                    showToast("Lỗi trong quá trình cập nhật!");
                            });

                }

                // Các biến sao chép kết quả của hàm truy vấn trên để sử dụng được ở hàm truy vấn ở đây
                int finalCountTypeAccountAvailable = countTypeAccountAvailable;
                String finalTypeOne = typeOne;
                boolean finalIsOneType = isOneType;
                int finalCountAdmin = countAdmin;
                int finalCountAccountant = countAccountant;
                int finalCountRegionalChief = countRegionalChief;
                int finalCountDirector = countDirector;
                int finalCountWorker = countWorker;
                database.collection(Constants.KEY_COLLECTION_COMPANY)
                        .document(preferenceManager.getString(Constants.KEY_COMPANY_ID))
                        .get()
                        .addOnCompleteListener(task -> {

                            DocumentSnapshot documentSnapshot = task.getResult();
                            //int currentTotalAccount = Integer.parseInt(Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_COMPANY_TOTAL_ACCOUNT)));

                            // Lấy số lượng các loại tài khoản hiện tài trên cơ sở dữ liệu
                            int currentAmountAdmin = Integer.parseInt(Objects.requireNonNull(
                                    documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_ADMIN)));
                            int currentAmountAccountant = Integer.parseInt(Objects.requireNonNull(
                                    documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT)));
                            int currentAmountRegionalChief = Integer.parseInt(Objects.requireNonNull(
                                    documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF)));
                            int currentAmountDirector = Integer.parseInt(Objects.requireNonNull(
                                    documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_DIRECTOR)));
                            int currentAmountWorker = Integer.parseInt(Objects.requireNonNull(
                                    documentSnapshot.getString(Constants.KEY_COMPANY_AMOUNT_WORKER)));

                            // Tạo các trường dữ liệu cần thay đổi trong bảng công ty
                            HashMap<String, Object> company = new HashMap<>();

                            /* Nếu danh sách được chọn có kích cỡ trùng với số lượng tài khoản thì cập nhật số lượng tài khoản như sau:
                             * Nếu tài khoản cần đổi là Admin thì đổi số lượng tài khoản Admin bằng độ dài của danh sách người dùng
                             *                   Các loại tài khoản khác bằng 0
                             * Tương tự với các loại tài khoản khác
                             * */

                            if (selectedUsers.size() == users.size()){

                                if (Constants.KEY_ADMIN.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, selectedUsers.size() + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, 0 + "");
                                }

                                if (Constants.KEY_ACCOUNTANT.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, selectedUsers.size() + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, 0 + "");
                                }

                                if (Constants.KEY_REGIONAL_CHIEF.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, selectedUsers.size() + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, 0 + "");
                                }

                                if (Constants.KEY_DIRECTOR.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, selectedUsers.size() + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, 0 + "");
                                }

                                if (Constants.KEY_WORKER.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, 0 + "");
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, selectedUsers.size() + "");
                                }

                            } if (finalIsOneType){

                                /*
                                 * Nếu danh sách tài khoản cần chỉnh sửa quyền mà người dùng chọn chỉ có một loại tài khoản duy nhất thì cập nhật như sau:
                                 *       Nếu loại tài khoản được chọn đó là Admin thì
                                 *           số lượng tài khoản Admin bằng với số Admin hiện tại (currentAmountAdmin) trừ cho danh sách mà người dùng chọn (selectedUsers.size())
                                 *                   Tương tự cho các loại tài khoản khác
                                 *       Nếu loại tài khoản người dùng cần thay đổi là Admin thì
                                 *           số lượng tài khoản Admin bằng với số lượng Admin hiện tại (currentAmountAdmin) cộng cho danh sách người dùng chọn (selectedUsers.size())
                                 *                   Tương tự cho các tài khoản khác
                                 * */


                                if (Constants.KEY_ADMIN.equals(finalTypeOne)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, currentAmountAdmin - selectedUsers.size() + "");
                                } else if (Constants.KEY_ACCOUNTANT.equals(finalTypeOne)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, currentAmountAccountant - selectedUsers.size() + "");
                                } else if (Constants.KEY_REGIONAL_CHIEF.equals(finalTypeOne)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, currentAmountRegionalChief - selectedUsers.size() + "");
                                } else if (Constants.KEY_DIRECTOR.equals(finalTypeOne)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, currentAmountDirector - selectedUsers.size() + "");
                                } else if (Constants.KEY_WORKER.equals(finalTypeOne)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, currentAmountWorker - selectedUsers.size() + "");
                                }

                                if (Constants.KEY_ADMIN.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, currentAmountAdmin + selectedUsers.size() + "");
                                } else if (Constants.KEY_ACCOUNTANT.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, currentAmountAccountant + selectedUsers.size() + "");
                                } else if (Constants.KEY_REGIONAL_CHIEF.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, currentAmountRegionalChief + selectedUsers.size() + "");
                                } else if (Constants.KEY_DIRECTOR.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, currentAmountDirector + selectedUsers.size() + "");
                                } else if (Constants.KEY_WORKER.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, currentAmountWorker + selectedUsers.size() + "");
                                }


                            } else {

                                /*
                                 * Nếu người dùng chọn hỗ hợp nhiều tài khoản khác nhau cần đổi quyền thì xử lý như sau:
                                 * Nếu loại tài khoản được đổi (typeAccount) là Admin thì
                                 *           số lượng tài khoản Admin hiện tài (currentAmountAdmin) + độ dài danh sách tài khoản mà người dùng chọn (selectedUsers.size())
                                 *                                             - số lượng tài khoản admin trong danh sách người dùng chọn (finalCountTypeAccountAvailable)
                                 * Ngược lại thì số lượng Admin bằng số lượng Admin hiện tại (currentAmountAdmin) trừ số lượng Admin trong danh sách mà người dùng chọn (countAdmin)
                                 *
                                 * Tương tự cho các loại tài khoản khác
                                 * */

                                if (Constants.KEY_ADMIN.equals(typeAccount)){
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, currentAmountAdmin + selectedUsers.size()
                                            - finalCountTypeAccountAvailable + "");
                                } else company.put(Constants.KEY_COMPANY_AMOUNT_ADMIN, currentAmountAdmin - finalCountAdmin + "");

                                if (Constants.KEY_ACCOUNTANT.equals(typeAccount))
                                    company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, currentAmountAccountant + selectedUsers.size()
                                            - finalCountTypeAccountAvailable + "");
                                else company.put(Constants.KEY_COMPANY_AMOUNT_ACCOUNTANT, currentAmountAccountant - finalCountAccountant + "");

                                if (Constants.KEY_REGIONAL_CHIEF.equals(typeAccount))
                                    company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, currentAmountRegionalChief + selectedUsers.size()
                                            - finalCountTypeAccountAvailable + "");
                                else company.put(Constants.KEY_COMPANY_AMOUNT_REGIONAL_CHIEF, currentAmountRegionalChief - finalCountRegionalChief + "");

                                if (Constants.KEY_DIRECTOR.equals(typeAccount))
                                    company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, currentAmountDirector + selectedUsers.size()
                                            - finalCountTypeAccountAvailable + "");
                                else company.put(Constants.KEY_COMPANY_AMOUNT_DIRECTOR, currentAmountDirector - finalCountDirector + "");

                                if (Constants.KEY_WORKER.equals(typeAccount))
                                    company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, currentAmountWorker + selectedUsers.size()
                                            - finalCountTypeAccountAvailable + "");
                                else company.put(Constants.KEY_COMPANY_AMOUNT_WORKER, currentAmountWorker - finalCountWorker + "");
                            }

                            database.collection(Constants.KEY_COLLECTION_COMPANY)
                                    .document(preferenceManager.getString(Constants.KEY_COMPANY_ID))
                                    .update(company);


                        });
                // Hàm giúp bỏ chọn các user đang được người dùng chọn sau khi bấm thay đổi quyền
                usersAdapter.setUserUnSelected(users);

            }

        });

        // Chọn loại tài khoản cần tạo
        mBinding.spinnerTypeAccount.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (listTypeAccount.get(i)) {
                    case Constants.KEY_ADMIN:
                        typeAccount = Constants.KEY_ADMIN;
                        break;
                    case Constants.KEY_ACCOUNTANT:
                        typeAccount = Constants.KEY_ACCOUNTANT;
                        break;
                    case Constants.KEY_REGIONAL_CHIEF:
                        typeAccount = Constants.KEY_REGIONAL_CHIEF;
                        break;
                    case Constants.KEY_DIRECTOR:
                        typeAccount = Constants.KEY_DIRECTOR;
                        break;
                    default:
                        typeAccount = Constants.KEY_WORKER;
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                typeAccount = Constants.KEY_WORKER;
            }
        });

        // Tìm kiếm tài khoản
        mBinding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                usersAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                usersAdapter.getFilter().filter(inputSearch);
                return true;
            }
        });

        // Mở hộp thoại bộ lọc
        mBinding.imageFilter.setOnClickListener(view -> openFilterSearchDialog());

        // Chọn hoặc bỏ chọn tất cả tài khoản
        mBinding.cbAllAccount.setOnClickListener(view -> {
            if (mBinding.cbAllAccount.getText().toString().equals("Tất Cả")){
                usersAdapter.setAllSelected(users);
                mBinding.cbAllAccount.setText("Bỏ Chọn");
            } else {
                usersAdapter.setAllUnSelected(users);
                mBinding.cbAllAccount.setText("Tất Cả");
            }
        });

    }

    // Hàm mở họp thoại tài khoản
    @SuppressLint("SetTextI18n")
    private void openFilterSearchDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_filter_search);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);

        //CheckBox
        CheckBox cbAllAccount, cbAdmin, cbAccountant, cbRegionalChief, cbDirector, cbWorker,
                cbAllArea, cbArea, cbCampus, cbPond;

        cbAllAccount = dialog.findViewById(R.id.cbAllAccount);
        cbAdmin = dialog.findViewById(R.id.cbAdmin);
        cbAccountant = dialog.findViewById(R.id.cbAccountant);
        cbRegionalChief = dialog.findViewById(R.id.cbRegionalChief);
        cbDirector = dialog.findViewById(R.id.cbDirector);
        cbWorker = dialog.findViewById(R.id.cbWorker);
        cbAllArea = dialog.findViewById(R.id.cbAllArea);
        cbArea = dialog.findViewById(R.id.cbArea);
        cbCampus = dialog.findViewById(R.id.cbCampus);
        cbPond = dialog.findViewById(R.id.cbPond);

        cbAdmin.setOnClickListener(view -> {
            if (cbAccountant.isChecked() && cbRegionalChief.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbAccountant.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbRegionalChief.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbRegionalChief.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbDirector.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbDirector.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbRegionalChief.isChecked() && cbWorker.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbWorker.setOnClickListener(view -> {
            if (cbAdmin.isChecked() && cbAccountant.isChecked() &&
                    cbRegionalChief.isChecked() && cbDirector.isChecked()){
                cbAllAccount.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            }
            if (!cbAdmin.isChecked() || !cbAccountant.isChecked() || !cbRegionalChief.isChecked()
                    || !cbDirector.isChecked() || !cbWorker.isChecked()){
                cbAllAccount.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }
        });

        cbArea.setOnClickListener(view -> {
            if (cbCampus.isChecked() && cbPond.isChecked()){
                cbAllArea.setChecked(true);
                cbAllArea.setText("Bỏ Chọn");
            }
            if (!cbArea.isChecked() || !cbCampus.isChecked() || !cbPond.isChecked()){
                cbAllArea.setChecked(false);
                cbAllArea.setText("Tất Cả");
            }
        });

        cbCampus.setOnClickListener(view -> {
            if (cbArea.isChecked() && cbPond.isChecked()){
                cbAllArea.setChecked(true);
                cbAllArea.setText("Bỏ Chọn");
            }
            if (!cbArea.isChecked() || !cbCampus.isChecked() || !cbPond.isChecked()){
                cbAllArea.setChecked(false);
                cbAllArea.setText("Tất Cả");
            }
        });

        cbPond.setOnClickListener(view -> {
            if (cbArea.isChecked() && cbCampus.isChecked()){
                cbAllArea.setChecked(true);
                cbAllArea.setText("Bỏ Chọn");
            }
            if (!cbArea.isChecked() || !cbCampus.isChecked() || !cbPond.isChecked()){
                cbAllArea.setChecked(false);
                cbAllArea.setText("Tất Cả");
            }
        });

        // Chọn tất cả loại tài khoản
        cbAllAccount.setOnClickListener(view -> {
            if (cbAllAccount.getText().toString().equals("Tất Cả")){
                cbAdmin.setChecked(true);
                cbAccountant.setChecked(true);
                cbRegionalChief.setChecked(true);
                cbDirector.setChecked(true);
                cbWorker.setChecked(true);
                cbAllAccount.setText("Bỏ Chọn");
            } else {
                cbAdmin.setChecked(false);
                cbAccountant.setChecked(false);
                cbRegionalChief.setChecked(false);
                cbDirector.setChecked(false);
                cbWorker.setChecked(false);
                cbAllAccount.setText("Tất Cả");
            }

        });

        // Chọn tất cả các vùng
        cbAllArea.setOnClickListener(view -> {
            if (cbAllArea.getText().toString().equals("Tất Cả")){
                cbArea.setChecked(true);
                cbCampus.setChecked(true);
                cbPond.setChecked(true);
                cbAllArea.setText("Bỏ Chọn");
            } else {
                cbArea.setChecked(false);
                cbCampus.setChecked(false);
                cbPond.setChecked(false);
                cbAllArea.setText("Tất Cả");
            }

        });

        // Bấm áp dụng
        btnApply.setOnClickListener(view -> {
            // Nếu checkbox tất cả tài khoản hoặc tất cả vùng được chọn thì hiển thị ra tất cả người dùng
            if (cbAllAccount.isChecked() || cbAllArea.isChecked()){
                users.clear();
                getUsers();
                dialog.dismiss();
            } else {
                users.clear();
                // Ngược lại thì truyền loại tài khoản người dùng vào hàm bộ lọc
                if (cbAdmin.isChecked()){
                    getFilterUser(Constants.KEY_ADMIN);
                }

                if (cbAccountant.isChecked()){
                    getFilterUser(Constants.KEY_ACCOUNTANT);
                }

                if (cbRegionalChief.isChecked()){
                    getFilterUser(Constants.KEY_REGIONAL_CHIEF);
                }

                if (cbDirector.isChecked()){
                    getFilterUser(Constants.KEY_DIRECTOR);
                }

                if (cbWorker.isChecked()){
                    getFilterUser(Constants.KEY_WORKER);
                }

                dialog.dismiss();
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
    }

    // Hàm hiển thị người dùng theo bộ lọc
    @SuppressLint("NotifyDataSetChanged")
    private void getFilterUser(String type){
        // Kiểm tra nếu tài khoản có cùng loại với bộ lọc thì mới hiển thị
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, type)
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            usersAdapter.notifyDataSetChanged();

                        }

                    }

                });
    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(PermissionEditActivity.this);
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

    // Hàm lấy các user từ database về và hiển thị ra recyclerview
    @SuppressLint("NotifyDataSetChanged")
    private void getUsers(){

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {

                    if (task.isSuccessful() && task.getResult() != null) {
                        users.clear();
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {

                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                            usersAdapter.notifyDataSetChanged();

                        }

                    }

                });

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

    // Hàm giả lập trạng thái loading và ẩn lúc tạo
    private void loading(Boolean isLoading){
        if(isLoading){
            mBinding.btnEdit.setVisibility(View.INVISIBLE);
            mBinding.progressBar.setVisibility(View.VISIBLE);
        } else{
            mBinding.btnEdit.setVisibility(View.VISIBLE);
            mBinding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    private void showToast(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

}