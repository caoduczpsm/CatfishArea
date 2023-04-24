package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemWarehouseBinding;
import com.example.catfisharea.listeners.WarehouseListener;
import com.example.catfisharea.models.Warehouse;
import java.util.List;


public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.WarehouseHolder>{

    private List<Warehouse> mWarehouses;
    private WarehouseListener warehouseListener;

    public WarehouseAdapter(List<Warehouse> mWarehouses, WarehouseListener warehouseListener) {
        this.mWarehouses = mWarehouses;
        this.warehouseListener = warehouseListener;
    }

    @NonNull
    @Override
    public WarehouseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemWarehouseBinding layoutItemWarehouseBinding =
                LayoutItemWarehouseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WarehouseHolder(layoutItemWarehouseBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseHolder holder, int position) {
        Warehouse warehouse = mWarehouses.get(position);
        holder.setData(warehouse);
    }

    @Override
    public int getItemCount() {
        return mWarehouses.size();
    }

    class WarehouseHolder extends RecyclerView.ViewHolder {
        private LayoutItemWarehouseBinding mBinding;
        public WarehouseHolder(@NonNull LayoutItemWarehouseBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(Warehouse warehouse) {
            mBinding.nameWarehouse.setText(warehouse.getName());
            mBinding.category.setText(warehouse.getPondName());
            mBinding.itemWarehouse.setOnClickListener(view -> {
                warehouseListener.openWarehouse(warehouse);
            });
        }
    }
}
