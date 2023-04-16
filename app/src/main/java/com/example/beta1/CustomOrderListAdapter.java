package com.example.beta1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 8/3/2023
 * This CustomAdapter is the ListView Item design used in the OrderHistory and
 * ActiveOrders Activities.
 */

public class CustomOrderListAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> dataList;

    public CustomOrderListAdapter(ArrayList<HashMap<String, String>> dataList) {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.order_decs, parent, false);
        }

        // get the data for the current position
        HashMap<String, String> data = (HashMap<String, String>) getItem(position);

        // set the data to the views in the layout
        TextView renterView = convertView.findViewById(R.id.RenterName);
        renterView.setText("You Ordered " + data.get("seller") + "'s Parking Space");


        TextView addressView = convertView.findViewById(R.id.AddressTv);
        addressView.setText("Address: " + data.get("address"));

        TextView dateView = convertView.findViewById(R.id.dateTextView);
        dateView.setText("Date: " + data.get("date"));

        TextView fromToView = convertView.findViewById(R.id.fromTextView);
        fromToView.setText("From: " + data.get("begin") + "   To: " + data.get("end"));

        TextView priceView = convertView.findViewById(R.id.priceTextView);
        priceView.setText("Price per hour: " + data.get("price") );

        TextView statusTv = convertView.findViewById(R.id.statusTv);
        statusTv.setText("Status: " + data.get("status"));

        TextView confirmDateTv = convertView.findViewById(R.id.confirmDate);
        confirmDateTv.setText("Confirm Date: " + data.get("confirm"));

        return convertView;
    }
}
