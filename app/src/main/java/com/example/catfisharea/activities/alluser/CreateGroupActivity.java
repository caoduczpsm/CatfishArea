package com.example.catfisharea.activities.alluser;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.widget.SearchView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityCreateGroupBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CreateGroupActivity extends BaseActivity implements UserListener, MultipleListener {

    private ActivityCreateGroupBinding binding;
    private PreferenceManager preferenceManager;
    private MultipleUserSelectionAdapter usersAdapter;
    private FirebaseFirestore database;
    private List<User> users;
    private String encodedImage;
    private RoundedImageView imageGroupProfile;
    private TextView textAddImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCreateGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListener();
        getUsers();
    }

    private void init(){
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        users = new ArrayList<>();
    }

    private void setListener(){
        binding.toolbarCreateGroup.setNavigationOnClickListener(view -> onBackPressed());
        binding.btnSelection.setOnClickListener(view -> {
            List<User> selectedUser = usersAdapter.getSelectedUser();
            StringBuilder userIds = new StringBuilder();
            for (int i = 0; i < selectedUser.size(); i++) {
                userIds.append(selectedUser.get(i).id).append(",");
            }
            if (selectedUser.size() <= 0){
                Toast.makeText(this, "Hãy chọn một người bạn", Toast.LENGTH_SHORT).show();
            } else {
                userIds.append(preferenceManager.getString(Constants.KEY_USER_ID));
                openSetInfoGroupDialog();
            }
        });

        // Mở hộp thoại bộ lọc
        binding.imageFilter.setOnClickListener(view -> openFilterSearchDialog());

        // Tìm kiếm tài khoản
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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
    }

    private void getUsers() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {
                    loading(false);
                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                            user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if (users.size() > 0) {
                            usersAdapter = new MultipleUserSelectionAdapter(users, this);
                            binding.userRecyclerview.setAdapter(usersAdapter);
                            binding.userRecyclerview.setVisibility(View.VISIBLE);
                        } else {
                            showErrorMessage();
                        }
                    } else {
                        showErrorMessage();
                    }
                });

    }

    // Hàm mở họp thoại tài khoản
    @SuppressLint("SetTextI18n")
    private void openSetInfoGroupDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_set_info_group);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnCreate = dialog.findViewById(R.id.btnCreateGroup);

        //RoundedImageView
        imageGroupProfile = dialog.findViewById(R.id.imageGroupProfile);

        //TextView
        textAddImage = dialog.findViewById(R.id.textAddImage);
        TextView textGroupName = dialog.findViewById(R.id.textGroupName);
        TextView textGroupDescription = dialog.findViewById(R.id.textGroupDescription);

        //FrameLayout
        FrameLayout layoutImage = dialog.findViewById(R.id.layoutImage);

        layoutImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        btnCreate.setOnClickListener(view -> {
            if (textGroupName.getText().toString().equals("") ||
                    textGroupDescription.getText().toString().equals("") ||
                    encodedImage == null){
                showToast("Vui lòng nhập đầy đủ thông tin!");
            } else {
                List<User> usersSelected = usersAdapter.getSelectedUser();
                List<String> userIds = new ArrayList<>();
                for (User user : usersSelected){
                    userIds.add(user.id);
                }
                userIds.add(preferenceManager.getString(Constants.KEY_USER_ID));
                loading(true);
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                HashMap<String, Object> member = new HashMap<>();
                member.put(Constants.KEY_GROUP_NAME, textGroupName.getText().toString());
                member.put(Constants.KEY_GROUP_DESCRIPTION, textGroupDescription.getText().toString());
                member.put(Constants.KEY_GROUP_IMAGE, encodedImage);
                member.put(Constants.KEY_GROUP_OWNER, preferenceManager.getString(Constants.KEY_USER_ID));

                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .add(member).addOnSuccessListener(documentReference -> {
                            for (String userId : userIds) {
                                HashMap<String, Object> id = new HashMap<>();
                                id.put("Owner", true);
                                database.collection(Constants.KEY_COLLECTION_GROUP).document(documentReference.getId())
                                        .collection(Constants.KEY_GROUP_MEMBER).document(userId)
                                        .set(id).addOnSuccessListener(aVoid ->
                                                database.collection(Constants.KEY_COLLECTION_USER)
                                                        .document(userId)
                                                        .collection(Constants.KEY_GROUP_ID)
                                                        .document(documentReference.getId())
                                                        .set(id)
                                                        .addOnSuccessListener(aVoid1 -> {
                                                            startActivity(new Intent(this, ConversationActivity.class));
                                                            Animatoo.animateSlideLeft(CreateGroupActivity.this);
                                                        })
                                                        .addOnFailureListener(e -> showToast(e.getMessage())))
                                        .addOnFailureListener(e -> showToast(e.getMessage()));

                            }
                        });
                dialog.dismiss();
                showToast("Tạo nhóm thành công!");
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();
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
        CheckBox cbAllAccount, cbAdmin, cbAccountant, cbRegionalChief, cbDirector, cbWorker;

        cbAllAccount = dialog.findViewById(R.id.cbAllAccount);
        cbAdmin = dialog.findViewById(R.id.cbAdmin);
        cbAccountant = dialog.findViewById(R.id.cbAccountant);
        cbRegionalChief = dialog.findViewById(R.id.cbRegionalChief);
        cbDirector = dialog.findViewById(R.id.cbDirector);
        cbWorker = dialog.findViewById(R.id.cbWorker);

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

        // Bấm áp dụng
        btnApply.setOnClickListener(view -> {
            // Nếu checkbox tất cả tài khoản hoặc tất cả vùng được chọn thì hiển thị ra tất cả người dùng
            if (cbAllAccount.isChecked()){
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

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    if (result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageGroupProfile.setImageBitmap(bitmap);
                            textAddImage.setVisibility(View.GONE);
                            encodedImage = encodeImage(bitmap);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private String encodeImage(Bitmap bitmap) {
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(CreateGroupActivity.this);
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

    private void showErrorMessage() {
        binding.textError.setText(String.format("%s", R.string.No_user));
        binding.textError.setVisibility(View.VISIBLE);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
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

    @Override
    public void onUserClicker(User user) {

    }

}