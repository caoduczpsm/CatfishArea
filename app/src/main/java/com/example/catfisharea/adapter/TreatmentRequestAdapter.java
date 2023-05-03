package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ItemContainerTreatmentRequestBinding;
import com.example.catfisharea.activities.alluser.ChatActivity;
import com.example.catfisharea.listeners.TreatmentListener;
import com.example.catfisharea.models.Medicine;
import com.example.catfisharea.models.Treatment;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class TreatmentRequestAdapter extends RecyclerView.Adapter<TreatmentRequestAdapter.MultipleTaskSelectionViewHolder> {

    private final List<Treatment> treatments;
    private final Context context;
    private final TreatmentListener treatmentListener;
    private ItemContainerTreatmentRequestBinding treatmentRequestBinding;

    public TreatmentRequestAdapter(Context context, List<Treatment> treatments, TreatmentListener treatmentListener) {
        this.treatments = treatments;
        this.context = context;
        this.treatmentListener = treatmentListener;
    }

    @NonNull
    @Override
    public MultipleTaskSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        treatmentRequestBinding = ItemContainerTreatmentRequestBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new MultipleTaskSelectionViewHolder(treatmentRequestBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleTaskSelectionViewHolder holder, int position) {
        holder.bindTaskSelection(treatments.get(position));
    }

    @Override
    public int getItemCount() {
        return treatments.size();
    }


    public class MultipleTaskSelectionViewHolder extends RecyclerView.ViewHolder{

        private final ItemContainerTreatmentRequestBinding mBinding;
        private final PreferenceManager preferenceManager;
        private final FirebaseFirestore database;

        MultipleTaskSelectionViewHolder(ItemContainerTreatmentRequestBinding mBinding){
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            treatmentRequestBinding = this.mBinding;
            database = FirebaseFirestore.getInstance();
            preferenceManager = new PreferenceManager(context);
        }

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        void bindTaskSelection(final Treatment treatment){

            database.collection(Constants.KEY_COLLECTION_CAMPUS)
                    .document(treatment.campusId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        mBinding.textNamePond.setText(documentSnapshot.getString(Constants.KEY_NAME) + " - ");
                    }).addOnSuccessListener(runnable ->
                            database.collection(Constants.KEY_COLLECTION_POND)
                            .document(treatment.pondId)
                            .get()
                            .addOnCompleteListener(task -> {
                                DocumentSnapshot documentSnapshot = task.getResult();
                                String name = mBinding.textNamePond.getText() +  documentSnapshot.getString(Constants.KEY_NAME);
                                mBinding.textNamePond.setText(name);
                            }));

            mBinding.textDate.setText(treatment.date.substring(8, 10) + "/" + treatment.date.substring(5, 7) +
                    "/" + treatment.date.substring(0, 4));

            if (treatment.note != null){
                mBinding.textNote.setText("Ghi chú: " + treatment.note);
            } else {
                mBinding.textNote.setVisibility(View.GONE);
            }

            if (treatment.noFood != null || treatment.suckMud != null || treatment.replaceWater != null){
                mBinding.layoutProtocol.setVisibility(View.VISIBLE);
                if (treatment.noFood != null){
                    if (!treatment.noFood.equals("")){
                        mBinding.textNoFood.setVisibility(View.VISIBLE);
                    }
                }
                if (treatment.replaceWater != null){
                    if (!treatment.replaceWater.equals("")){
                        mBinding.textReplaceWater.setVisibility(View.VISIBLE);
                    }
                }
                if (treatment.suckMud != null){
                    if (!treatment.suckMud.equals("")){
                        mBinding.textSuckMud.setVisibility(View.VISIBLE);
                    }
                }
            }
            mBinding.textGuess.setText(treatment.sickName);

            mBinding.layoutUserReport.setOnClickListener(view ->
                    database.collection(Constants.KEY_COLLECTION_USER)
                    .document(treatment.creatorId)
                    .get()
                    .addOnCompleteListener(task -> {
                       DocumentSnapshot documentSnapshot = task.getResult();
                        if (documentSnapshot.getString(Constants.KEY_DISABLE_USER) == null){
                            User user = new User();
                            user.name = documentSnapshot.getString(Constants.KEY_NAME);
                            user.phone = documentSnapshot.getString(Constants.KEY_PHONE);
                            user.position = documentSnapshot.getString(Constants.KEY_TYPE_ACCOUNT);
                            user.image = documentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = documentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id = documentSnapshot.getId();
                            Intent intent = new Intent(context, ChatActivity.class);
                            intent.putExtra(Constants.KEY_USER, user);
                            context.startActivity(intent);
                        }
                    }));

            switch (treatment.status) {
                case Constants.KEY_TREATMENT_PENDING:
                    mBinding.textStatus.setText("Chờ xử lý");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ffa96b"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fff4ec"));
                    mBinding.imageEdit.setVisibility(View.VISIBLE);
                    mBinding.imageDelete.setVisibility(View.VISIBLE);
                    mBinding.btnComplete.setVisibility(View.GONE);
                    break;
                case Constants.KEY_TREATMENT_ACCEPT:
                    mBinding.textStatus.setText("Chấp nhận");
                    mBinding.textStatus.setTextColor(Color.parseColor("#51b155"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#dff8ee"));
                    setDrawableTint(Color.parseColor("#51b155"));
                    mBinding.btnAccept.setVisibility(View.GONE);
                    mBinding.btnReject.setVisibility(View.GONE);
                    mBinding.imageEdit.setVisibility(View.GONE);
                    mBinding.imageDelete.setVisibility(View.GONE);
                    mBinding.btnComplete.setVisibility(View.VISIBLE);
                    if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR))
                        mBinding.layoutTask.setVisibility(View.VISIBLE);
                    break;
                case Constants.KEY_TREATMENT_REJECT:
                    mBinding.textStatus.setText("Từ chối");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ed444f"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fcdfe1"));
                    mBinding.imageEdit.setVisibility(View.VISIBLE);
                    mBinding.imageDelete.setVisibility(View.VISIBLE);
                    setDrawableTint(Color.parseColor("#ed444f"));
                    mBinding.btnAccept.setVisibility(View.GONE);
                    mBinding.btnReject.setVisibility(View.GONE);
                    mBinding.btnComplete.setVisibility(View.GONE);
                    break;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Medicine> medicines = new ArrayList<>();
                List<String> quantityList = new ArrayList<>();
                MedicineTreatmentUsedAdapter medicineAdapter = new MedicineTreatmentUsedAdapter(medicines, quantityList);
                mBinding.medicineRecyclerView.setAdapter(medicineAdapter);
                LinearLayoutManager layoutManager
                        = new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false);
                mBinding.medicineRecyclerView.setAdapter(medicineAdapter);
                mBinding.medicineRecyclerView.setLayoutManager(layoutManager);
                treatment.medicines.forEach((key, value) ->
                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                .whereEqualTo(Constants.KEY_POND_ID, treatment.pondId)
                                .get()
                                .addOnCompleteListener(task -> {
                                    for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                                .document(queryDocumentSnapshot.getId())
                                                .collection(Constants.KEY_COLLECTION_CATEGORY)
                                                .document(key)
                                                .get()
                                                .addOnCompleteListener(task1 -> {
                                                    DocumentSnapshot documentSnapshot = task1.getResult();
                                                    Medicine medicine = new Medicine();
                                                    medicine.id = documentSnapshot.getId();
                                                    medicine.amount = documentSnapshot.getString(Constants.KEY_AMOUNT_OF_ROOM);
                                                    medicine.effect = documentSnapshot.getString(Constants.KEY_EFFECT);
                                                    medicine.producer = documentSnapshot.getString(Constants.KEY_PRODUCER);
                                                    medicine.type = documentSnapshot.getString(Constants.KEY_CATEGORY_TYPE);
                                                    medicine.name = documentSnapshot.getString(Constants.KEY_NAME);
                                                    medicine.unit = documentSnapshot.getString(Constants.KEY_UNIT);
                                                    medicines.add(medicine);
                                                    quantityList.add(value.toString());
                                                    medicineAdapter.notifyDataSetChanged();
                                                });
                                    }
                                })
                );
            }

            mBinding.btnAccept.setOnClickListener(view -> {
                HashMap<String, Object> acceptTreatment = new HashMap<>();
                acceptTreatment.put(Constants.KEY_TREATMENT_STATUS, Constants.KEY_TREATMENT_ACCEPT);
                database.collection(Constants.KEY_COLLECTION_TREATMENT)
                        .document(treatment.id)
                        .update(acceptTreatment)
                        .addOnSuccessListener(unused -> {
                            showToast("Đã chấp nhận phác đồ điều trị!");
                            treatments.remove(treatment);
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(runnable -> showToast("Chấp nhận thất bại! Vui lòng thử lại sau!"));
            });

            mBinding.btnReject.setOnClickListener(view -> {
                HashMap<String, Object> acceptTreatment = new HashMap<>();
                acceptTreatment.put(Constants.KEY_TREATMENT_STATUS, Constants.KEY_TREATMENT_REJECT);
                database.collection(Constants.KEY_COLLECTION_TREATMENT)
                        .document(treatment.id)
                        .update(acceptTreatment)
                        .addOnSuccessListener(unused -> {
                            showToast("Đã từ chối phác đồ điều trị!");
                            treatments.remove(treatment);
                            notifyDataSetChanged();
                        })
                        .addOnFailureListener(runnable -> showToast("Từ chối thất bại! Vui lòng thử lại sau!"));
            });

            mBinding.layoutTask.setOnClickListener(view -> treatmentListener.onSelectWorker(treatment));

            mBinding.btnComplete.setOnClickListener(view -> openCompleteTreatmentDialog(treatment));

            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                mBinding.layoutUserReport.setVisibility(View.VISIBLE);
                mBinding.imageEdit.setVisibility(View.GONE);
                mBinding.imageDelete.setVisibility(View.GONE);
            } else {
                mBinding.layoutUserReport.setVisibility(View.GONE);
                mBinding.imageEdit.setVisibility(View.VISIBLE);
                mBinding.imageDelete.setVisibility(View.VISIBLE);
            }

            mBinding.imageDelete.setOnClickListener(view -> openDeleteTreatmentDialog(treatment));

            mBinding.imageEdit.setOnClickListener(view -> openEditTreatmentDialog(treatment));

            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
                mBinding.btnAccept.setVisibility(View.GONE);
                mBinding.btnReject.setVisibility(View.GONE);
                if (treatment.status.equals(Constants.KEY_TREATMENT_ACCEPT)){
                    mBinding.btnComplete.setVisibility(View.VISIBLE);
                }
            } else {
                mBinding.btnComplete.setVisibility(View.GONE);
            }
        }

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        private void openCompleteTreatmentDialog(Treatment treatment) {
            Dialog dialog = openDialog(R.layout.layout_dialog_confirm_delete_task);
            assert dialog != null;

            TextView textTitle, textMessage;
            AppCompatButton btnComplete, btnClose;

            textTitle = dialog.findViewById(R.id.textTitle);
            textMessage = dialog.findViewById(R.id.textMessage);
            btnComplete = dialog.findViewById(R.id.btnDelete);
            btnClose = dialog.findViewById(R.id.btnClose);

            textTitle.setText("Xác nhận hoàn thành điều trị");
            textMessage.setText("Bạn có chắc chắn muốn hoàn thành phác đồ điều trị này không?");

            btnComplete.setText("Hoàn thành");

            btnComplete.setOnClickListener(view -> {
                HashMap<String, Object> completed = new HashMap<>();
                completed.put(Constants.KEY_TREATMENT_STATUS, Constants.KEY_TREATMENT_COMPLETED);

                database.collection(Constants.KEY_COLLECTION_TREATMENT)
                        .document(treatment.id)
                        .update(completed)
                        .addOnSuccessListener(unused -> {
                            showToast("Đã hoàn thành điều trị!");
                            treatments.remove(treatment);
                            if (treatment.receiverIds != null){
                                for (String id : treatment.receiverIds){
                                    HashMap<String, Object> notAssignmentUser = new HashMap<>();
                                    notAssignmentUser.put(Constants.KEY_TREATMENT_ASSIGNMENT, Constants.KEY_TREATMENT_NOT_ASSIGNMENT);
                                    notAssignmentUser.put(Constants.KEY_TREATMENT_ID, "");
                                    database.collection(Constants.KEY_COLLECTION_USER)
                                            .document(id)
                                            .update(notAssignmentUser);
                                }
                            }
                            notifyDataSetChanged();
                            dialog.dismiss();
                        })
                        .addOnFailureListener(runnable -> showToast("Hoàn thành điều trị thất bại!"));
            });

            btnClose.setOnClickListener(view -> dialog.dismiss());

            dialog.show();
        }

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        private void openEditTreatmentDialog(Treatment treatment) {
            final Dialog dialog = openDialog(R.layout.layout_create_treatment_protocol_dialog);
            assert dialog != null;

            Treatment updatedTreatment = new Treatment(treatment);

            AutoCompleteTextView nameItem = dialog.findViewById(R.id.nameItem);
            AppCompatButton btnCreate, btnClose;
            TextView textDateReport, textNamePond, textTitle;
            TextInputEditText edtNote;
            CheckBox cbWater, cbFood, cbMud;
            AutoCompleteTextView edtMedicine;
            RecyclerView medicineRecyclerView;
            ConstraintLayout layoutQuantity, layoutDropdown;
            ImageView imageDropdown;

            textTitle = dialog.findViewById(R.id.textTitle);
            btnClose = dialog.findViewById(R.id.btnClose);
            btnCreate = dialog.findViewById(R.id.btnCreate);
            textDateReport = dialog.findViewById(R.id.textDateReport);
            textNamePond = dialog.findViewById(R.id.textNamePond);
            edtMedicine = dialog.findViewById(R.id.edtMedicine);
            edtNote = dialog.findViewById(R.id.edtNote);
            cbWater = dialog.findViewById(R.id.cbWater);
            cbFood = dialog.findViewById(R.id.cbFood);
            cbMud = dialog.findViewById(R.id.cbMud);
            medicineRecyclerView = dialog.findViewById(R.id.medicineRecyclerView);
            layoutQuantity = dialog.findViewById(R.id.layoutQuantityMedicine);
            layoutDropdown = dialog.findViewById(R.id.layoutDropdown);
            imageDropdown = dialog.findViewById(R.id.imageDropdown);

            textTitle.setText("Sửa phác đồ điều trị");
            btnCreate.setText("Sửa");

            if (updatedTreatment.note != null) {
                edtNote.setText(updatedTreatment.note);
            }

            if (updatedTreatment.noFood != null){
                if (!updatedTreatment.noFood.equals("")){
                    cbFood.setChecked(true);
                }
            }

            if (updatedTreatment.replaceWater != null){
                if (!updatedTreatment.replaceWater.equals("")){
                    cbWater.setChecked(true);
                }
            }

            if (updatedTreatment.suckMud != null){
                if (!updatedTreatment.suckMud.equals("")){
                    cbMud.setChecked(true);
                }
            }

            database.collection(Constants.KEY_COLLECTION_POND)
                    .document(treatment.pondId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        textNamePond.setText(documentSnapshot.getString(Constants.KEY_NAME));
                    });

            textDateReport.setText(updatedTreatment.date.substring(8, 10) + "/" + updatedTreatment.date.substring(5, 7) +
                    "/" + updatedTreatment.date.substring(0, 4));

            ArrayList<String> arrayItem = new ArrayList<>();
            arrayItem.add("Xuất huyết, phù đầu");
            arrayItem.add("Gan thận mũ");
            arrayItem.add("Trắng gan, trắng man");
            arrayItem.add("Vàng da");
            arrayItem.add("Bống hơi");
            arrayItem.add("Nội ngoại kí sinh trùng");

            ArrayAdapter<String> adapter = new ArrayAdapter<>(context,
                    android.R.layout.simple_dropdown_item_1line, arrayItem);
            adapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);
            nameItem.setAdapter(adapter);
            nameItem.showDropDown();

            nameItem.setText(treatment.sickName);

            ArrayList<Medicine> medicineItemList = new ArrayList<>();

            List<Medicine> medicinesSelected = new ArrayList<>();
            MedicineAdapter medicineAdapter = new MedicineAdapter(medicinesSelected);
            medicineRecyclerView.setAdapter(medicineAdapter);

            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                    .whereEqualTo(Constants.KEY_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID))
                    .get()
                    .addOnCompleteListener(task -> {
                        for (QueryDocumentSnapshot queryDocumentSnapshot : task.getResult()){
                            database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                    .document(queryDocumentSnapshot.getId())
                                    .collection(Constants.KEY_COLLECTION_CATEGORY)
                                    .whereEqualTo(Constants.KEY_CATEGORY_TYPE, Constants.KEY_CATEGORY_TYPE_MEDICINE)
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        for (QueryDocumentSnapshot queryDocumentSnapshot1 : task1.getResult()){
                                            Medicine medicine = new Medicine();
                                            medicine.id = queryDocumentSnapshot1.getId();
                                            medicine.amount = queryDocumentSnapshot1.getString(Constants.KEY_AMOUNT_OF_ROOM);
                                            medicine.effect = queryDocumentSnapshot1.getString(Constants.KEY_EFFECT);
                                            medicine.producer = queryDocumentSnapshot1.getString(Constants.KEY_PRODUCER);
                                            medicine.type = queryDocumentSnapshot1.getString(Constants.KEY_CATEGORY_TYPE);
                                            medicine.name = queryDocumentSnapshot1.getString(Constants.KEY_NAME);
                                            medicine.unit = queryDocumentSnapshot1.getString(Constants.KEY_UNIT);
                                            medicineItemList.add(medicine);
                                            medicineAdapter.notifyDataSetChanged();
                                        }
                                    });
                        }
                    });

            edtMedicine.setOnItemClickListener((adapterView, view, i, l) -> {
                if (!medicinesSelected.contains(medicineItemList.get(i))){
                    medicinesSelected.add(medicineItemList.get(i));
                }
                medicineAdapter.notifyDataSetChanged();
                layoutDropdown.setVisibility(View.VISIBLE);
            });

            layoutDropdown.setOnClickListener(view -> {
                if (medicineRecyclerView.getVisibility() == View.GONE){
                    medicineRecyclerView.setVisibility(View.VISIBLE);
                    layoutQuantity.setVisibility(View.VISIBLE);
                    imageDropdown.setImageResource(R.drawable.ic_down);
                } else {
                    medicineRecyclerView.setVisibility(View.GONE);
                    layoutQuantity.setVisibility(View.GONE);
                    imageDropdown.setImageResource(R.drawable.ic_up);
                }
            });

            MedicineAutoCompleteAdapter medicineAutoCompleteAdapter = new MedicineAutoCompleteAdapter(context,
                    R.layout.item_container_medicine_autocomplete, medicineItemList);
            edtMedicine.setAdapter(medicineAutoCompleteAdapter);
            edtMedicine.showDropDown();

            btnCreate.setOnClickListener(view -> {

                if (nameItem.getText().toString().equals("") ||
                        edtMedicine.getText().toString().equals("")) {
                    showToast("Vui lòng nhập đầy đủ thông tin!");
                } else {
                    boolean isValidationData = true;
                    for (int i = 0; i < medicineAdapter.getItemCount(); i++) {
                        MedicineAdapter.MedicineViewHolder viewHolder= (MedicineAdapter.MedicineViewHolder) medicineRecyclerView.findViewHolderForAdapterPosition(i);
                        assert viewHolder != null;
                        EditText edtQuantity = viewHolder.itemView.findViewById(R.id.edtQuantity);
                        if (edtQuantity.getText().toString().equals("")){
                            isValidationData = false;
                            break;
                        }
                    }

                    if (!isValidationData){
                        showToast("Vui lòng nhập số lượng thuốc cần dùng!");
                    } else {
                        HashMap<String, Object> newTreatment = new HashMap<>();
                        newTreatment.put(Constants.KEY_TREATMENT_DATE, LocalDate.now().toString());
                        newTreatment.put(Constants.KEY_AREA_ID, preferenceManager.getString(Constants.KEY_AREA_ID));
                        newTreatment.put(Constants.KEY_TREATMENT_POND_ID, updatedTreatment.pondId);
                        newTreatment.put(Constants.KEY_TREATMENT_SICK_NAME, nameItem.getText().toString());
                        newTreatment.put(Constants.KEY_TREATMENT_CREATOR_ID, preferenceManager.getString(Constants.KEY_USER_ID));
                        newTreatment.put(Constants.KEY_TREATMENT_CREATOR_IMAGE, preferenceManager.getString(Constants.KEY_IMAGE));
                        newTreatment.put(Constants.KEY_CREATOR_NAME, preferenceManager.getString(Constants.KEY_NAME));
                        newTreatment.put(Constants.KEY_CREATOR_PHONE, preferenceManager.getString(Constants.KEY_PHONE));
                        newTreatment.put(Constants.KEY_TREATMENT_CAMPUS_ID, preferenceManager.getString(Constants.KEY_CAMPUS_ID));
                        newTreatment.put(Constants.KEY_TREATMENT_NOTE, Objects.requireNonNull(edtNote.getText()).toString());
                        newTreatment.put(Constants.KEY_TREATMENT_STATUS, Constants.KEY_TREATMENT_PENDING);
                        newTreatment.put(Constants.KEY_TREATMENT_REPORT_FISH_ID, updatedTreatment.reportFishId);

                        HashMap<String, Object> medicineUsed = new HashMap<>();
                        for (int i = 0; i < medicineAdapter.getItemCount(); i++) {
                            MedicineAdapter.MedicineViewHolder viewHolder= (MedicineAdapter.MedicineViewHolder) medicineRecyclerView.findViewHolderForAdapterPosition(i);
                            assert viewHolder != null;
                            EditText edtQuantity = viewHolder.itemView.findViewById(R.id.edtQuantity);
                            medicineUsed.put(medicinesSelected.get(i).id, edtQuantity.getText().toString());
                        }
                        newTreatment.put(Constants.KEY_TREATMENT_MEDICINE, medicineUsed);

                        if (cbFood.isChecked()){
                            newTreatment.put(Constants.KEY_TREATMENT_NO_FOOD, Constants.KEY_TREATMENT_NO_FOOD);
                        } else {
                            newTreatment.put(Constants.KEY_TREATMENT_NO_FOOD, "");
                            updatedTreatment.noFood = "";
                        }

                        if (cbMud.isChecked()){
                            newTreatment.put(Constants.KEY_TREATMENT_SUCK_MUD, Constants.KEY_TREATMENT_SUCK_MUD);
                        } else {
                            newTreatment.put(Constants.KEY_TREATMENT_SUCK_MUD, "");
                            updatedTreatment.suckMud = "";
                        }

                        if (cbWater.isChecked()){
                            newTreatment.put(Constants.KEY_TREATMENT_REPLACE_WATER, Constants.KEY_TREATMENT_REPLACE_WATER);
                        } else {
                            newTreatment.put(Constants.KEY_TREATMENT_REPLACE_WATER, "");
                            updatedTreatment.replaceWater = "";
                        }

                        database.collection(Constants.KEY_COLLECTION_TREATMENT)
                                .document(updatedTreatment.id)
                                .update(newTreatment)
                                .addOnSuccessListener(runnable -> {
                                    showToast("Sửa phác đồ thành công");
                                    dialog.dismiss();
                                    treatments.remove(treatment);
                                    notifyDataSetChanged();
                                    treatments.add(treatment);
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(runnable -> showToast("Sửa phác đồ thất bại!"));
                        notifyDataSetChanged();
                    }

                }
                notifyDataSetChanged();

            });

            btnClose.setOnClickListener(view -> dialog.dismiss());
            dialog.show();
        }

        @SuppressLint({"SetTextI18n", "NotifyDataSetChanged"})
        private void openDeleteTreatmentDialog(Treatment treatment) {
            Dialog dialog = openDialog(R.layout.layout_dialog_confirm_delete_task);
            assert dialog != null;

            TextView textTitle, textMessage;
            AppCompatButton btnDelete, btnClose;

            textTitle = dialog.findViewById(R.id.textTitle);
            textMessage = dialog.findViewById(R.id.textMessage);
            btnDelete = dialog.findViewById(R.id.btnDelete);
            btnClose = dialog.findViewById(R.id.btnClose);

            textTitle.setText("Xác nhận xóa phác đồ điều trị");
            textMessage.setText("Bạn có chắc chắn muốn xóa phác đồ điều trị này không?");

            btnDelete.setOnClickListener(view ->
                    database.collection(Constants.KEY_COLLECTION_TREATMENT)
                    .document(treatment.id)
                    .delete()
                    .addOnSuccessListener(runnable -> {
                        HashMap<String, Object> updateReportFish = new HashMap<>();
                        updateReportFish.put(Constants.KEY_REPORT_STATUS, Constants.KEY_REPORT_PENDING);
                        database.collection(Constants.KEY_COLLECTION_REPORT_FISH)
                                .document(treatment.reportFishId)
                                .update(updateReportFish);
                        showToast("Xóa phác đồ thành công!");
                        dialog.dismiss();
                        treatments.remove(treatment);
                        notifyDataSetChanged();
                    }));

            btnClose.setOnClickListener(view -> dialog.dismiss());

            dialog.show();

        }

        private Dialog openDialog(int layout) {
            final Dialog dialog = new Dialog(context);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setContentView(layout);
            dialog.setCancelable(true);
            Window window = dialog.getWindow();
            if (window == null) {
                return null;
            }
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

            WindowManager.LayoutParams windowAttributes = window.getAttributes();
            windowAttributes.gravity = Gravity.CENTER;
            window.setAttributes(windowAttributes);

            return dialog;
        }

        private void showToast(String message){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        private void setDrawableTint(int color) {
            @SuppressLint("UseCompatLoadingForDrawables") Drawable drawable = context.getResources().getDrawable(R.drawable.ic_access_time);
            drawable = DrawableCompat.wrap(drawable);
            DrawableCompat.setTint(drawable, color);
            DrawableCompat.setTintMode(drawable, PorterDuff.Mode.SRC_IN);
            mBinding.textStatus.setCompoundDrawablesWithIntrinsicBounds(drawable, null, null, null);
        }

    }
}

