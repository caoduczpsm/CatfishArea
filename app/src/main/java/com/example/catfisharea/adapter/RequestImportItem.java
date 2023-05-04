package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.ImportRequestItemBinding;
import com.example.catfisharea.listeners.MaterialsListener;
import com.example.catfisharea.models.Materials;

import java.util.List;

public class RequestImportItem extends RecyclerView.Adapter<RequestImportItem.ImportHolder> {
    private List<Materials> materials;
    private final MaterialsListener listener;
    private boolean isChecked = false;

    public RequestImportItem(Context context, List<Materials> materials, MaterialsListener listener) {
        this.materials = materials;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImportRequestItemBinding requestItemBinding = ImportRequestItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ImportHolder(requestItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportHolder holder, int position) {
        Materials mtr = materials.get(position);
        holder.setData(mtr);
    }

    @Override
    public int getItemCount() {
        return materials.size();
    }

    class ImportHolder extends RecyclerView.ViewHolder {
        private final ImportRequestItemBinding mBinding;

        public ImportHolder(ImportRequestItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;

        }

        @SuppressLint("SetTextI18n")
        public void setData(Materials mtr) {
            if (mtr.getAmount() >= 0) {
                mBinding.amountItem.setText(mtr.getAmount() + "");
            }
            mBinding.nameItem.setText(mtr.getName());
            if (!mtr.getDecription().trim().isEmpty()) {
                mBinding.nameChildItem.setVisibility(View.VISIBLE);
                mBinding.nameChildItem.setText(mtr.getDecription());
            }
            if (isChecked) {
                mBinding.removeItem.setVisibility(View.GONE);
            }
            mBinding.removeItem.setOnClickListener(view -> listener.delete(mtr));
            mBinding.nameItem.setOnClickListener(view -> listener.edit(mtr));
            mBinding.amountItem.setOnClickListener(view -> listener.edit(mtr));
        }

    }

    public List<Materials> getMaterials() {
        return materials;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMaterials(List<Materials> materials) {
        this.materials = materials;
        notifyDataSetChanged();
    }

    public boolean isChecked() {
        return isChecked;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setChecked(boolean checked) {
        isChecked = checked;
        notifyDataSetChanged();
    }
}
