package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.LayoutImportWarehouseBinding;
import com.example.catfisharea.listeners.ImportWarehouseListener;
import com.example.catfisharea.models.Category;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ImportHolder> {
    private final List<Category> mCategories;
    private final Context context;
    private final List<Category> result;
    private final ImportWarehouseListener listener;

    public ImportAdapter(Context context, List<Category> mCategories, ImportWarehouseListener listener) {
        this.context = context;
        this.mCategories = mCategories;
        this.listener = listener;
        this.result = new ArrayList<>();
        result.add(new Category());
    }

    @NonNull
    @Override
    public ImportHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutImportWarehouseBinding mBinding = LayoutImportWarehouseBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false);

        return new ImportHolder(mBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull ImportHolder holder, int position) {
        Category category = result.get(position);
        holder.setData(category, position);
    }

    @Override
    public int getItemCount() {
        return result.size();
    }

    class ImportHolder extends RecyclerView.ViewHolder {
        private final LayoutImportWarehouseBinding mBinding;

        public ImportHolder(@NonNull LayoutImportWarehouseBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setData(Category category, int position) {

            if (position < result.size() - 1) {
                mBinding.addBtn.setVisibility(View.GONE);
            } else {
                mBinding.addBtn.setVisibility(View.VISIBLE);
            }
            AutoCompleteSpinner spinnerAdapter = new AutoCompleteSpinner(context, R.layout.layout_spinner_item, mCategories);
            mBinding.edtProduct.setAdapter(spinnerAdapter);

            mBinding.edtProduct.setOnItemClickListener((parent, view, position1, id) -> {
                mBinding.textInputAmount.setHint("Số lượng(" + mCategories.get(position1).getUnit() + ")");
                category.setId(mCategories.get(position1).getId());
                category.setName(mCategories.get(position1).getName());
                category.setUnit(mCategories.get(position1).getUnit());
                category.setEffect(mCategories.get(position1).getEffect());
                category.setProducer(mCategories.get(position1).getProducer());
            });

            mBinding.edtAmount.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!Objects.requireNonNull(mBinding.edtAmount.getText()).toString().isEmpty()) {
                        category.setAmount(mBinding.edtAmount.getText().toString());
                        listener.changeMoney(result);
                    }

                }
            });

            mBinding.edtMoney.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!Objects.requireNonNull(mBinding.edtMoney.getText()).toString().isEmpty()) {
                        category.setPrice(mBinding.edtMoney.getText().toString());
                        listener.changeMoney(result);
                    }

                }
            });


            mBinding.addBtn.setOnClickListener(view -> {
                result.add(new Category());
                mBinding.addBtn.setVisibility(View.GONE);
                notifyItemInserted(result.size());
            });

            mBinding.imageRemove.setOnClickListener(view -> {
                result.remove(position);
                notifyDataSetChanged();
            });
        }
    }

    public List<Category> getResult() {
        return result;
    }
}
