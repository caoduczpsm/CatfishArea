package com.example.catfisharea.activities.alluser;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.view.View;
import android.widget.Toast;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ActivityChatGroupBinding;
import com.blogspot.atifsoftwares.animatoolib.Animatoo;
import com.example.catfisharea.activities.BaseActivity;
import com.example.catfisharea.adapter.ChatGroupAdapter;
import com.example.catfisharea.listeners.MessageListener;
import com.example.catfisharea.models.ChatMessage;
import com.example.catfisharea.models.Group;
import com.example.catfisharea.network.ApiClient;
import com.example.catfisharea.network.ApiService;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatGroupActivity extends BaseActivity implements MessageListener {
    private ActivityChatGroupBinding binding;
    private Group receiverUser;
    private List<ChatMessage> chatMessages;
    private ChatGroupAdapter chatAdapter;
    private PreferenceManager preferenceManager;
    private FirebaseFirestore database;
    private String conversationId = null;
    private Boolean isReceiverAvailable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatGroupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        loadReceiverDetails();
        init();

        readMessages();
        //listenMessages();

    }


    private void init() {
        preferenceManager = new PreferenceManager(getApplicationContext());
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatGroupAdapter(
                chatMessages,
                getBitmapFromEncodedString(receiverUser.image),
                preferenceManager.getString(Constants.KEY_USER_ID), this
        );
        binding.chatRecyclerView.setAdapter(chatAdapter);
        binding.chatRecyclerView.setItemAnimator(null);
        database = FirebaseFirestore.getInstance();

        binding.imageBack.setOnClickListener(view -> onBackPressed());

    }

    @SuppressLint("NotifyDataSetChanged")
    private void readMessages() {

        ArrayList<String> member = (ArrayList<String>) receiverUser.member;

        for (String item : member) {
            if (item.equals(preferenceManager.getString(Constants.KEY_USER_ID))) {
                database.collection(Constants.KEY_COLLECTION_CHAT_GROUPS)
                        .get()
                        .addOnCompleteListener(task ->
                                database.collection(Constants.KEY_COLLECTION_CHAT_GROUPS)
                                        .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverUser.id)
                                        .addSnapshotListener(eventListener));
            }
        }
    }

    private void sendMessage(String text) {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_MESSAGE_TYPE, Constants.MESSAGE_TEXT);
        message.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
        message.put(Constants.KEY_MESSAGE, text);
        message.put(Constants.KEY_TIMESTAMP, new Date());
        message.put(Constants.KEY_SEEN_MESSAGE, false);
        //message.put(Constants.KEY_SEEN_TIME, new Date());
        database.collection(Constants.KEY_COLLECTION_CHAT_GROUPS).add(message);

        if (conversationId != null) {
            updateConversion(text);
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_MESSAGE_TYPE, Constants.MESSAGE_TEXT);
            conversion.put(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferenceManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, receiverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, receiverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, receiverUser.image);
            conversion.put(Constants.KEY_LAST_MESSAGE, text);
            conversion.put(Constants.KEY_TIMESTAMP, new Date());
            addConversion(conversion);
        }
        if (!isReceiverAvailable) {
            try {

                JSONArray tokens = new JSONArray();
                tokens.put(receiverUser.token);

                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferenceManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferenceManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, text);

                JSONObject body = new JSONObject();
                body.put(Constants.REMOTE_MSG_DATA, data);
                body.put(Constants.REMOTE_MSG_REGISTRATION_IDS, tokens);

                sendNotification(body.toString());

            } catch (Exception e) {
                //showToast(e.getMessage());
            }
        }
        binding.inputeMessage.setText(null);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void sendNotification(String messageBody) {
        ApiClient.getClient().create(ApiService.class).sendMessage(
                Constants.getRemoteMsgHeaders(),
                messageBody
        ).enqueue(new Callback<String>() {
            @Override
            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                if (response.isSuccessful()) {
                    try {
                        if (response.body() != null) {
                            JSONObject responseJson = new JSONObject(response.body());
                            JSONArray results = responseJson.getJSONArray("results");
                            if (responseJson.getInt("failure") == 1) {
                                JSONObject error = (JSONObject) results.get(0);
                                showToast(error.getString("error"));
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    //showToast("Notification sent successfully!");
                } else {
                    showToast("Error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
                showToast(t.getMessage());
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {
        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatMessages.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.id = documentChange.getDocument().getId();
                    chatMessage.type = documentChange.getDocument().getString(Constants.KEY_MESSAGE_TYPE);
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.receiverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP));
                    chatMessage.dataObject = documentChange.getDocument().getDate(Constants.KEY_TIMESTAMP);
                    //chatMessage.seenTime = getReadableDateTime(documentChange.getDocument().getDate(Constants.KEY_SEEN_TIME));

                    if (Objects.equals(chatMessage.senderId, preferenceManager.getString(Constants.KEY_USER_ID)))
                        chatMessage.model = "sender";
                    else chatMessage.model = "receiver";
                    chatMessage.isSeen = documentChange.getDocument().getBoolean(Constants.KEY_SEEN_MESSAGE);
                    chatMessages.add(chatMessage);
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                Collections.sort(chatMessages, Comparator.comparing(obj -> obj.dataObject));
            }
            if (count == 0) {
                chatAdapter.notifyItemRangeInserted(0, chatAdapter.getItemCount());
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatMessages.size(), chatMessages.size());
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size() - 1);
            }
        }
        if (conversationId == null) {
            checkForConversion();
        }
    };

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_GROUP).document(
                receiverUser.id
        ).addSnapshotListener(this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                receiverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if (receiverUser.image == null) {
                    receiverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(receiverUser.image));
                    chatAdapter.notifyItemRangeInserted(0, chatMessages.size());
                }
            }

        });
    }

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        receiverUser = (Group) getIntent().getSerializableExtra(Constants.KEY_GROUP);
        binding.textName.setText(receiverUser.name);
    }

    private void setListeners() {
        binding.imageBack.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), ConversationActivity.class);
            startActivity(intent);
            Animatoo.animateSlideLeft(ChatGroupActivity.this);
        });

        binding.inputeMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                setBtnVisible(!binding.inputeMessage.getText().toString().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        binding.layoutSend.setOnClickListener(v -> sendMessage(binding.inputeMessage.getText().toString()));

        binding.textName.setOnClickListener(view -> {
            Intent intent = new Intent(getApplicationContext(), SettingGroupActivity.class);
            intent.putExtra(Constants.KEY_GROUP, receiverUser);
            startActivity(intent);
            Animatoo.animateSlideLeft(ChatGroupActivity.this);
        });

    }

    private void addConversion(HashMap<String, Object> conversion) {

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversationId = documentReference.getId());

    }

    private void updateConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversationId);
        documentReference.update(
                Constants.KEY_MESSAGE_TYPE, Constants.MESSAGE_TEXT,
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIMESTAMP, new Date()
        );
    }

    private void checkForConversion() {
        if (chatMessages.size() > 0) {
            checkForConversionRemotely(
                    receiverUser.id
            );

        }
    }

    private String getReadableDateTime(Date date) {
        return new SimpleDateFormat("dd MMMM, yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void checkForConversionRemotely(String receiverId) {

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);

    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversationId = documentSnapshot.getId();
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }

    private void setBtnVisible(boolean visible) {
        if (visible) {
            binding.layoutImage.setVisibility(View.INVISIBLE);
            binding.layoutAttact.setVisibility(View.INVISIBLE);
            binding.layoutSend.setVisibility(View.VISIBLE);
        } else {
            binding.layoutImage.setVisibility(View.VISIBLE);
            binding.layoutAttact.setVisibility(View.VISIBLE);
            binding.layoutSend.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onMessageSelection(Boolean isSelected, int position, List<ChatMessage> chatMessages, ChatMessage chatMessage) {

    }

}