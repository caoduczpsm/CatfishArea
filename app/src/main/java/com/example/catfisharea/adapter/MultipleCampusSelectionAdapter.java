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
import com.example.catfisharea.models.Campus;

import java.util.ArrayList;
import java.util.List;

public class MultipleCampusSelectionAdapter extends RecyclerView.Adapter<MultipleCampusSelectionAdapter.MultipleCampusSelectionViewHolder> implements Filterable {

    private List<Campus> campuses;
    private final List<Campus> campusesOld;
    private final MultipleCampusListener multipleCampusListener;


    public MultipleCampusSelectionAdapter(List<Campus> campuses, MultipleCampusListener multipleCampusListener) {
        this.campuses = campuses;
        this.campusesOld = campuses;
        this.multipleCampusListener = multipleCampusListener;
    }

    // Đánh dấu chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllSelected(List<Campus> campuses){
        this.campuses = campuses;
        for (Campus campus : campuses)
            if (!campus.isSelected())
                campus.setSelected(true);
        notifyDataSetChanged();
    }

    // Bỏ chọn tất cả tài khoản
    @SuppressLint("NotifyDataSetChanged")
    public void setAllUnSelected(List<Campus> campuses){
        this.campuses = campuses;
        for (Campus campus : campuses)
            if (campus.isSelected())
                campus.setSelected(false);
        notifyDataSetChanged();
    }

    // Bỏ chọn một số tài khoản khi đã thực thi xong yêu cầu và thay đổi quyền tài khoản sau khi người dùng bấm thay đổi
    @SuppressLint("NotifyDataSetChanged")
    public void setUserUnSelected(List<Campus> campuses){
        for (Campus campus : campuses){
            if (campus.isSelected())
                campus.setSelected(false);
        }
        this.campuses = campuses;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MultipleCampusSelectionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new MultipleCampusSelectionViewHolder(
                LayoutInflater.from(parent.getContext()).inflate(
                        R.layout.item_container_campus,
                        parent,
                        false
                )
        );
    }

    @Override
    public void onBindViewHolder(@NonNull MultipleCampusSelectionViewHolder holder, int position) {
        holder.bindUserSelection(campuses.get(position));
    }

    @Override
    public int getItemCount() {
        return campuses.size();
    }

    public List<Campus> getSelectedCampuses(){
        List<Campus> selectedCampus = new ArrayList<>();
        for (Campus campus : campuses){
            if (campus.isSelected()){
                selectedCampus.add(campus);
            }
        }
        return selectedCampus;
    }

    // Tìm kiếm user
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String searchInput = charSequence.toString();
                if (searchInput.isEmpty()){
                    campuses = campusesOld;
                } else {
                    List<Campus> outputSearchCampuses  = new ArrayList<>();
                    for (Campus campus : campusesOld){
                        if (campus.getName().toLowerCase().contains(searchInput.toLowerCase())){
                            outputSearchCampuses.add(campus);
                        }
                    }
                    if (outputSearchCampuses.size() != 0) {
                        campuses = outputSearchCampuses;
                    }
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = campuses;
                return filterResults;
            }

            @SuppressLint("NotifyDataSetChanged")
            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                campuses = (List<Campus>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

    class MultipleCampusSelectionViewHolder extends RecyclerView.ViewHolder{

        ConstraintLayout layoutCampusSelection;
        TextView textName;
        ImageView imageSelected;
        View viewBackground;

        MultipleCampusSelectionViewHolder(@NonNull View itemView){
            super(itemView);
            layoutCampusSelection = itemView.findViewById(R.id.layoutCampusSelection);
            textName = itemView.findViewById(R.id.textName);
            imageSelected = itemView.findViewById(R.id.imageSelected);
            viewBackground = itemView.findViewById(R.id.viewBackground);
        }

        @SuppressLint("SetTextI18n")
        void bindUserSelection(final Campus campus){

            if (campus.isSelected()){
                viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                imageSelected.setVisibility(View.VISIBLE);
            } else {
                viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                imageSelected.setVisibility(View.GONE);
            }

            textName.setText(campus.getName());
            layoutCampusSelection.setOnClickListener(view -> {
                if (campus.isSelected()){
                    viewBackground.setBackgroundResource(R.drawable.user_selection_background);
                    imageSelected.setVisibility(View.GONE);
                    campus.setSelected(false);
                    if (getSelectedCampuses().size() == 0){
                        multipleCampusListener.onMultipleUserSelection(false);
                    }
                } else{
                    viewBackground.setBackgroundResource(R.drawable.background_user_selected);
                    imageSelected.setVisibility(View.VISIBLE);
                    campus.setSelected(true);
                    multipleCampusListener.onMultipleUserSelection(true);
                }
            });
        }

    }
}

