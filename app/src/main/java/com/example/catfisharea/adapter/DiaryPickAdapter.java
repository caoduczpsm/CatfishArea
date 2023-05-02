package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemPickDiaryBinding;
import com.example.catfisharea.listeners.DiaryListener;
import com.example.catfisharea.models.DiaryPickItem;

import java.util.List;

public class DiaryPickAdapter extends RecyclerView.Adapter<DiaryPickAdapter.DiaryPickHolder> {
    private List<DiaryPickItem> itemList;
    private DiaryListener listener;

    public DiaryPickAdapter(List<DiaryPickItem> itemList, DiaryListener listener) {
        this.itemList = itemList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiaryPickHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemPickDiaryBinding mBinding =
                LayoutItemPickDiaryBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new DiaryPickHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull DiaryPickHolder holder, int position) {
        DiaryPickItem item = itemList.get(position);
        holder.setData(item);
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class DiaryPickHolder extends RecyclerView.ViewHolder {
        private LayoutItemPickDiaryBinding mBinding;

        public DiaryPickHolder(LayoutItemPickDiaryBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(DiaryPickItem item) {
            mBinding.dateStart.setText(item.dateStart);
            mBinding.dateEnd.setText(item.dateEnd);
            mBinding.item.setOnClickListener(view -> {
                listener.clickDiary(item);
            });
        }
    }
}
