package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemHomeRecyclerviewBinding;
import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private final List<ItemHome> mRegion;
    private final Context context;
    private final CampusListener campusListener;
    private boolean isShowed = false;
    private List<RegionModel> regionModels = new ArrayList<>();

    public HomeAdapter(Context context, List<ItemHome> item, CampusListener campusListener) {
        this.mRegion = item;
        this.context = context;
        this.campusListener = campusListener;
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

    public void setShowed(boolean showed) {
        isShowed = showed;
    }

    public boolean isShowed() {
        return isShowed;
    }

    public void setRegionModels(List<RegionModel> regionModels) {
        this.regionModels = regionModels;
    }

    class HomeViewHolder extends RecyclerView.ViewHolder {
        private final LayoutItemHomeRecyclerviewBinding mBinding;
        private final PreferenceManager preferenceManager;

        public HomeViewHolder(LayoutItemHomeRecyclerviewBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            preferenceManager = new PreferenceManager(context);
        }

        @SuppressLint("SetTextI18n")
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
                String type = preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT);

                if (type != null) {
                    if (type.equals(Constants.KEY_DIRECTOR)) {
                        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 3);
                        mBinding.recyclerviewItemHome.setLayoutManager(gridLayoutManager);
                    } else {
                        LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                        layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                        mBinding.recyclerviewItemHome.setLayoutManager(layoutManager);
                    }
                }

                ItemHomeAdapter adapter = new ItemHomeAdapter(item.getReginonList(), campusListener);
                mBinding.recyclerviewItemHome.setAdapter(adapter);

                if (isShowed) {
                    if (regionModels.isEmpty()) {
                        mBinding.campusEmpty.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.campusEmpty.setVisibility(View.GONE);
                        ItemHomeAdapter adapter1 = new ItemHomeAdapter(regionModels, campusListener);
                        LinearLayoutManager layoutManager2 = new LinearLayoutManager(context);
                        layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
                        mBinding.recyclerviewItem.setLayoutManager(layoutManager2);
                        mBinding.recyclerviewItem.setAdapter(adapter1);
                        mBinding.recyclerviewItem.setVisibility(View.VISIBLE);
                    }
                } else {
                    mBinding.campusEmpty.setVisibility(View.GONE);
                    mBinding.recyclerviewItem.setVisibility(View.GONE);
                }
            }


//            mBinding.nameItem.setOnClickListener(view -> campusListener.OnCampusClicker(item.getRegionModel()));
        }
    }
}
