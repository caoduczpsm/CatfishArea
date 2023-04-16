package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ItemContainerReportFishBinding;
import com.example.catfisharea.listeners.MultipleReportFishListener;
import com.example.catfisharea.models.ReportFish;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ReportFishAdapter extends RecyclerView.Adapter<ReportFishAdapter.MultipleTaskSelectionViewHolder> {

    private List<ReportFish> reportFishList;
    private final MultipleReportFishListener multipleListener;
    private ItemContainerReportFishBinding itemContainerReportFishBinding;

    public ReportFishAdapter(List<ReportFish> reportFishList, MultipleReportFishListener multipleListener) {
        this.reportFishList = reportFishList;
        this.multipleListener = multipleListener;
    }

    // Đánh dấu chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllSelected(List<ReportFish> reportFishList){
        this.reportFishList = reportFishList;
        for (ReportFish reportFish : reportFishList)
            if (!reportFish.isSelected)
                reportFish.isSelected = true;
        notifyDataSetChanged();
    }

    // Bỏ chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllUnSelected(List<ReportFish> reportFishList){
        this.reportFishList = reportFishList;
        for (ReportFish reportFish : reportFishList)
            if (reportFish.isSelected)
                reportFish.isSelected = false;
        notifyDataSetChanged();
    }

    // Bỏ chọn một số tài khoản khi đã thực thi xong yêu cầu và thay đổi quyền tài khoản sau khi người dùng bấm thay đổi
    @SuppressLint("NotifyDataSetChanged")
    public void setTaskUnSelected(List<ReportFish> reportFishList){
        for (ReportFish reportFish : reportFishList){
            if (reportFish.isSelected)
                reportFish.isSelected = false;
        }
        this.reportFishList = reportFishList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MultipleTaskSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemContainerReportFishBinding = ItemContainerReportFishBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MultipleTaskSelectionViewHolder(itemContainerReportFishBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleTaskSelectionViewHolder holder, int position) {
        holder.bindTaskSelection(reportFishList.get(position));
    }

    @Override
    public int getItemCount() {
        return reportFishList.size();
    }

    public List<ReportFish> getSelectedTask(){
        List<ReportFish> selectedTask = new ArrayList<>();
        for (ReportFish reportFish : reportFishList){
            if (reportFish.isSelected){
                selectedTask.add(reportFish);
            }
        }
        return selectedTask;
    }


    public class MultipleTaskSelectionViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerReportFishBinding mBinding;
        private final FirebaseFirestore database;

        MultipleTaskSelectionViewHolder(ItemContainerReportFishBinding mBinding){
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            itemContainerReportFishBinding = this.mBinding;
            database = FirebaseFirestore.getInstance();
        }

        @SuppressLint("SetTextI18n")
        void bindTaskSelection(final ReportFish reportFish){

            if (reportFish.isSelected){
                mBinding.viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                mBinding.imageSelected.setVisibility(View.VISIBLE);
            } else {
                mBinding.viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                mBinding.imageSelected.setVisibility(View.GONE);
            }

            mBinding.textDateReport.setText(reportFish.date.substring(8, 10) + "/" + reportFish.date.substring(5, 7) +
                    "/" + reportFish.date.substring(0, 4));
            if (reportFish.guess == null || reportFish.guess.equals("")){
                mBinding.textGuess.setText("Chưa được phỏng đoán.");
            } else {
                mBinding.textGuess.setText("Phỏng đoán: " + reportFish.guess);
            }
            database.collection(Constants.KEY_COLLECTION_POND)
                    .document(reportFish.pondId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        mBinding.textNamePond.setText(documentSnapshot.getString(Constants.KEY_NAME));
                    });
            mBinding.viewBackground.setOnClickListener(view -> {
                if (getSelectedTask().size() == 0){
                    multipleListener.onReportClicker(reportFish);
                }
            });

        }

    }
}

