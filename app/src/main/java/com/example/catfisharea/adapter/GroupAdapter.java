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
import com.example.catfisharea.listeners.GroupListener;
import com.example.catfisharea.models.Group;

import java.util.ArrayList;
import java.util.List;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.GroupViewHolder> implements Filterable {

    private List<Group> groups;
    private final List<Group> groupsOld;
    private final GroupListener groupListener;

    public GroupAdapter(List<Group> groups, GroupListener groupListener) {
        this.groups = groups;
        this.groupsOld = groups;
        this.groupListener = groupListener;
    }

    @NonNull
    @Override
    public GroupViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemContainerUserBinding itemContainerUserBinding = ItemContainerUserBinding.inflate(
                LayoutInflater.from(parent.getContext()),parent,false
        );

        return new GroupViewHolder(itemContainerUserBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupViewHolder holder, int position) {
        holder.setUserData(groups.get(position));
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    // Tìm kiếm user
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchInput = charSequence.toString();
                if (searchInput.isEmpty()){
                    groups = groupsOld;
                } else {
                    List<Group> outputSearchGroups  = new ArrayList<>();
                    for (Group group : groupsOld){
                        if (group.name.toLowerCase().contains(searchInput.toLowerCase())){
                            outputSearchGroups.add(group);
                        }
                    }
                    groups = outputSearchGroups;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = groups;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                groups = (List<Group>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }


    class GroupViewHolder extends RecyclerView.ViewHolder{

        ItemContainerUserBinding binding;
        GroupViewHolder(ItemContainerUserBinding itemContainerUserBinding){
            super(itemContainerUserBinding.getRoot());
            binding = itemContainerUserBinding;
        }

        @SuppressLint("SetTextI18n")
        void setUserData(Group group){


            binding.imageProfile.setImageBitmap(getGroupImage(group.image));
            binding.textPhone.setText(group.description);
            binding.textName.setText(group.name);
            binding.getRoot().setOnClickListener(v -> groupListener.onGroupClicker(group));
        }

    }

    private Bitmap getGroupImage(String encodedImage){
        byte[] bytes = new byte[0];
        if (encodedImage != null){
            bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        }
        return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
    }

}
