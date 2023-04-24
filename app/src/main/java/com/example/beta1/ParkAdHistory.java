package com.example.beta1;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 8/3/2023
 * This Activity is designed to show the user its ParkAd history.
 */

public class ParkAdHistory extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> parkAdHistoryDataList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_park_ad_history);
        listView = findViewById(R.id.listview2);

        Intent gi = getIntent();
        currUserID = gi.getStringExtra("UID");

        fbDB = FirebaseDatabase.getInstance();
        VerifyParkAdDates();
    }

    /**
     * Method used to iterate through the ParkAds Branch of the current User in the database, and
     * update the active status of each ParkAd according to the current date.
     */
    public void VerifyParkAdDates() {
        DatabaseReference AdsDB = fbDB.getReference("ParkAds");
        AdsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ParkAd parkAd = snapshot1.getValue(ParkAd.class);
                    String currentDate = dateFormat.format(new Date());
                    currentDate = Services.addLeadingZerosToDate(currentDate, false);
                    String parkAdDateStr = parkAd.getDate();
                    parkAdDateStr = Services.addLeadingZerosToDate(parkAdDateStr, false);
                    try {
                        if (Integer.valueOf(currentDate) > Integer.valueOf(parkAdDateStr)) {
                            UpdateParkAdCompleted(snapshot1.getKey()); //parkDate has passed,hence its completed
                        } else if (parkAdDateStr.matches(currentDate)) {
                            long currentTimeMillis = System.currentTimeMillis();
                            Date current2 = new Date(currentTimeMillis);
                            String currentHour = hourFormat.format(current2);
                            if (!Services.isFirstTimeBeforeSecond(currentHour, parkAd.getFinishHour())) {
                                UpdateParkAdCompleted(snapshot1.getKey()); //ParkHour has passed,hence its completed
                            }
                        }
                    } catch (Error e) {
                    }
                }
                readParkAdHistory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * SubMethod for the SetParkAdMarkers Method. Used to update the completion status of a given
     * ParkAd to 'completed'.
     *
     * @param ParkAdID: The KeyID in the database for the completed ParkAd.
     */
    public void UpdateParkAdCompleted(String ParkAdID) {
        DatabaseReference ExpiredAd = fbDB.getReference("ParkAds").child(ParkAdID);
        ExpiredAd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ParkAd completeAd = snapshot.getValue(ParkAd.class);
                completeAd.setActive(0);
                DatabaseReference completeBranch = fbDB.getReference("Users").child(completeAd.getUserID()).child("ParkAds");
                completeBranch.child(ParkAdID).setValue(completeAd);
                ExpiredAd.setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * Method used to iterate through the now updated ParkAds Branch for the current user in the
     * database, and populate the ListView with the completed ParkAds.
     */
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
                        parkAdHistoryDataList.add(data);

                    }
                }

                if (parkAdHistoryDataList.size() == 0) {
                    String[] listString = new String[]{"Nothing to see here!"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ParkAdHistory.this, android.R.layout.simple_list_item_1, listString);
                    listView.setAdapter(adapter);
                } else {
                    CustomParkAdListAdapter adapter = new CustomParkAdListAdapter(parkAdHistoryDataList);
                    listView.setAdapter(adapter);
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}