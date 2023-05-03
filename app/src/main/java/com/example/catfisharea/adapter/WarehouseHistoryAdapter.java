package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.databinding.LayoutItemHistoryWarehouseBinding;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.models.WarehouseHistory;

import java.util.List;

public class WarehouseHistoryAdapter extends RecyclerView.Adapter<WarehouseHistoryAdapter.WarehouseHistoryHolder> {
    private final List<WarehouseHistory> mList;

    public WarehouseHistoryAdapter(List<WarehouseHistory> mList) {
        this.mList = mList;
    }

    @NonNull
    @Override
    public WarehouseHistoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemHistoryWarehouseBinding mBinding = LayoutItemHistoryWarehouseBinding
                .inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new WarehouseHistoryHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WarehouseHistoryHolder holder, int position) {
        WarehouseHistory item = mList.get(position);
        holder.setData(item);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    static class WarehouseHistoryHolder extends RecyclerView.ViewHolder {
        private final LayoutItemHistoryWarehouseBinding mBinding;

        public WarehouseHistoryHolder(@NonNull LayoutItemHistoryWarehouseBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("SetTextI18n")
        public void setData(WarehouseHistory data) {
            mBinding.dateImport.setText(data.getDate().getDate() + "/" + (data.getDate().getMonth() + 1)
                    + "/" + (data.getDate().getYear() + 1900));
            mBinding.total.setText(data.getTotal());
            StringBuilder nameItem = new StringBuilder();
            StringBuilder amount = new StringBuilder();
            for (Category item : data.getmCategory()) {
                nameItem.append(item.getName()).append("\n");
                amount.append(item.getAmount()).append(" ").append(item.getUnit()).append("\n");
            }
            mBinding.nameItem.setText(nameItem.toString().trim());
            mBinding.amountItem.setText(amount.toString().trim());
        }
    }
}
