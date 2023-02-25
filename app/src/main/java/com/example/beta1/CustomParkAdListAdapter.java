package com.example.beta1;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomParkAdListAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> dataList;

    public CustomParkAdListAdapter(ArrayList<HashMap<String, String>> dataList) {
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        // not needed for this example
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.park_ad_desc, parent, false);
        }

        // get the data for the current position
        HashMap<String, String> data = (HashMap<String, String>) getItem(position);

        // set the data to the views in the layout
        TextView dateView = convertView.findViewById(R.id.dateTextView);
        dateView.setText(data.get("date"));

        TextView fromToView = convertView.findViewById(R.id.fromTextView);
        fromToView.setText("From: " + data.get("begin") + "   To: " + data.get("end"));

        TextView priceView = convertView.findViewById(R.id.priceTextView);
        priceView.setText(data.get("price"));

        return convertView;
    }
}



