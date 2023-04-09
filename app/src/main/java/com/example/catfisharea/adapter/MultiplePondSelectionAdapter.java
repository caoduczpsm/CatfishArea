package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.android.app.catfisharea.R;
import com.example.catfisharea.listeners.MultipleCampusListener;
import com.example.catfisharea.models.Pond;

import java.util.ArrayList;
import java.util.List;

public class MultiplePondSelectionAdapter extends RecyclerView.Adapter<MultiplePondSelectionAdapter.MultiplePondSelectionViewHolder> implements Filterable {

    private List<Pond> ponds;
    private final List<Pond> pondsOld;
    private final MultipleCampusListener multipleCampusListener;


    public MultiplePondSelectionAdapter(List<Pond> ponds, MultipleCampusListener multipleCampusListener) {
        this.ponds = ponds;
        this.pondsOld = ponds;
        this.multipleCampusListener = multipleCampusListener;
    }

    // Đánh dấu chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllSelected(List<Pond> ponds){
        this.ponds = ponds;
        for (Pond pond : ponds)
            if (!pond.isSelected())
                pond.setSelected(true);
        notifyDataSetChanged();
    }

    // Bỏ chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllUnSelected(List<Pond> ponds){
        this.ponds = ponds;
        for (Pond pond : ponds)
            if (pond.isSelected())
                pond.setSelected(false);
        notifyDataSetChanged();
    }

    // Bỏ chọn một số tài khoản khi đã thực thi xong yêu cầu và thay đổi quyền tài khoản sau khi người dùng bấm thay đổi
    @SuppressLint("NotifyDataSetChanged")
    public void setUserUnSelected(List<Pond> ponds){
        for (Pond pond : ponds){
            if (pond.isSelected())
                pond.setSelected(false);
        }
        this.ponds = ponds;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MultiplePondSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MultiplePondSelectionViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_campus,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MultiplePondSelectionViewHolder holder, int position) {
        holder.bindUserSelection(ponds.get(position));
    }

    @Override
    public int getItemCount() {
        return ponds.size();
    }

    public List<Pond> getSelectedPonds(){
        List<Pond> selectedPond = new ArrayList<>();
        for (Pond pond : ponds){
            if (pond.isSelected()){
                selectedPond.add(pond);
            }
        }
        return selectedPond;
    }

    // Tìm kiếm user
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchInput = charSequence.toString();
                if (searchInput.isEmpty()){
                    ponds = pondsOld;
                } else {
                    List<Pond> outputSearchPonds = new ArrayList<>();
                    for (Pond pond : pondsOld){
                        if (pond.getName().toLowerCase().contains(searchInput.toLowerCase())){
                            outputSearchPonds.add(pond);
                        }
                    }
                    if (outputSearchPonds.size() != 0) {
                        ponds = outputSearchPonds;
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = ponds;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                ponds = (List<Pond>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class MultiplePondSelectionViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout layoutCampusSelection;
        TextView textName;
        ImageView imageSelected;
        View viewBackground;

        MultiplePondSelectionViewHolder(@NonNull View itemView){
            super(itemView);
            layoutCampusSelection = itemView.findViewById(R.id.layoutCampusSelection);
            textName = itemView.findViewById(R.id.textName);
            imageSelected = itemView.findViewById(R.id.imageSelected);
            viewBackground = itemView.findViewById(R.id.viewBackground);
        }

        @SuppressLint("SetTextI18n")
        void bindUserSelection(final Pond pond){

            if (pond.isSelected()){
                viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                imageSelected.setVisibility(View.VISIBLE);
            } else {
                viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                imageSelected.setVisibility(View.GONE);
            }

            textName.setText(pond.getName());
            layoutCampusSelection.setOnClickListener(view -> {
                if (pond.isSelected()){
                    viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                    imageSelected.setVisibility(View.GONE);
                    pond.setSelected(false);
                    if (getSelectedPonds().size() == 0){
                        multipleCampusListener.onMultipleUserSelection(false);
                    }
                } else{
                    viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                    imageSelected.setVisibility(View.VISIBLE);
                    pond.setSelected(true);
                    multipleCampusListener.onMultipleUserSelection(true);
                }
            });
        }

    }
}

