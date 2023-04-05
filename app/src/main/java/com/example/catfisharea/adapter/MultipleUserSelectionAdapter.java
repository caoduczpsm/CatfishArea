package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.example.catfisharea.listeners.MultipleListener;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MultipleUserSelectionAdapter extends RecyclerView.Adapter<MultipleUserSelectionAdapter.MultipleUserSelectionViewHolder> implements Filterable {

    private List<User> users;
    private final List<User> usersOld;
    private final MultipleListener multipleUserListener;


    public MultipleUserSelectionAdapter(List<User> users, MultipleListener multipleUserListener) {
        this.users = users;
        this.usersOld = users;
        this.multipleUserListener = multipleUserListener;
    }

    // Đánh dấu chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllSelected(List<User> users){
        this.users = users;
        for (User user : users)
            if (!user.isSelected)
                user.isSelected = true;
        notifyDataSetChanged();
    }

    // Bỏ chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllUnSelected(List<User> users){
        this.users = users;
        for (User user : users)
            if (user.isSelected)
                user.isSelected = false;
        notifyDataSetChanged();
    }

    // Bỏ chọn một số tài khoản khi đã thực thi xong yêu cầu và thay đổi quyền tài khoản sau khi người dùng bấm thay đổi
    @SuppressLint("NotifyDataSetChanged")
    public void setUserUnSelected(List<User> users){
        for (User user : users){
            if (user.isSelected)
                user.isSelected = false;
        }
        this.users = users;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MultipleUserSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MultipleUserSelectionViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.custom_list_account,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleUserSelectionViewHolder holder, int position) {
        holder.bindUserSelection(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public List<User> getSelectedUser(){
        List<User> selectedUser = new ArrayList<>();
        for (User user : users){
            if (user.isSelected){
                selectedUser.add(user);
            }
        }
        return selectedUser;
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
                    if (outputSearchUsers.size() != 0) {
                        users = outputSearchUsers;
                    }
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

    class MultipleUserSelectionViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout layoutUserSelection;
        CircleImageView imageProfile;
        TextView textName, textPosition, textPhone;
        ImageView imageSelected;
        View viewBackground;

        MultipleUserSelectionViewHolder(@NonNull View itemView){
            super(itemView);
            layoutUserSelection = itemView.findViewById(R.id.layoutUserSelection);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textName = itemView.findViewById(R.id.textName);
            textPhone = itemView.findViewById(R.id.textPhone);
            textPosition = itemView.findViewById(R.id.textPosition);
            imageSelected = itemView.findViewById(R.id.imageSelected);
            viewBackground = itemView.findViewById(R.id.viewBackground);
        }

        @SuppressLint("SetTextI18n")
        void bindUserSelection(final User user){
            imageProfile.setImageBitmap(getUserImage(user.image));
            if (user.name.equals("")){
                textName.setText("Chưa cập nhật tên!");
            } else {
                textName.setText(user.name);
            }
            if (user.phone.length() != 10){
                textPhone.setText("Chưa cập nhật số điện thoại!");
            } else {
                textPhone.setText(user.phone);
            }

            if (user.position.equals(Constants.KEY_ADMIN))
                textPosition.setText("Admin");
            if (user.position.equals(Constants.KEY_ACCOUNTANT))
                textPosition.setText("Kế Toán");
            if (user.position.equals(Constants.KEY_REGIONAL_CHIEF))
                textPosition.setText("Trưởng Vùng");
            if (user.position.equals(Constants.KEY_DIRECTOR))
                textPosition.setText("Trưởng Khu");
            if (user.position.equals(Constants.KEY_WORKER))
                textPosition.setText("Công Nhân");


            //PreferenceManager preferenceManager = new PreferenceManager(itemView.getContext());
            if (user.isSelected){
                viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                imageSelected.setVisibility(View.VISIBLE);
            } else {
                viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                imageSelected.setVisibility(View.GONE);
            }
            layoutUserSelection.setOnClickListener(view -> {
                if (user.isSelected){
                    viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                    imageSelected.setVisibility(View.GONE);
                    user.isSelected = false;
                    if (getSelectedUser().size() == 0){
                        multipleUserListener.onMultipleUserSelection(false);
                    }
                } else{
                    viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                    imageSelected.setVisibility(View.VISIBLE);
                    user.isSelected = true;
                    multipleUserListener.onMultipleUserSelection(true);
                }
            });
        }

        private Bitmap getUserImage(String encodedImage){
            byte[] bytes = new byte[0];
            if (encodedImage != null){
                bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            }

            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
        }

    }
}

