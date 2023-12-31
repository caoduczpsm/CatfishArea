package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.RequestItemBinding;
import com.example.catfisharea.listeners.MaterialsListener;
import com.example.catfisharea.listeners.RequestListener;
import com.example.catfisharea.models.ImportRequest;
import com.example.catfisharea.models.Materials;
import com.example.catfisharea.models.RequestLeave;
import com.example.catfisharea.models.User;
import com.example.catfisharea.ultilities.Constants;
import com.example.catfisharea.ultilities.PreferenceManager;

import java.util.ArrayList;
import java.util.List;


public class RequestAdapter extends RecyclerView.Adapter<RequestAdapter.RequestHolder> {
    private final List<Object> requests;
    private final Context context;
    private final RequestListener requestListener;

    public RequestAdapter(Context context, List<Object> requests, RequestListener requestListener) {
        this.context = context;
        this.requests = requests;
        this.requestListener = requestListener;
    }

    @NonNull
    @Override
    public RequestHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RequestItemBinding requestItemBinding = RequestItemBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RequestHolder(requestItemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull RequestHolder holder, int position) {
        if (requests.get(position) instanceof RequestLeave) {
            RequestLeave requestLeave = (RequestLeave) requests.get(position);
            holder.setRequestLeave(requestLeave);
        } else if (requests.get(position) instanceof ImportRequest){
            ImportRequest requestLeave = (ImportRequest) requests.get(position);
            holder.setRequestImport(requestLeave);
        }
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class RequestHolder extends RecyclerView.ViewHolder {
        private final RequestItemBinding mBinding;
        private final PreferenceManager preferenceManager;

        public RequestHolder(RequestItemBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
            preferenceManager = new PreferenceManager(context);
        }

        @SuppressLint("SetTextI18n")
        public void setRequestLeave(RequestLeave request) {
            mBinding.nameRequest.setText("Xin nghỉ phép");
            mBinding.imageLeave.setVisibility(View.VISIBLE);
            mBinding.dateLeave.setText(request.getDateStart() + " - " + request.getDateEnd());
            mBinding.reasonRequeset.setText(request.getReason());
            mBinding.recyclerview.setVisibility(View.GONE);

            switch (request.getStatus()) {
                case Constants.KEY_PENDING:
                    mBinding.textStatus.setText("Chờ xử lý");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ffa96b"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fff4ec"));
                    mBinding.acceptBtn.setVisibility(View.VISIBLE);
                    mBinding.refuseBtn.setVisibility(View.VISIBLE);
                    break;
                case Constants.KEY_ACCEPT:
                    mBinding.textStatus.setText("Chấp nhận");
                    mBinding.textStatus.setTextColor(Color.parseColor("#51b155"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#dff8ee"));
                    setDrawableTint(Color.parseColor("#51b155"));
                    mBinding.acceptBtn.setVisibility(View.GONE);
                    mBinding.refuseBtn.setVisibility(View.GONE);
                    break;
                case Constants.KEY_REFUSE:
                    mBinding.textStatus.setText("Từ chối");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ed444f"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fcdfe1"));
                    mBinding.acceptBtn.setVisibility(View.GONE);
                    mBinding.refuseBtn.setVisibility(View.GONE);
                    setDrawableTint(Color.parseColor("#ed444f"));
                    break;
            }
            if (request.getNote() == null || request.getNote().isEmpty()) {
                mBinding.noteRequest.setVisibility(View.GONE);
            } else {
                mBinding.noteRequest.setText(request.getNote());
            }

            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(request.getRequeseter().id)) {
                mBinding.acceptBtn.setVisibility(View.GONE);
                mBinding.refuseBtn.setVisibility(View.GONE);
            }

            User user = request.getRequeseter();
            if (user == null) return;
            mBinding.imageSender.setImageBitmap(User.getUserImage(user.image));

