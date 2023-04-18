package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.databinding.ItemContainerTreatmentMedicineUsedBinding;
import com.example.catfisharea.models.Medicine;
import java.util.List;

public class MedicineTreatmentUsedAdapter extends RecyclerView.Adapter<MedicineTreatmentUsedAdapter.MedicineViewHolder> {

    private final List<Medicine> medicineList;
    private final List<String> quantityList;
    private ItemContainerTreatmentMedicineUsedBinding itemContainerMedicineListBinding;

    public MedicineTreatmentUsedAdapter(List<Medicine> medicineList, List<String> quantityList) {
        this.medicineList = medicineList;
        this.quantityList = quantityList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        itemContainerMedicineListBinding = ItemContainerTreatmentMedicineUsedBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MedicineViewHolder(itemContainerMedicineListBinding);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        holder.bindTaskSelection(medicineList.get(position));
        holder.binding.textQuantity.setText("Liều dùng: " + quantityList.get(position));
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }


    public class MedicineViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerTreatmentMedicineUsedBinding binding;

        MedicineViewHolder(ItemContainerTreatmentMedicineUsedBinding mBinding){
            super(mBinding.getRoot());
            this.binding = mBinding;
            itemContainerMedicineListBinding = this.binding;
        }

        @SuppressLint("SetTextI18n")
        void bindTaskSelection(final Medicine medicine){

            binding.textMedicineName.setText(medicine.name);

            binding.textProducer.setText(medicine.producer);
        }

    }
}

