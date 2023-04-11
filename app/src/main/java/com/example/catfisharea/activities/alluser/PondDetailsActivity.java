package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityPondDetailsBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.UsersAdapter;
import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PondDetailsActivity extends BaseActivity implements UserListener {

    private Pond pond;
    private ActivityPondDetailsBinding binding;
    private FirebaseFirestore database;
    private UsersAdapter usersAdapter;
    private List<User> users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPondDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
        setListeners();
        showToast(pond.getId());
    }

    @SuppressLint("SetTextI18n")
    void init() {

        database = FirebaseFirestore.getInstance();

        pond = (Pond) getIntent().getSerializableExtra(Constants.KEY_POND);

        users = new ArrayList<>();
        usersAdapter = new UsersAdapter(users, this);

        binding.textName.setText(pond.getName());
        binding.textAcreage.setText(pond.getAcreage() + " (m2)");

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_TYPE_ACCOUNT, Constants.KEY_WORKER)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    int countWorker = 0;
                    for (QueryDocumentSnapshot ignored : task.getResult()){
                        countWorker++;
                    }
                    binding.textNumOfWorker.setText(countWorker + " người");
                });

    }

    private void setListeners() {
        binding.layoutShowWorker.setOnClickListener(view -> openShowWorkerDialog());
    }

    @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
    private void openShowWorkerDialog(){
        Dialog dialog = openDialog(R.layout.layout_dialog_show_worker_in_pond);
        assert dialog != null;

        TextView textTitle = dialog.findViewById(R.id.textTitle);
        textTitle.setText("Danh sách công nhân làm việc tại ao " + pond.getName());

        Button btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(view -> dialog.dismiss());

        RecyclerView userRecyclerView = dialog.findViewById(R.id.userRecyclerView);

        userRecyclerView.setAdapter(usersAdapter);

        users.clear();

        database.collection(Constants.KEY_COLLECTION_USER)
                .whereEqualTo(Constants.KEY_POND_ID, pond.getId())
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
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
                });
        dialog.show();
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

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUserClicker(User user) {

    }
}