package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.ItemContainerMedicineListBinding;
import com.example.catfisharea.models.Medicine;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> medicineList;
    private ItemContainerMedicineListBinding itemContainerMedicineListBinding;

    public MedicineAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemContainerMedicineListBinding = ItemContainerMedicineListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MedicineViewHolder(itemContainerMedicineListBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        holder.bindTaskSelection(medicineList.get(position));
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }


    public class MedicineViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerMedicineListBinding binding;

        MedicineViewHolder(ItemContainerMedicineListBinding mBinding){
            super(mBinding.getRoot());
            this.binding = mBinding;
            itemContainerMedicineListBinding = this.binding;
        }

        @SuppressLint("SetTextI18n")
        void bindTaskSelection(final Medicine medicine){

            binding.textMedicineName.setText(medicine.name);

            binding.textProducer.setText(medicine.producer);

            binding.textQuantityInWarehouse.setText("Kho: " + medicine.amount);
        }

    }
}

