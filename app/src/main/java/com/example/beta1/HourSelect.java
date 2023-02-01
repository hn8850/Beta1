package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class HourSelect extends AppCompatActivity {

    String topHour,bottomHour;
    String parkAdID;
    ParkAd parkAd;
    TextView startTv,endTv;
    FirebaseDatabase fbDB;

    ArrayList<TimeBarView.Segment> segments;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hour_select);

        Intent gi = getIntent();
        topHour = gi.getStringExtra("beginHour");
        bottomHour = gi.getStringExtra("endHour");
        parkAdID = gi.getStringExtra("parkAdID");

        startTv = findViewById(R.id.textView6);
        endTv = findViewById(R.id.textView7);
        startTv.setText(topHour);
        endTv.setText(bottomHour);

        fbDB = FirebaseDatabase.getInstance();
        readParkAd();
        setTimeBar();



    }


    public void setTimeBar(){
        TimeBarView timeBarView = findViewById(R.id.time_bar_view);
        timeBarView.setTopNumber( getDoubleFromTimeString(topHour));
        timeBarView.setBottomNumber(getDoubleFromTimeString(bottomHour));
        getSegments();

    }

    private void getSegments(){
        segments = new ArrayList<>();
        DatabaseReference parkAdref = fbDB.getReference("Orders");

    }

    public void readParkAd() {
        DatabaseReference parkAdref = fbDB.getReference("ParkAds").child(parkAdID);
        parkAdref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkAd = snapshot.getValue(ParkAd.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    public static double getDoubleFromTimeString(String timeStr) {
        String[] timeComponents = timeStr.split(":");
        int hour = Integer.parseInt(timeComponents[0]);
        int minute = Integer.parseInt(timeComponents[1]);
        double minuteFactor = 0;
        switch (minute) {
            case 0:
                minuteFactor = 0;
                break;
            case 15:
                minuteFactor = 0.25;
                break;
            case 30:
                minuteFactor = 0.5;
                break;
            case 45:
                minuteFactor = 0.75;
                break;
            default:
                minuteFactor = 0;
        }
        return hour + minuteFactor;
    }
}