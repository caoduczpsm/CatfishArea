package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutMedicineListWarehouseBinding;
import com.example.catfisharea.models.Category;

import java.util.List;

public class WarehouseAnotherDetailAdapter extends RecyclerView.Adapter<WarehouseAnotherDetailAdapter.WarehouseDetailHolder>{
    private final List<Category> mCategories;

    public WarehouseAnotherDetailAdapter(List<Category> mCategories) {
        this.mCategories = mCategories;
    }

    @NonNull
    @Override
    public WarehouseDetailHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutMedicineListWarehouseBinding mBinding = LayoutMedicineListWarehouseBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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
        private final LayoutMedicineListWarehouseBinding mBinding;

        public WarehouseDetailHolder(@NonNull LayoutMedicineListWarehouseBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("SetTextI18n")
        public void setData(Category category) {
            mBinding.nameCategory.setText(category.getName());
            mBinding.nameProducer.setText(category.getProducer());
            mBinding.amountCatagory.setText(category.getAmount()+ " " + category.getUnit());
        }
    }
}
