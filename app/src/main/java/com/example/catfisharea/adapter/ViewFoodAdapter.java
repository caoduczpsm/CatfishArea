package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.databinding.LayoutItemFoodViewBinding;
import com.example.catfisharea.models.Feed;

import java.text.SimpleDateFormat;
import java.util.List;

public class ViewFoodAdapter extends RecyclerView.Adapter<ViewFoodAdapter.ViewFoodHolder>{

    private final List<Feed> feedList;

    public ViewFoodAdapter(List<Feed> feedList) {
        this.feedList = feedList;
    }

    @NonNull
    @Override
    public ViewFoodHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemFoodViewBinding mBinding = LayoutItemFoodViewBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ViewFoodHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewFoodHolder holder, int position) {
        Feed feed = feedList.get(position);
        holder.setData(feed);
    }

    @Override
    public int getItemCount() {
        return feedList.size();
    }

    static class ViewFoodHolder extends RecyclerView.ViewHolder {
        private final LayoutItemFoodViewBinding mBinding;

        public ViewFoodHolder(LayoutItemFoodViewBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("SetTextI18n")
        public void setData(Feed feed) {
            @SuppressLint("SimpleDateFormat") SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");
            mBinding.dateItem.setText("Ngày: " + format.format(feed.getDate()) );
            mBinding.dateBirth.setText("Ngày tuổi: "+ feed.getOld() + "");
            mBinding.food1.setText(feed.getAmountFed().get(0));
            mBinding.food2.setText(feed.getAmountFed().get(1));
            mBinding.food3.setText(feed.getAmountFed().get(2));
            mBinding.food4.setText(feed.getAmountFed().get(3));
            mBinding.food5.setText(feed.getAmountFed().get(4));
            mBinding.food6.setText(feed.getAmountFed().get(5));
            mBinding.food7.setText(feed.getAmountFed().get(6));
            mBinding.food8.setText(feed.getAmountFed().get(7));
            mBinding.food.setText(feed.sumFood());
            mBinding.LKFood.setText(feed.getTotalFood()+ "");

        }
    }
}
