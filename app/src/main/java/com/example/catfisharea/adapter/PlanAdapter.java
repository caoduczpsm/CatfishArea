package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.LayoutItemPlanDetailBinding;
import com.example.catfisharea.models.Plan;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class PlanAdapter extends RecyclerView.Adapter<PlanAdapter.PlanHolder> {

    private final List<Plan> mPlans;

    public PlanAdapter(List<Plan> mPlans) {
        this.mPlans = mPlans;
    }

    @NonNull
    @Override
    public PlanHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemPlanDetailBinding layoutPlanItemBinding = LayoutItemPlanDetailBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
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

    static class PlanHolder extends RecyclerView.ViewHolder {
        private final LayoutItemPlanDetailBinding mBinding;

        public PlanHolder(@NonNull LayoutItemPlanDetailBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("SetTextI18n")
        public void setData(Plan plan) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
//            String date = plan.getDate().getDay() +"-"+ plan.getDate().getMonth() + "-" +plan.getDate().getYear();
            mBinding.dateItem.setText("Ngày: "+ format.format(plan.getDate()));
            mBinding.dateBirth.setText("Ngày tuổi: " +plan.getOld());
            mBinding.fishWeight.setText(formatText(plan.getFishWeight()));
            mBinding.numberOfFish.setText(formatText(plan.getNumberOfFish()));
            mBinding.numberOfDeadFish.setText(formatText(plan.getNumberOfDeadFish()));
            mBinding.LKnumberOfDeadFish.setText(formatText(plan.getLKnumberOfDeadFish()));
            mBinding.fishLeft.setText(formatText(plan.getNumberOfFishAlive()));
            mBinding.tlhh.setText(roundThreeDecimals(plan.getSurvivalRate()) + "");
            mBinding.numberOfFood.setText(formatText(plan.getFood()));
            mBinding.LKnumberOfFood.setText(formatText(plan.getTotalFood()));
            mBinding.BQFish.setText(plan.getAVG()+ "");
        }

        public double roundThreeDecimals(double d) {
            BigDecimal bd = new BigDecimal(d);
            bd = bd.setScale(3, RoundingMode.HALF_UP);
            return bd.doubleValue();
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
