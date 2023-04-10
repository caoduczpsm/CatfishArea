package com.example.catfisharea.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemHomeRecyclerviewBinding;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private final List<ItemHome> mRegion;
    private final Context context;

    public HomeAdapter(Context context, List<ItemHome> item) {
        this.mRegion = item;
        this.context = context;
    }

    @NonNull
    @Override
    public HomeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemHomeRecyclerviewBinding mBinding = LayoutItemHomeRecyclerviewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new HomeViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull HomeViewHolder holder, int position) {
        ItemHome item = mRegion.get(position);
        holder.setData(item);
    }

    @Override
    public int getItemCount() {
        return mRegion.size();
    }

    class HomeViewHolder extends RecyclerView.ViewHolder {
        private final LayoutItemHomeRecyclerviewBinding mBinding;
        private ItemHomeAdapter adapter;

        public HomeViewHolder(LayoutItemHomeRecyclerviewBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(ItemHome item) {
            mBinding.nameItem.setText(item.getRegionModel().getName());
            if (item.getReginonList() == null || item.getReginonList().isEmpty()) {
                if (item.getRegionModel() instanceof Area) {
                    mBinding.nameEmpty.setText("Vùng trống");
                    mBinding.nameEmpty.setVisibility(View.VISIBLE);
                } else if (item.getRegionModel() instanceof Campus) {
                    mBinding.nameEmpty.setText("Khu trống");
                    mBinding.nameEmpty.setVisibility(View.VISIBLE);
                }
            } else {
                mBinding.nameEmpty.setVisibility(View.GONE);
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                mBinding.recyclerviewItemHome.setLayoutManager(layoutManager);
                adapter = new ItemHomeAdapter(item.getReginonList());
                mBinding.recyclerviewItemHome.setAdapter(adapter);
            }


        }
    }
}
