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

import com.android.app.catfisharea.databinding.ItemContainerUserBinding;

import com.example.catfisharea.listeners.UserListener;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;

import java.util.ArrayList;
import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder> implements Filterable {

    private List<User> users;
    private final List<User> usersOld;
    private final UserListener userListener;

    public UsersAdapter(List<User> users, UserListener userListener) {
        this.users = users;
        this.usersOld = users;
        this.userListener = userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false
        );

        return new UserViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    // Tìm kiếm user
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchInput = charSequence.toString();
                if (searchInput.isEmpty()){
                    users = usersOld;
                } else {
                    List<User> outputSearchUsers  = new ArrayList<>();
                    for (User user : usersOld){
                        if (user.name.toLowerCase().contains(searchInput.toLowerCase())){
                            outputSearchUsers.add(user);
                        }
                    }
                    users = outputSearchUsers;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = users;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                users = (List<User>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class UserViewHolder extends RecyclerView.ViewHolder{

        ItemContainerUserBinding binding;
        UserViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        @SuppressLint("SetTextI18n")
        void setUserData(User user){

            if (user.name.equals("")){
                binding.textName.setText("Chưa cập nhật tên!");
            } else {
                binding.textName.setText(user.name);
            }
            if (user.phone.length() != 10){
                binding.textPhone.setText("Chưa cập nhật số điện thoại!");
            } else {
                binding.textPhone.setText(user.phone);
            }

            if (user.position.equals(Constants.KEY_ADMIN))
                binding.textPosition.setText("Admin");
            if (user.position.equals(Constants.KEY_ACCOUNTANT))
                binding.textPosition.setText("Kế Toán");
            if (user.position.equals(Constants.KEY_REGIONAL_CHIEF))
                binding.textPosition.setText("Trưởng Vùng");
            if (user.position.equals(Constants.KEY_DIRECTOR))
                binding.textPosition.setText("Trưởng Khu");
            if (user.position.equals(Constants.KEY_WORKER))
                binding.textPosition.setText("Công Nhân");
            binding.imageProfile.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicker(user));
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
