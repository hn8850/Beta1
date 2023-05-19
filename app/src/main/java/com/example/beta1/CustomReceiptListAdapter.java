package com.example.beta1;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 8/3/2023
 * This CustomAdapter is the ListView Item design used in the ReceiptHistory Activity.
 */

public class CustomReceiptListAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> dataList;


    public CustomReceiptListAdapter(ArrayList<HashMap<String, String>> dataList) {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.receipt_desc, parent, false);
        }

        // get the data for the current position
        HashMap<String, String> data = (HashMap<String, String>) getItem(position);

        TextView sellerView = convertView.findViewById(R.id.sellerTextView);
        sellerView.setText("Seller: " + data.get("seller"));

        TextView buyerView = convertView.findViewById(R.id.buyerTextView);
        buyerView.setText("Buyer: " + data.get("buyer"));

        TextView priceView = convertView.findViewById(R.id.priceTextView);
        priceView.setText("Final Price: " + data.get("price") + " NIS" );

        TextView confirmDateTv = convertView.findViewById(R.id.dateOfConfirmTextView);
        confirmDateTv.setText("Confirm Date: " + data.get("confirm"));

        TextView paymentIdTv = convertView.findViewById(R.id.paymentIdTextView);
        paymentIdTv.setText("Payment ID: " + data.get("ID"));


        return convertView;
    }
}
