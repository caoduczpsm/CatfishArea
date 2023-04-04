package com.example.catfisharea.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutPlanItemBinding;
import com.example.catfisharea.models.Plan;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanHolder> {

    private List<Plan> mPlans;

    public PlanAdapter(List<Plan> mPlans) {
        this.mPlans = mPlans;
    }

    @NonNull
    @Override
    public PlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutPlanItemBinding layoutPlanItemBinding = LayoutPlanItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PlanHolder(layoutPlanItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull PlanHolder holder, int position) {
        Plan plan = mPlans.get(position);
        holder.setData(plan);
    }

    @Override
    public int getItemCount() {
        return mPlans.size();
    }

    class PlanHolder extends RecyclerView.ViewHolder {
        private LayoutPlanItemBinding mBinding;

        public PlanHolder(@NonNull LayoutPlanItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(Plan plan) {
            mBinding.nameItem.setText(plan.getPondName());
            mBinding.acreage.setText(formatText(Float.parseFloat(plan.getAcreage())));
            mBinding.date.setText(plan.getDate());
            mBinding.consistence.setText(plan.getConsistence() + "");
            mBinding.numberOfFish.setText(formatText(plan.getNumberOfFish()));
            mBinding.survivalRate.setText(plan.getSurvivalRate() + "");
            mBinding.numberOfFishAlive.setText(formatText(plan.getNumberOfFish()));
            mBinding.harvestSize.setText(plan.getHarvestSize() + "");
            mBinding.harvestYield.setText(formatText(plan.getHarvestYield()));
            mBinding.fcr.setText(plan.getFcr() + "");
            mBinding.food.setText(formatText(plan.getFood()));
            mBinding.fingerlingSamples.setText(formatText(plan.getFingerlingSamples()));
        }

        private String formatText(Object text) {
            DecimalFormatSymbols dfs = new DecimalFormatSymbols(Locale.US);
            dfs.setDecimalSeparator('.');
            dfs.setGroupingSeparator(',');
            dfs.setCurrencySymbol("VNĐ");

            DecimalFormat df = new DecimalFormat("#,###.##", dfs);
            return df.format(text);
        }

    }
}
