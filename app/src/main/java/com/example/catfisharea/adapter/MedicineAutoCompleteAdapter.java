package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.android.app.catfisharea.R;
import com.example.catfisharea.models.Medicine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class MedicineAutoCompleteAdapter extends BaseAdapter implements Filterable {

    private final Context mContext;
    private final int myLayout;
    private final List<Medicine> mList;

    public MedicineAutoCompleteAdapter(Context mContext, int myLayout, List<Medicine> mList) {
        this.mContext = mContext;
        this.myLayout = myLayout;
        this.mList = mList;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = inflater.inflate(myLayout, null);

        TextView textName = convertView.findViewById(R.id.textMedicineName);

        textName.setText(mList.get(position).name);

        return convertView;
    }

    @Override
    public Filter getFilter() {
        return customFilter;
    }

    private final Filter customFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            List<Medicine> suggestions = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                suggestions.addAll(mList);
            } else {
                String filterPattern = constraint.toString().toLowerCase().trim();

                for (Medicine item : mList) {
                    if (item.name.toLowerCase().contains(filterPattern)) {
                        suggestions.add(item);
                    }
                }
            }
            results.values = suggestions;
            results.count = suggestions.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            mList.clear();
            mList.addAll((Collection<? extends Medicine>) results.values);
            notifyDataSetChanged();
        }

        @Override
        public CharSequence convertResultToString(Object resultValue) {
            return ((Medicine) resultValue).name;

        }
    };
}



