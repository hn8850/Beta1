package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
 * @since 25/2/2023
 * This Activity is designed to show a list of different ParkAds for the same space.
 */

public class ParkAdQueryListView extends AppCompatActivity {

    ListView listView;
    String lat, lan;
    FirebaseDatabase fbDB;
    String beginHour, endHour, date, queryDate1, queryDate2;
    ArrayList<HashMap<String, String>> parkAdsDataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_ad_query_list_view);
        listView = findViewById(R.id.listview);
        fbDB = FirebaseDatabase.getInstance();

        Intent gi = getIntent();
        lat = gi.getStringExtra("lat");
        lan = gi.getStringExtra("long");
        if (!gi.getStringExtra("date1").matches("NONE")) {
            queryDate1 = gi.getStringExtra("date1");
            queryDate2 = gi.getStringExtra("date2");
        } else {
            queryDate1 = "1/1/1970";
            queryDate2 = "12/12/3000";
        }
        SetParkAdsDataList();
    }

    /**
     * Method used to iterate through the general ParkAds branch and populate the ListView with any
     * ParkAds that locations match the location of the ParkAd Marker pressed in the Navi Activity.
     * The Method also takes into account the user's query from the Navi Activity.
     */
    public void SetParkAdsDataList() {
        DatabaseReference parkAdsBranch = fbDB.getReference("ParkAds");
        parkAdsBranch.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ParkAd parkAd = snapshot1.getValue(ParkAd.class);
                    if (parkAd.getLatitude().matches(lat) && parkAd.getLongitude().matches(lan)) {
                        if (Services.isDateBetween(parkAd.getDate(), queryDate1, queryDate2)) {
                            HashMap<String, String> data = new HashMap<>();
                            data.put("date", parkAd.getDate());
                            data.put("begin", parkAd.getBeginHour());
                            data.put("end", parkAd.getFinishHour());
                            data.put("price", String.valueOf(parkAd.getHourlyRate()));
                            parkAdsDataList.add(data);
                        }


                    }
                }
                CustomParkAdListAdapter adapter = new CustomParkAdListAdapter(parkAdsDataList);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        HashMap<String, String> itemData = parkAdsDataList.get(i);
                        beginHour = itemData.get("begin");
                        beginHour = "B" + beginHour.substring(0, 2) + beginHour.substring(3);
                        endHour = itemData.get("end");
                        endHour = "E" + endHour.substring(0, 2) + endHour.substring(3);
                        date = itemData.get("date");
                        date = "D" + Services.addLeadingZerosToDate(date, false);
                        String locationKey = (lat + lan).replace(".", "");
                        String parkAdPath = locationKey + date + beginHour + endHour;
                        Intent si = new Intent(getApplicationContext(), ViewParkAd.class);
                        si.putExtra("path", parkAdPath);
                        startActivity(si);

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}