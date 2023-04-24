package com.example.beta1;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
 * @version 2.1
 * @since 25/3/2023
 * This Activity is designed to show the user its currently active/future ParkAds,
 * with the option to cancel any ParkAd.
 */


public class ActiveParkAds extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> activeParkAdDataList = new ArrayList<>();
    ArrayList<String> activeParkAdIDs = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_park_ads);
        listView = findViewById(R.id.listview6);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * OnClick Method for the ActiveParkAds ListView. Clicking a ParkAd will create an
             * AlertDialog with the option to remove that parkAd.
             * @param adapterView
             * @param view
             * @param pos
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ActiveParkAds.this);
                adb.setTitle("You Have Selected a ParkAd");
                adb.setNeutralButton("Go Back", new DialogInterface.OnClickListener() {
                    /** AlertDialog Click Method to close the AlertDialog box.
                     *
                     * @param dialogInterface: The AlertDialog Created.
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
                adb.setNegativeButton("Remove ParkAd", new DialogInterface.OnClickListener() {
                    /** AlertDialog Click Method to remove a ParkAd.
                     *
                     * @param dialogInterface: The AlertDialog Created.
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String canceledParkAdID = activeParkAdIDs.get(pos);
                        DatabaseReference canceledParkAdRef4User = fbDB.getReference("Users").child(currUserID).child("ParkAds").child(canceledParkAdID);
                        canceledParkAdRef4User.child("active").setValue(0);
                        DatabaseReference canceledParkAdRefGeneral = fbDB.getReference("ParkAds").child(canceledParkAdID);
                        canceledParkAdRefGeneral.setValue(null);
                        activeParkAdDataList.remove(pos);
                        CustomParkAdListAdapter adapter = new CustomParkAdListAdapter(activeParkAdDataList);
                        listView.setAdapter(adapter);
                        AlertDialog.Builder adb2 =new AlertDialog.Builder(ActiveParkAds.this);
                        adb2.setTitle("ParkAd Removed.");
                        adb2.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface2, int i) {
                                dialogInterface2.dismiss();
                            }
                        });
                        adb2.create().show();
                        dialogInterface.dismiss();
                    }
                });
                AlertDialog dialog = adb.create();
                dialog.show();
            }
        });


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
                readActiveParkAds();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * SubMethod for the VerifyParkAdDates Method. Used to update the completion status of a given
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
     * database, and populate the ListView with the active ParkAds.
     */
    public void readActiveParkAds() {
        DatabaseReference userAds = fbDB.getReference("Users").child(currUserID).child("ParkAds");
        userAds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ParkAd parkAd = snapshot1.getValue(ParkAd.class);
                    if (parkAd.getActive() == 1) {
                        activeParkAdIDs.add(snapshot1.getKey());
                        HashMap<String, String> data = new HashMap<>();
                        data.put("date", parkAd.getDate());
                        data.put("begin", parkAd.getBeginHour());
                        data.put("end", parkAd.getFinishHour());
                        data.put("address", parkAd.getAddress());
                        data.put("price", "NONE");
                        activeParkAdDataList.add(data);

                    }
                }

                if (activeParkAdDataList.size()==0){
                    String[] listString = new String[]{"Nothing to see here!"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ActiveParkAds.this, android.R.layout.simple_list_item_1, listString);
                    listView.setAdapter(adapter);
                }
                else{
                    CustomParkAdListAdapter adapter = new CustomParkAdListAdapter(activeParkAdDataList);
                    listView.setAdapter(adapter);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

}