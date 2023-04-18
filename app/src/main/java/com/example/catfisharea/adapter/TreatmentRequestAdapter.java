package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.ItemContainerTreatmentRequestBinding;
import com.example.catfisharea.activities.alluser.ChatActivity;
import com.example.catfisharea.models.Medicine;
import com.example.catfisharea.models.Treatment;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TreatmentRequestAdapter extends RecyclerView.Adapter<TreatmentRequestAdapter.MultipleTaskSelectionViewHolder> {

    private final List<Treatment> treatments;
    private final Context context;
    private ItemContainerTreatmentRequestBinding treatmentRequestBinding;

    public TreatmentRequestAdapter(Context context, List<Treatment> treatments) {
        this.treatments = treatments;
        this.context = context;
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

            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_DIRECTOR)){
                mBinding.btnAccept.setVisibility(View.GONE);
                mBinding.btnReject.setVisibility(View.GONE);
            }

            database.collection(Constants.KEY_COLLECTION_CAMPUS)
                    .document(treatment.campusId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        mBinding.textNamePond.setText(documentSnapshot.getString(Constants.KEY_NAME) + " - ");
                    });

            database.collection(Constants.KEY_COLLECTION_POND)
                    .document(treatment.pondId)
                    .get()
                    .addOnCompleteListener(task -> {
                        DocumentSnapshot documentSnapshot = task.getResult();
                        mBinding.textNamePond.setText(mBinding.textNamePond.getText() + documentSnapshot.getString(Constants.KEY_NAME));
                    });

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
                    mBinding.textNoFood.setVisibility(View.VISIBLE);
                }
                if (treatment.replaceWater != null){
                    mBinding.textReplaceWater.setVisibility(View.VISIBLE);
                }
                if (treatment.suckMud != null){
                    mBinding.textSuckMud.setVisibility(View.VISIBLE);
                }
            }

            mBinding.imageProfile.setImageBitmap(getUserImage(treatment.creatorImage));
            mBinding.textGuess.setText(treatment.sickName);
            mBinding.textName.setText(treatment.creatorName);
            mBinding.textPhone.setText(treatment.creatorPhone);

            if (preferenceManager.getString(Constants.KEY_TYPE_ACCOUNT).equals(Constants.KEY_REGIONAL_CHIEF)){
                mBinding.layoutUserReport.setVisibility(View.VISIBLE);
            } else {
                mBinding.layoutUserReport.setVisibility(View.GONE);
            }

            mBinding.layoutUserReport.setOnClickListener(view ->
                    database.collection(Constants.KEY_COLLECTION_USER)
                    .document(treatment.creatorId)
                    .get()
                    .addOnCompleteListener(task -> {
                       DocumentSnapshot documentSnapshot = task.getResult();
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
                    }));

            switch (treatment.status) {
                case Constants.KEY_TREATMENT_PENDING:
                    mBinding.textStatus.setText("Chờ xử lý");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ffa96b"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fff4ec"));
                    break;
                case Constants.KEY_TREATMENT_ACCEPT:
                    mBinding.textStatus.setText("Chấp nhận");
                    mBinding.textStatus.setTextColor(Color.parseColor("#51b155"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#dff8ee"));
                    setDrawableTint(Color.parseColor("#51b155"));
                    mBinding.btnAccept.setVisibility(View.GONE);
                    mBinding.btnReject.setVisibility(View.GONE);
                    break;
                case Constants.KEY_TREATMENT_REJECT:
                    mBinding.textStatus.setText("Từ chối");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ed444f"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fcdfe1"));
                    setDrawableTint(Color.parseColor("#ed444f"));
                    mBinding.btnAccept.setVisibility(View.GONE);
                    mBinding.btnReject.setVisibility(View.GONE);
                    break;
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                List<Medicine> medicines = new ArrayList<>();
                List<String> quantityList = new ArrayList<>();
                MedicineTreatmentUsedAdapter medicineAdapter = new MedicineTreatmentUsedAdapter(medicines, quantityList);
                mBinding.medicineRecyclerView.setAdapter(medicineAdapter);
                treatment.medicines.forEach((key, value) ->
                        database.collection(Constants.KEY_COLLECTION_WAREHOUSE)
                                .whereEqualTo(Constants.KEY_CAMPUS_ID, treatment.campusId)
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

        }

        private void showToast(String message){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        }

        private Bitmap getUserImage(String encodedImage){
            byte[] bytes = new byte[0];
            if (encodedImage != null){
                bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            }

            return BitmapFactory.decodeByteArray(bytes,0, bytes.length);
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

