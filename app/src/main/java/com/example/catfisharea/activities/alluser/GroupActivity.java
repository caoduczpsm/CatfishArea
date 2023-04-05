package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityGroupBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.adapter.GroupAdapter;
import com.example.catfisharea.listeners.GroupListener;
import com.example.catfisharea.models.Group;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class GroupActivity extends AppCompatActivity implements GroupListener {
    private ActivityGroupBinding binding;
    private PreferenceManager preferenceManager;
    private GroupAdapter groupAdapter;
    private FirebaseFirestore database;
    private List<Group> groups;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Animatoo.animateSlideLeft(GroupActivity.this);
        init();
        getGroups();
        setListeners();
    }

    private void init(){
        database = FirebaseFirestore.getInstance();
        preferenceManager = new PreferenceManager(getApplicationContext());
    }

    private void setListeners() {

        binding.imageBack.setOnClickListener(v -> onBackPressed());

        binding.imageCreateGroup.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), CreateGroupActivity.class));
            Animatoo.animateSlideLeft(GroupActivity.this);
        });

        groups = new ArrayList<>();

        // Tìm kiếm tài khoản
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                groupAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                groupAdapter.getFilter().filter(inputSearch);
                return true;
            }
        });
    }

    private void getGroups() {
        loading(true);
        database.collection(Constants.KEY_COLLECTION_USER)
                .document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_GROUP_ID)
                .get()
                .addOnCompleteListener(task -> {
                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                        database.collection(Constants.KEY_COLLECTION_GROUP)
                                .get()
                                .addOnCompleteListener(task1 -> {
                                    loading(false);
                                    String currentUserId = preferenceManager.getString(Constants.KEY_USER_ID);
                                    if (task1.isSuccessful() && task1.getResult() != null) {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()) {
                                            if (currentUserId.equals(queryDocumentSnapshot1.getId())) {
                                                continue;
                                            }
                                            if (queryDocumentSnapshot.getId().equals(queryDocumentSnapshot1.getId())){
                                                Group group = new Group();
                                                group.name = queryDocumentSnapshot1.getString(Constants.KEY_GROUP_NAME);
                                                group.image = queryDocumentSnapshot1.getString(Constants.KEY_GROUP_IMAGE);
                                                group.description = queryDocumentSnapshot1.getString(Constants.KEY_GROUP_DESCRIPTION);
                                                group.token = queryDocumentSnapshot1.getString(Constants.KEY_FCM_TOKEN);
                                                group.id = queryDocumentSnapshot1.getId();
                                                groups.add(group);
                                            }

                                        }
                                        if (groups.size() > 0) {
                                            groupAdapter = new GroupAdapter(groups, this);
                                            binding.userRecyclerView.setAdapter(groupAdapter);
                                            binding.userRecyclerView.setVisibility(View.VISIBLE);
                                        } else {
                                            loading(false);
                                            showErrorMessage();
                                        }
                                    } else {
                                        loading(false);
                                        showErrorMessage();
                                    }
                                });
                    }
                });


    }


    private void showErrorMessage() {
        binding.textErrorMessage.setText(String.format("%s", "Bạn chưa tham gia nhóm nào!"));
        binding.textErrorMessage.setVisibility(View.VISIBLE);
    }

    private void loading(Boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
        } else {
            binding.progressBar.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onGroupClicker(Group group) {
        Group groupIntent = new Group();
        groupIntent.id = group.id;
        groupIntent.image = group.image;
        groupIntent.name = group.name;
        //group = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
        database.collection(Constants.KEY_COLLECTION_GROUP).document(group.id)
                .collection(Constants.KEY_GROUP_MEMBER).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> members = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        String member = queryDocumentSnapshot.getId();
                        members.add(member);
                    }
                    groupIntent.member = members;
                    Intent intent = new Intent(getApplicationContext(), ChatGroupActivity.class);
                    intent.putExtra(Constants.KEY_GROUP, groupIntent);
                    startActivity(intent);
                    Animatoo.animateSlideLeft(GroupActivity.this);
                });
    }

    @Override
    protected void attachBaseContext(Context newBase) {

        final Configuration override = new Configuration(newBase.getResources().getConfiguration());
        override.fontScale = 1.0f;
        applyOverrideConfiguration(override);

        super.attachBaseContext(newBase);
    }
}