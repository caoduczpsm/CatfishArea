package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemHistoryWarehouseBinding;
import com.example.catfisharea.models.Category;
import com.example.catfisharea.models.ImportRequest;
import com.example.catfisharea.models.Warehouse;
import com.example.catfisharea.models.WarehouseHistory;

import java.util.List;

public class WarehouseHistoryAdapter extends RecyclerView.Adapter<WarehouseHistoryAdapter.WarehouseHistoryHolder> {
    private List<WarehouseHistory> mList;

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

    class WarehouseHistoryHolder extends RecyclerView.ViewHolder {
        private LayoutItemHistoryWarehouseBinding mBinding;

        public WarehouseHistoryHolder(@NonNull LayoutItemHistoryWarehouseBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(WarehouseHistory data) {
            mBinding.dateImport.setText(data.getDate().getDate() + "/" + (data.getDate().getMonth() + 1) + "/" + (data.getDate().getYear() + 1900));
            mBinding.total.setText(data.getTotal());
            String nameitem = new String();
            String amount = new String();
            for (Category item : data.getmCategory()) {
                nameitem += item.getName() + "\n";
                amount += item.getAmount() + " " + item.getUnit() + "\n";
            }
            mBinding.nameItem.setText(nameitem);
            mBinding.amountItem.setText(amount);
        }
    }
}
