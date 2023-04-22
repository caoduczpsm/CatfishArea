package com.example.catfisharea.activities.alluser;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivitySettingGroupBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.MultipleUserSelectionAdapter;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Group;
import com.example.catfisharea.models.Task;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.makeramen.roundedimageview.RoundedImageView;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class SettingGroupActivity extends BaseActivity implements MultipleListener, UserListener {
    private Group group;
    private FirebaseFirestore database;
    private PreferenceManager preferenceManager;
    private ActivitySettingGroupBinding binding;
    private TextView textNewAddImage;
    private RoundedImageView imageNewGroupProfile;
    private String encodedImage;
    private List<User> users;
    private List<String> groupMember;
    private MultipleUserSelectionAdapter usersAdapter;
    private RecyclerView usersNotInGroupRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();

        setGroupInfo();
        setListener();
    }

    private void init(){
        group = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
        users = new ArrayList<>();
        groupMember = new ArrayList<>();
        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(group.id)
                .collection(Constants.KEY_GROUP_MEMBER)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                        groupMember.add(queryDocumentSnapshot.getId());
                    }
                });
        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(group.id)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();
                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(documentSnapshot.getString(Constants.KEY_GROUP_OWNER))){
                        binding.layoutDeleteMember.setVisibility(View.VISIBLE);
                        binding.layoutChangeTeamLead.setVisibility(View.VISIBLE);
                        binding.layoutDeleteGroup.setVisibility(View.VISIBLE);
                    } else {
                        binding.layoutDeleteMember.setVisibility(View.GONE);
                        binding.layoutChangeTeamLead.setVisibility(View.GONE);
                        binding.layoutDeleteGroup.setVisibility(View.GONE);
                    }
                });
    }

    private void setGroupInfo(){

        database.collection(Constants.KEY_COLLECTION_GROUP)
                .document(group.id)
                .get()
                .addOnCompleteListener(task -> {
                    DocumentSnapshot documentSnapshot = task.getResult();

                    // Hiển thị tên và mô tả của nhóm
                    binding.textName.setText(documentSnapshot.getString(Constants.KEY_GROUP_NAME));
                    binding.textDescription.setText(documentSnapshot.getString(Constants.KEY_GROUP_DESCRIPTION));

                    // Hiển thị ảnh đại diện nhóm
                    byte[] bytes = Base64.decode(documentSnapshot.getString(Constants.KEY_GROUP_IMAGE), Base64.DEFAULT);
                    Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    binding.imageProfile.setImageBitmap(image);
                });
    }

    private void setListener(){
        binding.toolbarSettingGroup.setNavigationOnClickListener(view -> onBackPressed());

        // Mở hộp thoại đổi tên nhóm
        binding.layoutChangeName.setOnClickListener(view -> openChangeNameDialog());

        // Mở hộp thoại mô tả nhóm
        binding.layoutChangeDescription.setOnClickListener(view -> openChangeDescriptionDialog());

        // Mở hộp thoại ảnh đại diện nhóm
        binding.layoutChangeAvatar.setOnClickListener(view -> openChangeAvatarDialog());

        // Mở hộp thoại xem thành viên
        binding.layoutShowMember.setOnClickListener(view -> openShowMemberDialog());

        // Mở hộp thoại thêm thành viên
        binding.layoutAddMember.setOnClickListener(view -> openAddMemberDialog());

        // Mở hộp thoại xóa thành viên
        binding.layoutDeleteMember.setOnClickListener(view -> openDeleteMemberDialog());

        // Mở hộp thoại đổi trưởng nhóm
        binding.layoutChangeTeamLead.setOnClickListener(view -> openChangeTeamLeadDialog());

        // Mở hộp thoại thoát nhóm
        binding.layoutExitGroup.setOnClickListener(view -> openExitGroupDialog());

        // Mở hộp thoại xóa nhóm
        binding.layoutDeleteGroup.setOnClickListener(view -> openDeleteGroupDialog());
    }

    // Hàm mở hộp thoại đổi tên nhóm
    @SuppressLint("SetTextI18n")
    private void openChangeNameDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_change_group_name);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);

        EditText edtGroupName = dialog.findViewById(R.id.textGroupName);

        btnApply.setOnClickListener(view -> {

            if (!edtGroupName.getText().toString().equals("")){
                HashMap<String, Object> info = new HashMap<>();
                info.put(Constants.KEY_GROUP_NAME, edtGroupName.getText().toString());

                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .document(group.id)
                        .update(info)
                        .addOnSuccessListener(unused -> {
                            showToast("Cập nhật tên nhóm thành công!");
                            binding.textName.setText(edtGroupName.getText().toString());

                            // Đổi tên nhóm trong conversation
                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                                            HashMap<String, Object> conversation = new HashMap<>();
                                            conversation.put(Constants.KEY_RECEIVER_NAME, edtGroupName.getText().toString());
                                            if (Objects.equals(queryDocumentSnapshot.getString(Constants.KEY_RECEIVER_ID), group.id)){
                                                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                                        .document(queryDocumentSnapshot.getId())
                                                        .update(conversation);
                                            }
                                        }
                                    });

                        })
                        .addOnFailureListener(e -> showToast("Cập nhật tên nhóm thất bại!"));
                dialog.dismiss();
            } else {
                showToast("Vui lòng nhập tên nhóm mới!");
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại đổi mô tả nhóm
    @SuppressLint("SetTextI18n")
    private void openChangeDescriptionDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_change_group_description);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);

        EditText edtGroupDescription = dialog.findViewById(R.id.textGroupDescription);

        btnApply.setOnClickListener(view -> {

            if (!edtGroupDescription.getText().toString().equals("")){
                HashMap<String, Object> info = new HashMap<>();
                info.put(Constants.KEY_GROUP_DESCRIPTION, edtGroupDescription.getText().toString());

                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .document(group.id)
                        .update(info)
                        .addOnSuccessListener(unused -> {
                            showToast("Cập nhật mô tả nhóm thành công!");
                            binding.textDescription.setText(edtGroupDescription.getText().toString());
                        })
                        .addOnFailureListener(e -> showToast("Cập nhật mô tả nhóm thất bại!"));
                dialog.dismiss();
            } else {
                showToast("Vui lòng nhập mô tả nhóm mới!");
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại đổi ảnh đại diện nhóm
    @SuppressLint("SetTextI18n")
    private void openChangeAvatarDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_change_group_avatar);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        textNewAddImage = dialog.findViewById(R.id.textNewAddImage);
        imageNewGroupProfile = dialog.findViewById(R.id.imageNewGroupProfile);
        FrameLayout layoutNewImage = dialog.findViewById(R.id.layoutNewImage);

        layoutNewImage.setOnClickListener(view -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            pickImage.launch(intent);
        });

        btnApply.setOnClickListener(view -> {

            if (!Objects.equals(encodedImage, "")){

                HashMap<String, Object> info = new HashMap<>();
                info.put(Constants.KEY_GROUP_IMAGE, encodedImage);

                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .document(group.id)
                        .update(info)
                        .addOnSuccessListener(unused -> {
                            showToast("Cập nhật ảnh đại diện nhóm thành công!");

                            // Hiển thị ảnh đại diện nhóm mới
                            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                            Bitmap image = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            binding.imageProfile.setImageBitmap(image);
                        })
                        .addOnFailureListener(e -> showToast("Cập nhật ảnh đại diện nhóm thất bại!"));
                dialog.dismiss();
            } else {
                showToast("Vui lòng chọn một ảnh đại diện mới!");
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại xem thành viên trong nhóm
    @SuppressLint("SetTextI18n")
    private void openShowMemberDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_show_member);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        users.clear();
        for (String memberId : groupMember){

            database.collection(Constants.KEY_COLLECTION_USER)
                    .document(memberId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        User user = new User();
                        user.name = documentSnapshot.getString(Constants.KEY_NAME);
                        user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                        user.position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                        user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                        user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        user.id = documentSnapshot.getId();
                        users.add(user);
                        users.size();
                        UsersAdapter adapter = new UsersAdapter(users, this);
                        userRecyclerView.setAdapter(adapter);
                        userRecyclerView.setVisibility(View.VISIBLE);
                    });

        }

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại thêm thành viên vào nhóm
    @SuppressLint("SetTextI18n")
    private void openAddMemberDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_add_member);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        usersNotInGroupRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        getUsersNotInGroup();

        btnApply.setOnClickListener(view -> {
            List<User> selectedUser = usersAdapter.getSelectedUser();
            if (selectedUser.size() > 0){
                for (int i = 0; i < selectedUser.size(); i++) {
                    HashMap<String, Object> id = new HashMap<>();
                    id.put("Owner", true);
                    String userID = selectedUser.get(i).id;
                    database.collection(Constants.KEY_COLLECTION_GROUP)
                            .document(group.id)
                            .collection(Constants.KEY_GROUP_MEMBER)
                            .get()
                            .addOnCompleteListener(task -> {
                                for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                    if (!userID.equals(queryDocumentSnapshot.getId())) {
                                        database.collection(Constants.KEY_COLLECTION_GROUP)
                                                .document(group.id)
                                                .collection(Constants.KEY_GROUP_MEMBER)
                                                .document(userID)
                                                .set(id)
                                                .addOnSuccessListener(unused ->
                                                        database.collection(Constants.KEY_COLLECTION_USER)
                                                                .document(userID)
                                                                .collection(Constants.KEY_GROUP_ID)
                                                                .document(group.id)
                                                                .set(id));
                                    }
                                }
                            });
                    dialog.dismiss();
                    groupMember.add(userID);
                    users.clear();
                }
                showToast("Thêm những người dùng được chọn vào nhóm thành công!");
            } else {
                showToast("Hãy chọn ít nhất một người dùng cần thêm vào nhóm!");
            }

        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại xóa thành viên khỏi nhóm
    @SuppressLint("SetTextI18n")
    private void openDeleteMemberDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_delete_member);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        users.clear();
        for (String memberId : groupMember){

            database.collection(Constants.KEY_COLLECTION_USER)
                    .document(memberId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (!documentSnapshot.getId().equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                            user.position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                            users.size();
                            usersAdapter = new MultipleUserSelectionAdapter(users, this);
                            userRecyclerView.setAdapter(usersAdapter);
                            userRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });
        }

        btnApply.setOnClickListener(view -> {
            List<User> selectedUser = usersAdapter.getSelectedUser();
            if (selectedUser.size() > 0){
                for (int i = 0; i < selectedUser.size(); i++) {
                    database.collection(Constants.KEY_COLLECTION_GROUP)
                            .document(group.id)
                            .collection(Constants.KEY_GROUP_MEMBER)
                            .document(selectedUser.get(i).id)
                            .delete();
                    database.collection(Constants.KEY_COLLECTION_USER)
                            .document(selectedUser.get(i).id)
                            .collection(Constants.KEY_GROUP_ID)
                            .document(group.id)
                            .delete();
                    groupMember.remove(selectedUser.get(i).id);
                }
                dialog.dismiss();
                showToast("Xóa thành viên thành công!");
            } else {
                showToast("Hãy chọn ít nhất một thành viên cần xóa khỏi nhóm!");
            }


        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại đổi trưởng nhóm
    @SuppressLint("SetTextI18n")
    private void openChangeTeamLeadDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_change_teamlead);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);
        users.clear();
        for (String memberId : groupMember){

            database.collection(Constants.KEY_COLLECTION_USER)
                    .document(memberId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        if (!documentSnapshot.getId().equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                            user.position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            users.add(user);
                            users.size();
                            usersAdapter = new MultipleUserSelectionAdapter(users, this);
                            userRecyclerView.setAdapter(usersAdapter);
                            userRecyclerView.setVisibility(View.VISIBLE);
                        }
                    });

        }

        btnApply.setOnClickListener(view -> {
            List<User> selectedUser = usersAdapter.getSelectedUser();
            if (selectedUser.size() == 1){
                HashMap<String, Object> owner = new HashMap<>();
                owner.put(Constants.KEY_GROUP_OWNER, selectedUser.get(0).id);
                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .document(group.id)
                        .update(owner)
                        .addOnSuccessListener(unused -> showToast(selectedUser.get(0).name + R.string.isleader));
                dialog.dismiss();
                Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                startActivity(intent);
                Animatoo.animateSlideLeft(SettingGroupActivity.this);
                showToast("Đổi trưởng nhóm thành công");
            } else if (selectedUser.size() == 0){
                showToast("Hãy chọn một thành viên để làm trưởng nhóm!");
            }


        });

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại thoát nhóm
    @SuppressLint("SetTextI18n")
    private void openExitGroupDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_exit_group);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);

        btnApply.setOnClickListener(view ->
                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .document(group.id)
                        .collection(Constants.KEY_GROUP_MEMBER)
                        .document(preferenceManager.getString(Constants.KEY_USER_ID))
                        .delete()
                        .addOnCompleteListener(task -> {
                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                    .document(group.id)
                                    .collection(Constants.KEY_GROUP_MEMBER)
                                    .get()
                                    .addOnCompleteListener(task12 -> {
                                        int count = 0;
                                        for (QueryDocumentSnapshot ignored : task12.getResult()) {
                                            count++;
                                        }
                                        if (count < 2) {
                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                    .document(group.id)
                                                    .delete();

                                            database.collection(Constants.KEY_COLLECTION_USER)
                                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                                    .collection(Constants.KEY_GROUP_ID)
                                                    .document(group.id)
                                                    .delete();

                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                    .document(group.id)
                                                    .collection(Constants.KEY_GROUP_MEMBER)
                                                    .get()
                                                    .addOnCompleteListener(task121 -> {
                                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task121.getResult()) {
                                                            database.collection(Constants.KEY_COLLECTION_USER)
                                                                    .document(queryDocumentSnapshot.getId())
                                                                    .collection(Constants.KEY_GROUP_ID)
                                                                    .document(group.id)
                                                                    .delete();
                                                        }
                                                    });

                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                    .document(group.id)
                                                    .collection(Constants.KEY_GROUP_MEMBER)
                                                    .get()
                                                    .addOnCompleteListener(task1212 -> {
                                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task1212.getResult()) {
                                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                                    .document(group.id)
                                                                    .collection(Constants.KEY_GROUP_MEMBER)
                                                                    .document(queryDocumentSnapshot.getId())
                                                                    .delete();
                                                        }
                                                    });

                                            showToast(getString(R.string.ExitGroupSuccessfully));
                                            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                            startActivity(intent);
                                            Animatoo.animateSlideLeft(SettingGroupActivity.this);
                                        } else {
                                            database.collection(Constants.KEY_COLLECTION_USER)
                                                    .document(preferenceManager.getString(Constants.KEY_USER_ID))
                                                    .collection(Constants.KEY_GROUP_ID)
                                                    .document(group.id)
                                                    .delete();

                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                    .document(group.id)
                                                    .get()
                                                    .addOnCompleteListener(task1 -> {
                                                        DocumentSnapshot documentSnapshot = task1.getResult();
                                                        if (Objects.requireNonNull(documentSnapshot.getString(Constants.KEY_GROUP_OWNER)).equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                                    .document(group.id)
                                                                    .collection(Constants.KEY_GROUP_MEMBER)
                                                                    .get()
                                                                    .addOnCompleteListener(task11 -> {
                                                                        QuerySnapshot querySnapshot = task11.getResult();
                                                                        HashMap<String, Object> user = new HashMap<>();
                                                                        user.put(Constants.KEY_GROUP_OWNER, querySnapshot.getDocuments().get(0).getId());
                                                                        database.collection(Constants.KEY_COLLECTION_GROUP)
                                                                                .document(group.id)
                                                                                .update(user);
                                                                    });
                                                        }
                                                        showToast(getString(R.string.ExitGroupSuccessfully));
                                                        Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                        startActivity(intent);
                                                        Animatoo.animateSlideLeft(SettingGroupActivity.this);
                                                    });
                                        }
                                    });
                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                    .whereEqualTo(Constants.KEY_RECEIVER_ID, group.id)
                                    .get().addOnCompleteListener(task1 -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult())
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .delete();
                                    });

                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                    .whereEqualTo(Constants.KEY_RECEIVER_ID, group.id)
                                    .get().addOnCompleteListener(task1 -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task1.getResult()) {
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .delete();
                                        }
                                    });
                            groupMember.remove(preferenceManager.getString(Constants.KEY_USER_ID));
                        }));

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    // Hàm mở hộp thoại xóa nhóm
    @SuppressLint("SetTextI18n")
    private void openDeleteGroupDialog() {
        final Dialog dialog = openDialog(R.layout.layout_dialog_exit_group);
        assert dialog != null;

        //Button trong dialog
        Button no_btn = dialog.findViewById(R.id.btnClose);
        Button btnApply = dialog.findViewById(R.id.btnApply);
        TextView textTitle = dialog.findViewById(R.id.textTitle);

        textTitle.setText("Bạn có chắc chắn muốn xóa nhóm này không?");
        btnApply.setText("Xóa");

        btnApply.setOnClickListener(view ->
                database.collection(Constants.KEY_COLLECTION_GROUP)
                        .document(group.id)
                        .delete()
                        .addOnSuccessListener(unused -> {

                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                    .document(group.id)
                                    .collection(Constants.KEY_GROUP_MEMBER)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                            database.collection(Constants.KEY_COLLECTION_USER)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .collection(Constants.KEY_GROUP_ID)
                                                    .document(group.id)
                                                    .delete();
                                        }
                                    });

                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                    .document(group.id)
                                    .collection(Constants.KEY_GROUP_MEMBER)
                                    .get()
                                    .addOnCompleteListener(task -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                            database.collection(Constants.KEY_COLLECTION_GROUP)
                                                    .document(group.id)
                                                    .collection(Constants.KEY_GROUP_MEMBER)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .delete();
                                        }
                                    });

                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                    .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                                    .whereEqualTo(Constants.KEY_RECEIVER_ID, group.id)
                                    .get().addOnCompleteListener(task -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult())
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .delete();
                                    });

                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                    .whereEqualTo(Constants.KEY_RECEIVER_ID, group.id)
                                    .get().addOnCompleteListener(task -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()) {
                                            database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                                                    .document(queryDocumentSnapshot.getId())
                                                    .delete();
                                        }
                                    });

                            showToast(getString(R.string.DeleteGroup));
                            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            Animatoo.animateSlideLeft(SettingGroupActivity.this);
                            startActivity(intent);
                        }));

        no_btn.setOnClickListener(view -> dialog.dismiss());
        dialog.show();

    }

    private void getUsersNotInGroup() {
        users.clear();
        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_COMPANY_ID, preferenceManager.getString(Constants.KEY_COMPANY_ID))
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        int countFor = 0;
                        for (String memberId : groupMember){
                            if (memberId.equals(queryDocumentSnapshot.getId())){
                                countFor++;
                                break;
                            }
                        }
                        if (countFor != 0){
                            continue;
                        }
                        User user = new User();
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                        user.position = queryDocumentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                        user.phone = queryDocumentSnapshot.getString(Constants.KEY_PHONE);
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                        user.id = queryDocumentSnapshot.getId();
                        users.add(user);
                        usersAdapter = new MultipleUserSelectionAdapter(users, this);
                        usersNotInGroupRecyclerView.setAdapter(usersAdapter);
                        usersNotInGroupRecyclerView.setVisibility(View.VISIBLE);
                    }
                });

    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null){
                        Uri imageUri = result.getData().getData();
                        try {
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            imageNewGroupProfile.setImageBitmap(bitmap);
                            textNewAddImage.setVisibility(View.GONE);
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
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    // Hàm giúp mở lên một dialog
    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(SettingGroupActivity.this);
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