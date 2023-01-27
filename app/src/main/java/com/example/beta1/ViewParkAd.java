package com.example.beta1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ViewParkAd extends AppCompatActivity {

    Intent gi;
    FirebaseDatabase fbDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_park_ad);
        gi = getIntent();
        double latitude = gi.getDoubleExtra("lat",0);
        double longitude = gi.getDoubleExtra("long",0);
        String parkAdID = String.valueOf(latitude) + String.valueOf(longitude);
        parkAdID.replace(".", "");



    }
}