package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.databinding.ItemContainerMedicineListBinding;
import com.example.catfisharea.models.Medicine;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private final List<Medicine> medicineList;
    private ItemContainerMedicineListBinding itemContainerMedicineListBinding;
    private final Context context;

    public MedicineAdapter(Context context, List<Medicine> medicineList) {
        this.medicineList = medicineList;
        this.context = context;
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
        private final FirebaseFirestore database;
        private final PreferenceManager preferenceManager;

        MedicineViewHolder(ItemContainerMedicineListBinding mBinding){
            super(mBinding.getRoot());
            this.binding = mBinding;
            itemContainerMedicineListBinding = this.binding;
            database = FirebaseFirestore.getInstance();
            preferenceManager = new PreferenceManager(context);
        }

        @SuppressLint("SetTextI18n")
        void bindTaskSelection(final Medicine medicine){

            binding.textMedicineName.setText(medicine.name);

            binding.textProducer.setText(medicine.producer);

            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                    .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                    .document(queryDocumentSnapshot.getId())
                                    .collection(Constants.KEY_COLLECTION_CATEGORY)
                                    .document(medicine.id)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        DocumentSnapshot documentSnapshot = task1.getResult();
                                        binding.textQuantityInWarehouse.setText("Kho: " + documentSnapshot.getString(Constants.KEY_AMOUNT_OF_ROOM));
                                    });
                        }
                    });
        }

    }
}

