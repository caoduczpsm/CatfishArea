package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemEnvironmentBinding;
import com.example.catfisharea.models.Environment;
import com.example.catfisharea.models.Feed;
import com.example.catfisharea.ultilities.Constants;

import java.text.SimpleDateFormat;
import java.util.List;

public class EnvironmentViewAdapter extends RecyclerView.Adapter<EnvironmentViewAdapter.EnvironmentViewHolder> {
    private List<Environment> environments;


    public EnvironmentViewAdapter(List<Environment> environments) {
        this.environments = environments;
    }

    @NonNull
    @Override
    public EnvironmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemEnvironmentBinding mBinding = LayoutItemEnvironmentBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new EnvironmentViewHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull EnvironmentViewHolder holder, int position) {
        Environment environment = environments.get(position);
        holder.setData(environment);
    }

    @Override
    public int getItemCount() {
        return environments.size();
    }

    class EnvironmentViewHolder extends RecyclerView.ViewHolder {
        private LayoutItemEnvironmentBinding mBinding;

        public EnvironmentViewHolder(LayoutItemEnvironmentBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(Environment environment) {
            SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            mBinding.dateItem.setText("Ngày: " + format.format(environment.getDate()) );
            mBinding.dateBirth.setText("Ngày tuổi: "+ environment.getOld() + "");
            mBinding.food1.setText(environment.getParameter().get(Constants.KEY_SPECIFICATION_SALINITY));
            mBinding.food2.setText(environment.getParameter().get(Constants.KEY_SPECIFICATION_ALKALINITY));
            mBinding.food3.setText(environment.getParameter().get(Constants.KEY_SPECIFICATION_PH));
            mBinding.food5.setText(environment.getParameter().get(Constants.KEY_SPECIFICATION_H2S));
            mBinding.food6.setText(environment.getParameter().get(Constants.KEY_SPECIFICATION_NH3));
            mBinding.food7.setText(environment.getParameter().get(Constants.KEY_SPECIFICATION_TEMPERATE));

        }

    }
}
