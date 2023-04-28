package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.LayoutItemHomeRecyclerviewBinding;
import com.android.app.catfisharea.databinding.LayoutItemRecyclerViewItemHomeBinding;
import com.example.catfisharea.listeners.WarehouseListener;
import com.example.catfisharea.models.Warehouse;

import java.util.List;

public class ItemWarehouseAdapter extends RecyclerView.Adapter<ItemWarehouseAdapter.ItemWarehouseViewHolder> {
    private List<Warehouse> warehouseList;
    private WarehouseListener warehouseListener;

    public ItemWarehouseAdapter(List<Warehouse> warehouseList, WarehouseListener warehouseListener) {
        this.warehouseList = warehouseList;
        this.warehouseListener = warehouseListener;
    }

    @NonNull
    @Override
    public ItemWarehouseAdapter.ItemWarehouseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemRecyclerViewItemHomeBinding mBinding = LayoutItemRecyclerViewItemHomeBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new ItemWarehouseAdapter.ItemWarehouseViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemWarehouseAdapter.ItemWarehouseViewHolder holder, int position) {
        Warehouse item = warehouseList.get(position);
        holder.setData(item);
    }

    @Override
    public int getItemCount() {
        return warehouseList.size();
    }

    class ItemWarehouseViewHolder extends RecyclerView.ViewHolder {
        private final LayoutItemRecyclerViewItemHomeBinding mBinding;

        public ItemWarehouseViewHolder(LayoutItemRecyclerViewItemHomeBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(Warehouse warehouse) {
            mBinding.nameItem.setText(warehouse.getName());
            mBinding.imageItem.setImageResource(R.drawable.warehouse);
            mBinding.layoutItem.setOnClickListener(view -> warehouseListener.openWarehouse(warehouse));
        }
    }
}
