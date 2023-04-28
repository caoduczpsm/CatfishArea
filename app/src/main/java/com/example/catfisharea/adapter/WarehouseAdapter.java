package com.example.catfisharea.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemHomeRecyclerviewBinding;
import com.android.app.catfisharea.databinding.LayoutItemWarehouseBinding;
import com.example.catfisharea.listeners.WarehouseListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemWarehouse;
import com.example.catfisharea.models.Warehouse;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

import java.util.List;


public class WarehouseAdapter extends RecyclerView.Adapter<WarehouseAdapter.WarehouseHolder>{

    private List<ItemWarehouse> mWarehouses;
    private final Context context;
    private WarehouseListener warehouseListener;

    public WarehouseAdapter(List<ItemWarehouse> mWarehouses, Context context, WarehouseListener warehouseListener) {
        this.mWarehouses = mWarehouses;
        this.context = context;
        this.warehouseListener = warehouseListener;
    }

    @NonNull
    @Override
    public WarehouseHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemHomeRecyclerviewBinding layoutItemWarehouseBinding =
                LayoutItemHomeRecyclerviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WarehouseHolder(layoutItemWarehouseBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseHolder holder, int position) {
        ItemWarehouse itemWarehouse = mWarehouses.get(position);
        holder.setData(itemWarehouse);
    }

    @Override
    public int getItemCount() {
        return mWarehouses.size();
    }

    class WarehouseHolder extends RecyclerView.ViewHolder {
        private final LayoutItemHomeRecyclerviewBinding mBinding;
        private PreferenceManager preferenceManager;

        public WarehouseHolder(@NonNull LayoutItemHomeRecyclerviewBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            preferenceManager = new PreferenceManager(context);
        }

        public void setData(ItemWarehouse itemWarehouse) {
            mBinding.nameItem.setText(itemWarehouse.getRegionModel().getName());
            if (itemWarehouse.getWarehouseList() != null && !itemWarehouse.getWarehouseList().isEmpty()){
                mBinding.nameEmpty.setVisibility(View.GONE);
                String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);
                if (type.equals(Constants.KEY_DIRECTOR)) {
                    GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                    mBinding.recyclerviewItemHome.setLayoutManager(gridLayoutManager);
                } else {
                    LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                    layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                    mBinding.recyclerviewItemHome.setLayoutManager(layoutManager);
                }

                ItemWarehouseAdapter adapter = new ItemWarehouseAdapter(itemWarehouse.getWarehouseList(), warehouseListener);
                mBinding.recyclerviewItemHome.setAdapter(adapter);
            } else {
                mBinding.nameEmpty.setText("Chưa có kho");
                mBinding.nameEmpty.setVisibility(View.VISIBLE);
            }
        }


    }
}
