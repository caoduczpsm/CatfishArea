package com.example.catfisharea.adapter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.example.catfisharea.listeners.PickUserListener;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserPickerAdapter extends RecyclerView.Adapter<UserPickerAdapter.UserViewHolder> {
    private List<User> mUser;
    private PickUserListener mListener;

    public UserPickerAdapter(List<User> mUser, PickUserListener mListener) {
        this.mUser = mUser;
        this.mListener = mListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserViewHolder holder = new UserViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.custom_list_account,
                        parent,
                        false)
        );
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        holder.setUser(mUser.get(position));
    }

    @Override
    public int getItemCount() {
        return mUser.size();
    }

    class UserViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout layoutUserSelection;
        CircleImageView imageProfile;
        TextView textName, textPosition, textPhone;
        ImageView imageSelected;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            layoutUserSelection = itemView.findViewById(R.id.layoutUserSelection);
            imageProfile = itemView.findViewById(R.id.imageProfile);
            textName = itemView.findViewById(R.id.textName);
            textPhone = itemView.findViewById(R.id.textPhone);
            textPosition = itemView.findViewById(R.id.textPosition);
            imageSelected = itemView.findViewById(R.id.imageSelected);
            imageSelected.setVisibility(View.INVISIBLE);

        }

        public void setUser(User user) {
            imageProfile.setImageBitmap(getUserImage(user.image));
//            textName.setText(user.name);
            if (user.name.equals("")) {
                textName.setText("Chưa cập nhật tên!");
            } else {
                textName.setText(user.name);
            }
            if (user.phone.length() != 10) {
                textPhone.setText("Chưa cập nhật số điện thoại!");
            } else {
                textPhone.setText(user.phone);
            }

            switch (user.position) {
                case Constants.KEY_ADMIN:
                    textPosition.setText("Admin");
                    break;
                case Constants.KEY_ACCOUNTANT:
                    textPosition.setText("Kế Toán");
                    break;
                case Constants.KEY_REGIONAL_CHIEF:
                    textPosition.setText("Trưởng Vùng");
                    break;
                case Constants.KEY_DIRECTOR:
                    textPosition.setText("Trưởng Khu");
                    break;
                default:
                    textPosition.setText("Công Nhân");
                    break;
            }
            layoutUserSelection.setOnClickListener(view -> {
                mListener.onClickUser(user);
            });
        }

        private Bitmap getUserImage(String encodedImage) {
            byte[] bytes = new byte[0];
            if (encodedImage != null) {
                bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            }

            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        }
    }
}
