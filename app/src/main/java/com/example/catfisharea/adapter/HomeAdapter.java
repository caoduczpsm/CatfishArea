package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemHomeRecyclerviewBinding;
import com.example.catfisharea.activities.alluser.PondDetailsActivity;
import com.example.catfisharea.listeners.CampusListener;
import com.example.catfisharea.listeners.PondListener;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.ItemHome;
import com.example.catfisharea.models.RegionModel;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

import java.util.List;

public class HomeAdapter extends RecyclerView.Adapter<HomeAdapter.HomeViewHolder> {

    private final List<ItemHome> mRegion;
    private final Context context;
    private final CampusListener campusListener;

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

    class HomeViewHolder extends RecyclerView.ViewHolder implements PondListener {
        private final LayoutItemHomeRecyclerviewBinding mBinding;

        public HomeViewHolder(LayoutItemHomeRecyclerviewBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
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
                LinearLayoutManager layoutManager = new LinearLayoutManager(context);
                layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
                mBinding.recyclerviewItemHome.setLayoutManager(layoutManager);

                ItemHomeAdapter adapter = new ItemHomeAdapter(item.getReginonList(), this);
                mBinding.recyclerviewItemHome.setAdapter(adapter);
            }
            mBinding.nameItem.setOnClickListener(view -> campusListener.OnCampusClicker(item.getRegionModel()));
        }

        @Override
        public void OnPondClicker(RegionModel regionModel) {
            PreferenceManager preferenceManager = new PreferenceManager(context);
            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF) ||
                    preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
                Intent intent = new Intent(context, PondDetailsActivity.class);
                intent.putExtra(Constants.KEY_POND, regionModel);
                context.startActivity(intent);
            }
        }
    }
}
