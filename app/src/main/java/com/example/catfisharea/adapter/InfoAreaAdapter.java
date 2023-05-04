package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.android.app.catfisharea.databinding.LayoutItemAreaBinding;
import com.example.catfisharea.activities.admin.ManagementAreaActivity;
import com.example.catfisharea.listeners.InfoClicked;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.Pond;
import com.example.catfisharea.ultilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class InfoAreaAdapter extends RecyclerView.Adapter<InfoAreaAdapter.InfoHolder> {
    private final List<Object> mItems;
    private final InfoClicked infoClicked;
    private final Context context;

    private boolean isDeleted = false;

    public InfoAreaAdapter(Context context, List<Object> mItems, InfoClicked infoClicked) {
        this.context = context;
        this.mItems = mItems;
        this.infoClicked = infoClicked;
    }

    @NonNull
    @Override
    public InfoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutItemAreaBinding layoutItemAreaBinding = LayoutItemAreaBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent, false);
        return new InfoHolder(layoutItemAreaBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull InfoHolder holder, int position) {
        if (mItems.get(position) instanceof Area) {
            Area area = (Area) mItems.get(position);
            if (area == null) return;
            holder.setInfo(area);
        } else if (mItems.get(position) instanceof Campus) {
            Campus campus = (Campus) mItems.get(position);
            if (campus == null) return;
            holder.setInfo(campus);
        } else {
            Pond pond = (Pond) mItems.get(position);
            if (pond == null) return;
            holder.setInfo(pond);
        }

    }

    @Override
    public int getItemCount() {
        if (mItems != null) {
            return mItems.size();
        }
        return 0;
    }

    public class InfoHolder extends RecyclerView.ViewHolder {
        private final LayoutItemAreaBinding mBinding;

        public InfoHolder(@NonNull LayoutItemAreaBinding mBinding) {
            super(mBinding.getRoot());
            this.mBinding = mBinding;
        }

        @SuppressLint("SetTextI18n")
        public void setInfo(Area area) {
            mBinding.textNameItem.setText(area.getName());
            if (area.getManager() != null) {
                mBinding.nameManager.setText(area.getManager().name + "");
            }
            mBinding.numberCampus.setText(area.getNumberCumpus() + "");
            LatLng center = area.getPolygonCenterPoint();

            mBinding.coordinates.setText(area.getDMStoDec(center.getLatitude(), center.getLongitude()));
            mBinding.showInfoBtn.setOnClickListener(v -> {
                int visible = mBinding.infoLayout.getVisibility();
                infoClicked.clickItem(area.getPolygonCenterPoint(), area.getListLatLng());
                if (visible == View.VISIBLE) {
                    mBinding.infoLayout.setVisibility(View.GONE);
                    mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_more);
                } else {
                    mBinding.infoLayout.setVisibility(View.VISIBLE);
                    mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_less);
                }
            });

            mBinding.editItemBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, ManagementAreaActivity.class);
                intent.putExtra("request", "edit");
                intent.putExtra("typeItem", Constants.KEY_AREA);
                intent.putExtra("idItem", area.getId());
                context.startActivity(intent);
            });

            if (isDeleted) {
                if (area.isSelected()) {
                    mBinding.imageSelected.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageSelected.setVisibility(View.GONE);
                }
                mBinding.layoutItem.setOnClickListener(view -> {
                    area.setSelected(!area.isSelected());
                    if (area.isSelected()) {
                        mBinding.imageSelected.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.imageSelected.setVisibility(View.GONE);
                    }
                });
            } else {
                mBinding.layoutItem.setOnClickListener(v -> {
                    infoClicked.clickItem(area.getPolygonCenterPoint(), area.getListLatLng());
                    int visible = mBinding.infoLayout.getVisibility();
                    if (visible == View.VISIBLE) {
                        mBinding.infoLayout.setVisibility(View.GONE);
                        mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_more);
                    } else {
                        mBinding.infoLayout.setVisibility(View.VISIBLE);
                        mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_less);
                    }
                });
            }

        }

        @SuppressLint("SetTextI18n")
        public void setInfo(Campus campus) {
            mBinding.textNameManager.setText("Trưởng khu");
            mBinding.textNumberCumpus.setText("Số ao");
            mBinding.textNameItem.setText(campus.getName());
            if (campus.getManager() != null) {
                mBinding.nameManager.setText(campus.getManager().name + "");
            }
            mBinding.numberCampus.setText(campus.getNumberPond() + "");
            LatLng center = campus.getPolygonCenterPoint();

            mBinding.coordinates.setText(campus.getDMStoDec(center.getLatitude(), center.getLongitude()));
            mBinding.showInfoBtn.setOnClickListener(v -> {
                int visible = mBinding.infoLayout.getVisibility();
                infoClicked.clickItem(campus.getPolygonCenterPoint(), campus.getListLatLng());
                if (visible == View.VISIBLE) {
                    mBinding.infoLayout.setVisibility(View.GONE);
                    mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_more);
                } else {
                    mBinding.infoLayout.setVisibility(View.VISIBLE);
                    mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_less);
                }
            });

            mBinding.editItemBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, ManagementAreaActivity.class);
                intent.putExtra("request", "edit");
                intent.putExtra("typeItem", Constants.KEY_CAMPUS);
                intent.putExtra("idItem", campus.getId());
                context.startActivity(intent);
            });

            if (isDeleted) {
                if (campus.isSelected()) {
                    mBinding.imageSelected.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageSelected.setVisibility(View.GONE);
                }
                mBinding.layoutItem.setOnClickListener(view -> {
                    campus.setSelected(!campus.isSelected());
                    if (campus.isSelected()) {
                        mBinding.imageSelected.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.imageSelected.setVisibility(View.GONE);
                    }
                });
            } else {
                mBinding.layoutItem.setOnClickListener(v -> {
                    infoClicked.clickItem(campus.getPolygonCenterPoint(), campus.getListLatLng());
                    int visible = mBinding.infoLayout.getVisibility();
                    if (visible == View.VISIBLE) {
                        mBinding.infoLayout.setVisibility(View.GONE);
                        mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_more);
                    } else {
                        mBinding.infoLayout.setVisibility(View.VISIBLE);
                        mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_less);
                    }
                });
            }

        }

        @SuppressLint("SetTextI18n")
        public void setInfo(Pond pond) {
            mBinding.textNameManager.setText("Khu");
            mBinding.textNumberCumpus.setText("Số công nhân");
            mBinding.textNameItem.setText(pond.getName());
            mBinding.numberCampus.setText(pond.getNumberWorker() + "");
            FirebaseFirestore database = FirebaseFirestore.getInstance();

            database.collection(Constants.KEY_COLLECTION_CAMPUS)
                    .document(pond.getIdCampus()).get().addOnSuccessListener(documentSnapshot -> {
                        String nameCampus = documentSnapshot.getString(Constants.KEY_NAME);
                        mBinding.nameManager.setText(nameCampus);
                    });
            mBinding.coordinates.setText(pond.getDMStoDec());
            mBinding.showInfoBtn.setOnClickListener(v -> {
                int visible = mBinding.infoLayout.getVisibility();
                if (visible == View.VISIBLE) {
                    mBinding.infoLayout.setVisibility(View.GONE);
                    mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_more);
                } else {
                    mBinding.infoLayout.setVisibility(View.VISIBLE);
                    mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_less);
                }
            });

            mBinding.editItemBtn.setOnClickListener(v -> {
                Intent intent = new Intent(context, ManagementAreaActivity.class);
                intent.putExtra("request", "edit");
                intent.putExtra("typeItem", Constants.KEY_POND);
                intent.putExtra("idItem", pond.getId());
                context.startActivity(intent);
            });

            if (isDeleted) {
                if (pond.isSelected()) {
                    mBinding.imageSelected.setVisibility(View.VISIBLE);
                } else {
                    mBinding.imageSelected.setVisibility(View.GONE);
                }
                mBinding.layoutItem.setOnClickListener(view -> {
                    pond.setSelected(!pond.isSelected());
                    if (pond.isSelected()) {
                        mBinding.imageSelected.setVisibility(View.VISIBLE);
                    } else {
                        mBinding.imageSelected.setVisibility(View.GONE);
                    }
                });
            } else {
                mBinding.layoutItem.setOnClickListener(v -> {
                    infoClicked.clickPond(pond.getLatLng());
                    int visible = mBinding.infoLayout.getVisibility();
                    if (visible == View.VISIBLE) {
                        mBinding.infoLayout.setVisibility(View.GONE);
                        mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_more);
                    } else {
                        mBinding.infoLayout.setVisibility(View.VISIBLE);
                        mBinding.showInfoBtn.setImageResource(R.drawable.ic_expand_less);
                    }
                });
            }

        }
    }

    public List<Area> getAreasSeleted() {
        List<Area> areas = new ArrayList<>();
        if (mItems.get(0) instanceof Area) {
            for (Object item : mItems) {
                if (((Area) item).isSelected()) {
                    areas.add((Area) item);
                }
            }
            return areas;
        }
        return null;
    }

    public List<Pond> getPondsSeleted() {
        List<Pond> ponds = new ArrayList<>();
        if (mItems.get(0) instanceof Pond) {
            for (Object item : mItems) {
                if (((Pond) item).isSelected()) {
                    ponds.add((Pond) item);
                }
            }
            return ponds;
        }
        return null;
    }

    public List<Campus> getCampuesSeleted() {
        List<Campus> campues = new ArrayList<>();
        if (mItems.get(0) instanceof Campus) {
            for (Object item : mItems) {
                if (((Campus) item).isSelected()) {
                    campues.add((Campus) item);
                }
            }
            return campues;
        }
        return null;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
        notifyDataSetChanged();
    }
}
