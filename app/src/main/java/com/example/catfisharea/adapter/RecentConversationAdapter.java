package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.ItemContainerRecentConversationBinding;
import com.example.catfisharea.listeners.ConversionListener;
import com.example.catfisharea.models.ChatMessage;
import com.example.catfisharea.models.Group;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class RecentConversationAdapter extends RecyclerView.Adapter<RecentConversationAdapter.ConversionViewHolder> implements Filterable {

    private List<ChatMessage> chatMessages;
    private final List<ChatMessage> chatMessagesOld;
    private final ConversionListener conversionListener;

    public RecentConversationAdapter(List<ChatMessage> chatMessages, ConversionListener conversionListener) {
        this.chatMessages = chatMessages;
        this.chatMessagesOld = chatMessages;
        this.conversionListener = conversionListener;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(
                ItemContainerRecentConversationBinding.inflate(
                        LayoutInflater.from(parent.getContext()),
                        parent,
                        false
                )
        );
    }

    // Tìm kiếm conversation
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchInput = charSequence.toString();
                if (searchInput.isEmpty()){
                    chatMessages = chatMessagesOld;
                } else {
                    List<ChatMessage> outputSearchConversation  = new ArrayList<>();
                    for (ChatMessage chatMessage : chatMessagesOld){
                        if (chatMessage.conversionName.toLowerCase().contains(searchInput.toLowerCase())){
                            outputSearchConversation.add(chatMessage);
                        }
                    }
                    chatMessages = outputSearchConversation;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = chatMessages;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                chatMessages = (List<ChatMessage>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    class ConversionViewHolder extends RecyclerView.ViewHolder {

        ItemContainerRecentConversationBinding binding;

        ConversionViewHolder(ItemContainerRecentConversationBinding itemContainerRecentConversionBinding) {
            super(itemContainerRecentConversionBinding.getRoot());
            binding = itemContainerRecentConversionBinding;
        }

        void setData(ChatMessage chatMessage) {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.textName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);

            binding.getRoot().setOnClickListener(view -> {
                FirebaseFirestore database = FirebaseFirestore.getInstance();
                database.collection(Constants.KEY_COLLECTION_GROUP).get().addOnSuccessListener(queryDocumentSnapshots -> {


                    ArrayList<String> groupIds = new ArrayList<>();
                    for (QueryDocumentSnapshot queryDocumentSnapshot : queryDocumentSnapshots) {
                        groupIds.add(queryDocumentSnapshot.getId());
                    }
                    groupIds.add("1");

                    if (groupIds.contains(chatMessage.receiverId)) {
                        Group group = new Group();
                        group.id = chatMessage.receiverId;
                        group.name = chatMessage.conversionName;
                        group.image = chatMessage.conversionImage;
                        conversionListener.onConversionClicker(group);

                    } else {
                        User user = new User();
                        user.id = chatMessage.conversionId;
                        user.name = chatMessage.conversionName;
                        user.image = chatMessage.conversionImage;
                        database.collection(Constants.KEY_COLLECTION_USER).document(user.id).get().addOnSuccessListener(
                                documentSnapshot -> {
                                    user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                                    conversionListener.onConversionClicker(user);
                                }
                        );
                    }

                });


            });
        }

        private Bitmap getConversionImage(String encodedImage) {
            if (encodedImage != null) {
                byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } else {
                return null;
            }
        }

    }

}