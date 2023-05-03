package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutWarehouseDetailBinding;
import com.example.catfisharea.models.Category;

import java.util.List;

public class WarehouseFoodDetailAdapter extends RecyclerView.Adapter<WarehouseFoodDetailAdapter.WarehouseDetailHolder>{
    private final List<Category> mCategories;

    public WarehouseFoodDetailAdapter(List<Category> mCategories) {
        this.mCategories = mCategories;
    }

    @NonNull
    @Override
    public WarehouseDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutWarehouseDetailBinding mBinding = LayoutWarehouseDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WarehouseDetailHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseDetailHolder holder, int position) {
        Category category = mCategories.get(position);
        holder.setData(category);
    }

    @Override
    public int getItemCount() {
        return mCategories.size();
    }

    static class WarehouseDetailHolder extends RecyclerView.ViewHolder {
        private final LayoutWarehouseDetailBinding mBinding;

        public WarehouseDetailHolder(@NonNull LayoutWarehouseDetailBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("SetTextI18n")
        public void setData(Category category) {
            //mBinding.nameCatagory.setText(category.getName());
            mBinding.nameProducer.setText(category.getProducer());
            mBinding.amountCatagory.setText(category.getAmount()+ " " + category.getUnit());
        }
    }
}
