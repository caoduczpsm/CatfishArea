package com.example.catfisharea.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.android.app.catfisharea.R;
import com.example.catfisharea.models.Area;
import com.example.catfisharea.models.Campus;
import com.example.catfisharea.models.Pond;

import java.util.List;

public class SpinnerAdapter extends BaseAdapter {
    
    private final Context mContext;
    private final int myLayout;
    private final List<Object> mList;

    public SpinnerAdapter(Context mContext, int myLayout, List<Object> mList) {
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

        TextView nameArea = convertView.findViewById(R.id.nameArea);
        //TextView nameManager = convertView.findViewById(R.id.nameManager);
        if (mList.get(position) instanceof Area){
            nameArea.setText(((Area) mList.get(position)).getName());
        } else if (mList.get(position) instanceof Campus){
            nameArea.setText(((Campus) mList.get(position)).getName());
        } else {
            nameArea.setText(((Pond) mList.get(position)).getName());
        }

        return convertView;
    }
}
