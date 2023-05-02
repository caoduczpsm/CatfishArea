package com.example.catfisharea.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemAccountingPondBinding;
import com.example.catfisharea.models.AccountingItem;

import java.util.List;

public class AccountingAdapter extends RecyclerView.Adapter<AccountingAdapter.AccountingHolder> {

    private List<AccountingItem> accountingItems;

    public AccountingAdapter(List<AccountingItem> accountingItems) {
        this.accountingItems = accountingItems;
    }

    @NonNull
    @Override
    public AccountingHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemAccountingPondBinding mBinding = LayoutItemAccountingPondBinding.inflate(LayoutInflater.from(parent.getContext())
        , parent, false);
        return new AccountingHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull AccountingHolder holder, int position) {
        AccountingItem accountingItem = accountingItems.get(position);
        holder.setData(accountingItem);
    }

    @Override
    public int getItemCount() {
        return accountingItems.size();
    }

    class AccountingHolder extends RecyclerView.ViewHolder {
        private LayoutItemAccountingPondBinding mBinding;

        public AccountingHolder(LayoutItemAccountingPondBinding mBiding) {
            super(mBiding.getRoot());
            this.mBinding = mBiding;
        }

        public void setData(AccountingItem accountingItem) {
            mBinding.cardPercent.setCardBackgroundColor(accountingItem.color);
            mBinding.percent.setText(accountingItem.perent);
            mBinding.name.setText(accountingItem.name);
            mBinding.total.setText(accountingItem.total);
            if (accountingItem.color == Color.TRANSPARENT) {
                mBinding.cardPercent.setVisibility(View.GONE);
            }
        }
    }
}
