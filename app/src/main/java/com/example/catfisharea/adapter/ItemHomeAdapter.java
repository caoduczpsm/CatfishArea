package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemRecyclerViewItemHomeBinding;
import com.example.catfisharea.models.RegionModel;

import java.util.List;

public class ItemHomeAdapter extends RecyclerView.Adapter<ItemHomeAdapter.ItemHomeViewHolder> {

    private List<RegionModel> mReginon;

    public ItemHomeAdapter(List<RegionModel> mReginon) {
        this.mReginon = mReginon;
    }

    @NonNull
    @Override
    public ItemHomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemRecyclerViewItemHomeBinding mBinding = LayoutItemRecyclerViewItemHomeBinding
                .inflate(LayoutInflater.from(
                        parent.getContext()),
                        parent,
                        false
                );
        return new ItemHomeViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemHomeViewHolder holder, int position) {
        RegionModel regionModel = mReginon.get(position);
        holder.setData(regionModel);
    }

    @Override
    public int getItemCount() {
        return mReginon.size();
    }

    class ItemHomeViewHolder extends RecyclerView.ViewHolder {
        private LayoutItemRecyclerViewItemHomeBinding mBinding;

        public ItemHomeViewHolder(LayoutItemRecyclerViewItemHomeBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(RegionModel regionModel) {
            mBinding.nameItem.setText(regionModel.getName());
        }
    }
}
