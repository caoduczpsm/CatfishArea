package com.example.catfisharea.activities.worker;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.app.catfisharea.databinding.ActivityWorkerHomeBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.activities.alluser.ConferenceActivity;
import com.example.catfisharea.activities.alluser.ConversationActivity;
import com.example.catfisharea.activities.alluser.LoginActivity;
import com.example.catfisharea.activities.director.RequestManagementActivity;
import com.example.catfisharea.activities.director.TaskManagerActivity;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.List;


public class WorkerHomeActivity extends BaseActivity {

    private ActivityWorkerHomeBinding mBinding;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mBinding = ActivityWorkerHomeBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        init();
        setListener();
    }

    private void init(){
        preferenceManager = new PreferenceManager(this);
        database = FirebaseFirestore.getInstance();

        database.collection(Constants.KEY_COLLECTION_FIXED_TASK)
                .whereEqualTo(Constants.KEY_TASK_TITLE, Constants.KEY_FIXED_TASK_FEED_FISH)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){

                        List<String> receiverFeedFishTask = (List<String>) queryDocumentSnapshot.get(Constants.KEY_RECEIVER_ID);

                        assert receiverFeedFishTask != null;
                        if (receiverFeedFishTask.contains(preferenceManager.getString(Constants.KEY_USER_ID))){
                            mBinding.layoutHome.cardFood.setVisibility(View.VISIBLE);
                        } else {
                            mBinding.layoutHome.cardFood.setVisibility(View.GONE);
                        }

                    }
                });
    }

    private void setListener() {
        mBinding.toolbaWorkerHome.setNavigationOnClickListener(view -> onBackPressed());

        mBinding.layoutControlWorkerHome.layoutTask.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), TaskManagerActivity.class)));

        mBinding.layoutControlWorkerHome.layoutRequest.setOnClickListener(view -> {
            Intent intent = new Intent(this, RequestManagementActivity.class);
            startActivity(intent);
        });

        mBinding.toolbaWorkerHome.setTitle(preferenceManager.getString(Constants.KEY_NAME));

        mBinding.imageConference.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ConferenceActivity.class)));

        mBinding.imageChat.setOnClickListener(view -> startActivity(new Intent(getApplicationContext(), ConversationActivity.class)));

        mBinding.imageLogout.setOnClickListener(view -> logOut());
    }

    private void setUpHomePage() {
        String pondId = preferenceManager.getString(Constants.KEY_POND_ID);
        assert pondId != null;

    }

    private Dialog openDialog(int layout) {
        final Dialog dialog = new Dialog(this);
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

    // Hàm đăng xuất
    public void logOut() {

        showToast("Đang đăng xuất...");

        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );

        // Xóa bộ nhớ tạm
        preferenceManager.clear();

        // Xóa FCM TOKEN
        HashMap<String, Object> updates = new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    // Xóa bộ nhớ tạm
                    preferenceManager.clear();
                    // Chuyển sang màn hình đăng nhập
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> showToast("Không thể đăng xuất..."));
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}