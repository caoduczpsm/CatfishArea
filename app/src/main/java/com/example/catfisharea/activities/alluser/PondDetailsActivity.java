package com.example.catfisharea.activities.alluser;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Toast;

import com.android.app.catfisharea.databinding.ActivityPondDetailsBinding;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class PondDetailsActivity extends BaseActivity {

    private Pond pond;
    private ActivityPondDetailsBinding binding;
    private FirebaseFirestore database;


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

    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}