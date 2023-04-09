package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ItemContainerCommentBinding;
import com.example.catfisharea.models.Comment;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

import java.util.List;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {

    private final List<Comment> comments;

    public CommentAdapter(List<Comment> comments) {
        this.comments = comments;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerCommentBinding itemContainerCommentBinding = ItemContainerCommentBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false
        );

        return new CommentViewHolder(itemContainerCommentBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        holder.setUserData(comments.get(position));
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    class CommentViewHolder extends RecyclerView.ViewHolder{

        ItemContainerCommentBinding binding;
        CommentViewHolder(ItemContainerCommentBinding itemContainerCommentBinding){
            super(itemContainerCommentBinding.getRoot());
            binding = itemContainerCommentBinding;
        }

        @SuppressLint("SetTextI18n")
        void setUserData(Comment comment){
            PreferenceManager preferenceManager = new PreferenceManager(this.itemView.getContext());
            binding.textName.setText(comment.userCommentName);
            if (comment.userCommentPosition.equals(Constants.KEY_ADMIN))
                binding.textPosition.setText("Admin");
            if (comment.userCommentPosition.equals(Constants.KEY_ACCOUNTANT))
                binding.textPosition.setText("Kế Toán");
            if (comment.userCommentPosition.equals(Constants.KEY_REGIONAL_CHIEF))
                binding.textPosition.setText("Trưởng Vùng");
            if (comment.userCommentPosition.equals(Constants.KEY_DIRECTOR))
                binding.textPosition.setText("Trưởng Khu");
            if (comment.userCommentPosition.equals(Constants.KEY_WORKER))
                binding.textPosition.setText("Công Nhân");
            binding.textContent.setText(comment.content);
            binding.imageProfile.setImageBitmap(getUserImage(comment.userCommentImage));
            binding.textDateTime.setText(comment.dateTime);
            if (comment.userCommentId.equals(preferenceManager.getString(Constants.KEY_USER_ID))){
                binding.userCommentLayout.setBackgroundResource(R.drawable.background_user_selected);
            }
        }

    }

    private Bitmap getUserImage(String encodedImage){
        byte[] bytes = new byte[0];
        if (encodedImage != null){
            bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

}
