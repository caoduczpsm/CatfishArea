package com.example.catfisharea.activities.alluser;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityConversationBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.RecentConversationAdapter;
import com.example.catfisharea.listeners.ConversionListener;
import com.example.catfisharea.models.ChatMessage;
import com.example.catfisharea.models.Group;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ConversationActivity extends BaseActivity implements ConversionListener {
    private ActivityConversationBinding binding;
    private PreferenceManager preferenceManager;
    private List<ChatMessage> conversations;
    private RecentConversationAdapter conversationAdapter;
    private FirebaseFirestore database;
    private boolean isFABOpen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityConversationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());
        init();

        getToken();
        setListeners();
        listenConversation();
    }

    private void init(){
        conversations = new ArrayList<>();
        conversationAdapter = new RecentConversationAdapter(conversations, this);
        binding.conversationRecyclerView.setAdapter(conversationAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void setListeners(){

        binding.fabNewChat.setOnClickListener(v -> {
            if(!isFABOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });

        binding.fabUser.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
            Animatoo.animateSlideLeft(ConversationActivity.this);
        });

        binding.fabGroup.setOnClickListener(view -> {
            startActivity(new Intent(getApplicationContext(), GroupActivity.class));
            Animatoo.animateSlideLeft(ConversationActivity.this);
        });

        // Tìm kiếm tài khoản
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                conversationAdapter.getFilter().filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String inputSearch) {
                conversationAdapter.getFilter().filter(inputSearch);
                return true;
            }
        });

        binding.imageBack.setOnClickListener(view -> onBackPressed());

    }

    private void showFABMenu(){
        isFABOpen=true;
        binding.fabUser.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        binding.fabGroup.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        binding.textFabUser.setVisibility(View.VISIBLE);
        binding.textFabGroup.setVisibility(View.VISIBLE);
        binding.textFabUser.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        binding.textFabGroup.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        binding.fabUser.animate().translationY(0);
        binding.fabGroup.animate().translationY(0);
        binding.textFabUser.animate().translationY(0);
        binding.textFabGroup.animate().translationY(0);
        binding.textFabUser.setVisibility(View.GONE);
        binding.textFabGroup.setVisibility(View.GONE);
    }

    private void listenConversation() {
        conversations.clear();
        database.collection(Constants.KEY_COLLECTION_USER).document(preferenceManager.getString(Constants.KEY_USER_ID))
                .collection(Constants.KEY_GROUP_ID).get().addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<String> groupIds = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        groupIds.add(queryDocumentSnapshot.getId());
                    }
                    groupIds.add("1");

                    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                            .whereNotIn(Constants.KEY_RECEIVER_ID, groupIds)
                            .addSnapshotListener(eventListener);

                    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
                            .whereNotIn(Constants.KEY_RECEIVER_ID, groupIds)
                            .addSnapshotListener(eventListener);

                    database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                            .whereIn(Constants.KEY_RECEIVER_ID, groupIds)
                            .addSnapshotListener(eventListener);
                });

    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }

        if (value != null) {
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage = new ChatMessage();

                    chatMessage.senderId = senderId;
                    chatMessage.receiverId = receiverId;

                    if (preferenceManager.getString(Constants.KEY_USER_ID).equals(senderId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    } else if (preferenceManager.getString(Constants.KEY_USER_ID).equals(receiverId)) {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    } else {
                        chatMessage.conversionImage = documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName = documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }

                    chatMessage.type = documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    conversations.add(chatMessage);
                } else if (documentChange.getType() == DocumentChange.Type.MODIFIED) {
                    for (int i = 0; i < conversations.size(); i++) {
                        String senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        //Log.d("BBB", "+++" + receiverId);
                        if (conversations.get(i).senderId.equals(senderId) && conversations.get(i).receiverId.equals(receiverId)) {
                            conversations.get(i).type = documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE);
                            conversations.get(i).message = documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations, (obj1, obj2) -> obj2.dataObject.compareTo(obj1.dataObject));
            conversationAdapter.notifyDataSetChanged();
            binding.conversationRecyclerView.smoothScrollToPosition(0);
            binding.conversationRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN, token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference = database.collection(Constants.KEY_COLLECTION_USER).document(
                preferenceManager.getString(Constants.KEY_USER_ID)
        );
        documentReference.update(Constants.KEY_FCM_TOKEN, token)

                .addOnFailureListener(e -> showToast("Unable to update token"));
    }

    @Override
    public void onConversionClicker(User user) {
        Intent intent = new Intent(getApplicationContext(), ChatActivity.class);
        intent.putExtra(Constants.KEY_USER, user);
        startActivity(intent);
        Animatoo.animateSlideLeft(ConversationActivity.this);
    }

    @Override
    public void onConversionClicker(Group group) {
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
                    Animatoo.animateSlideLeft(ConversationActivity.this);
                });
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}