package com.example.catfisharea.adapter;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.LayoutImportWarehouseBinding;
import com.example.catfisharea.listeners.ImportWarehouseListener;
import com.example.catfisharea.models.Category;

import java.util.ArrayList;
import java.util.List;

public class ImportAdapter extends RecyclerView.Adapter<ImportAdapter.ImportHolder> {
    private List<Category> mCategories;
    private Context context;
    private List<Category> result;
    private ImportWarehouseListener listener;

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
        private LayoutImportWarehouseBinding mBinding;

        public ImportHolder(@NonNull LayoutImportWarehouseBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        public void setData(Category category, int position) {

            if (position < result.size() - 1) {
                mBinding.addBtn.setVisibility(View.GONE);
            } else {
                mBinding.addBtn.setVisibility(View.VISIBLE);
            }
            AutoCompleteSpinner spinnerAdapter = new AutoCompleteSpinner(context, R.layout.layout_spinner_item, mCategories);
            mBinding.edtProduct.setAdapter(spinnerAdapter);

            mBinding.edtProduct.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mBinding.textInputAmount.setHint("Số lượng(" + mCategories.get(position).getUnit() + ")");
                    category.setId(mCategories.get(position).getId());
                    category.setName(mCategories.get(position).getName());
                    category.setUnit(mCategories.get(position).getUnit());
                    category.setEffect(mCategories.get(position).getEffect());
                    category.setProducer(mCategories.get(position).getProducer());
                }
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
                    if (!mBinding.edtAmount.getText().toString().isEmpty()) {
                        category.setAmount(Integer.parseInt(mBinding.edtAmount.getText().toString()));
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
                    if (!mBinding.edtMoney.getText().toString().isEmpty()) {
                        category.setPrice(Float.parseFloat(mBinding.edtMoney.getText().toString()));
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
