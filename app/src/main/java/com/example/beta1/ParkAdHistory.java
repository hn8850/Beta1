package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class ParkAdHistory extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> historicData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_ad_history);
        listView = findViewById(R.id.listview2);

        Intent gi = getIntent();
        currUserID = gi.getStringExtra("UID");

        fbDB = FirebaseDatabase.getInstance();
        readParkAdHistory();
    }

    public void readParkAdHistory() {
        DatabaseReference userAds = fbDB.getReference("Users").child(currUserID).child("ParkAds");
        userAds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ParkAd parkAd = snapshot1.getValue(ParkAd.class);
                    if (parkAd.getActive() == 0) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("date", parkAd.getDate());
                        data.put("begin", parkAd.getBeginHour());
                        data.put("end", parkAd.getFinishHour());
                        data.put("address", parkAd.getAddress());
                        data.put("price", "NONE");
                        historicData.add(data);

                    }
                }
                System.out.println("Data = " + historicData.toString());

                CustomParkAdListAdapter adapter = new CustomParkAdListAdapter(historicData);
                listView.setAdapter(adapter);
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }
}