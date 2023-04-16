package com.example.beta1;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 8/3/2023
 * This CustomAdapter is the ListView Item design used in the ReviewHistory Activity.
 */
public class CustomReviewListAdapter extends BaseAdapter {
    private ArrayList<HashMap<String, String>> dataList;

    public CustomReviewListAdapter(ArrayList<HashMap<String, String>> dataList) {
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
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.review_desc, parent, false);
        }

        // get the data for the current position
        HashMap<String, String> data = (HashMap<String, String>) getItem(position);

        // set the data to the views in the layout
        TextView submitterTV = convertView.findViewById(R.id.submitterTextView);
        submitterTV.setText(data.get("submitter") + ":");

        TextView contentTv = convertView.findViewById(R.id.ContentTextView);
        if (data.get("content").equals(""))  contentTv.setText("No Details");
        else contentTv.setText(data.get("content"));

        ImageView star1 = convertView.findViewById(R.id.star1);
        ImageView star2 = convertView.findViewById(R.id.star2);
        ImageView star3 = convertView.findViewById(R.id.star3);
        ImageView star4 = convertView.findViewById(R.id.star4);
        ImageView star5 = convertView.findViewById(R.id.star5);
        ImageView[] stars = {star1,star2,star3,star4,star5};

        int clickedStarIndex = Integer.parseInt(data.get("pos"));
        for (int i = 0; i < clickedStarIndex; i++) {
            ImageView star = stars[i];
            star.setColorFilter(Color.YELLOW);
        }

        return convertView;
    }
}