            switch (user.position) {
                case Constants.KEY_ADMIN:
                    mBinding.textSender.setText("Admin");
                    break;
                case Constants.KEY_ACCOUNTANT:
                    mBinding.textSender.setText("Kế Toán");
                    break;
                case Constants.KEY_REGIONAL_CHIEF:
                    mBinding.textSender.setText("Trưởng Vùng");
                    break;
                case Constants.KEY_DIRECTOR:
                    mBinding.textSender.setText("Trưởng Khu");
                    break;
                case Constants.KEY_WORKER:
                    mBinding.textSender.setText("Công Nhân");
                    break;
            }

            mBinding.nameSender.setText(user.name);
            mBinding.acceptBtn.setOnClickListener(view -> requestListener.accept(request));
            mBinding.refuseBtn.setOnClickListener(view -> requestListener.refush(request));
        }

        @SuppressLint("SetTextI18n")
        public void setRequestImport(ImportRequest request) {
            mBinding.nameRequest.setText("Xin nhập vật tư");
            mBinding.imageLeave.setVisibility(View.GONE);
            mBinding.recyclerview.setVisibility(View.VISIBLE);

            switch (request.getStatus()) {
                case Constants.KEY_PENDING:
                    mBinding.textStatus.setText("Chờ xử lý");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ffa96b"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fff4ec"));
                    mBinding.acceptBtn.setVisibility(View.VISIBLE);
                    mBinding.refuseBtn.setVisibility(View.VISIBLE);
                    break;
                case Constants.KEY_ACCEPT:
                    mBinding.textStatus.setText("Chấp nhận");
                    mBinding.textStatus.setTextColor(Color.parseColor("#51b155"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#dff8ee"));
                    setDrawableTint(Color.parseColor("#51b155"));
                    mBinding.acceptBtn.setVisibility(View.GONE);
                    mBinding.refuseBtn.setVisibility(View.GONE);
                    break;
                case Constants.KEY_REFUSE:
                    mBinding.textStatus.setText("Từ chối");
                    mBinding.textStatus.setTextColor(Color.parseColor("#ed444f"));
                    mBinding.cardStatus.setCardBackgroundColor(Color.parseColor("#fcdfe1"));
                    mBinding.acceptBtn.setVisibility(View.GONE);
                    mBinding.refuseBtn.setVisibility(View.GONE);
                    setDrawableTint(Color.parseColor("#ed444f"));
                    break;
            }

            mBinding.noteRequest.setVisibility(View.GONE);
            mBinding.reasonRequeset.setVisibility(View.GONE);

            if (preferenceManager.getString(Constants.KEY_USER_ID).equals(request.getRequeseter().id)) {
                mBinding.acceptBtn.setVisibility(View.GONE);
                mBinding.refuseBtn.setVisibility(View.GONE);
            }

            User user = request.getRequeseter();
            if (user == null) return;
            mBinding.imageSender.setImageBitmap(User.getUserImage(user.image));
            switch (user.position) {
                case Constants.KEY_ADMIN:
                    mBinding.textSender.setText("Admin");
                    break;
                case Constants.KEY_ACCOUNTANT:
                    mBinding.textSender.setText("Kế Toán");
                    break;
                case Constants.KEY_REGIONAL_CHIEF:
                    mBinding.textSender.setText("Trưởng Vùng");
                    break;
                case Constants.KEY_DIRECTOR:
                    mBinding.textSender.setText("Trưởng Khu");
                    break;
                case Constants.KEY_WORKER:
                    mBinding.textSender.setText("Công Nhân");
                    break;
            }

            mBinding.nameSender.setText(user.name);
            ArrayList<Materials> materialsList = request.getMaterials();
            RequestImportItem adapter = new RequestImportItem(context, materialsList, new MaterialsListener() {
                @Override
                public void delete(Materials mtr) {

                }

                @Override
                public void edit(Materials mtr) {

                }
            });
            adapter.setChecked(true);

            mBinding.recyclerview.setAdapter(adapter);
            mBinding.recyclerview.setVisibility(View.VISIBLE);
//            materialsList = request.getMaterials();
//            adapter.notifyDataSetChanged();

            mBinding.acceptBtn.setOnClickListener(view -> requestListener.accept(request));
            mBinding.refuseBtn.setOnClickListener(view -> requestListener.refush(request));
            mBinding.layoutItem.setOnClickListener(view -> requestListener.delete(request));
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
