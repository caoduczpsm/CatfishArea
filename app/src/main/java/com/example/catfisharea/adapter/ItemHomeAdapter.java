package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemRecyclerViewItemHomeBinding;
import com.example.catfisharea.listeners.PondListener;
import com.example.catfisharea.models.RegionModel;

import java.util.List;

public class ItemHomeAdapter extends RecyclerView.Adapter<ItemHomeAdapter.ItemHomeViewHolder> {

    private final List<RegionModel> mRegion;
    private final PondListener pondListener;

    public ItemHomeAdapter(List<RegionModel> mRegion, PondListener pondListener) {
        this.mRegion = mRegion;
        this.pondListener = pondListener;
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
        RegionModel regionModel = mRegion.get(position);
        holder.setData(regionModel);
    }

    @Override
    public int getItemCount() {
        return mRegion.size();
    }

    class ItemHomeViewHolder extends RecyclerView.ViewHolder {
        private final LayoutItemRecyclerViewItemHomeBinding mBinding;

        public ItemHomeViewHolder(LayoutItemRecyclerViewItemHomeBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(RegionModel regionModel) {
            mBinding.nameItem.setText(regionModel.getName());
            mBinding.layoutItem.setOnClickListener(view -> pondListener.OnPondClicker(regionModel));
        }
    }
}
