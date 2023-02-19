package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class HourSelect extends AppCompatActivity {

    String topHour, bottomHour;
    String parkAdID, userID;
    ParkAd parkAd;
    TextView startTv, endTv;
    String beginHour, beginMinute, endHour, endMinute;
    boolean makeOrder;

    Spinner SpinBeginHour, SpinBeginMinute, SpinEndHour, SpinEndMinute;

    TimeBarView timeBarView;

    FirebaseDatabase fbDB;
    FirebaseAuth mAuth;

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

        String[] hours = new String[25];
        hours[0] = "Choose hour";
        for (int i = 1; i < 25; i++) {
            hours[i] = String.valueOf(i);
        }

        String[] minutes = {"Choose minutes", "00", "15", "30", "45"};

        SpinBeginHour = findViewById(R.id.hour1);
        SpinEndHour = findViewById(R.id.hour2);
        SpinBeginMinute = findViewById(R.id.minute1);
        SpinEndMinute = findViewById(R.id.minute2);


        SpinBeginHour.setOnItemSelectedListener(spinListener);
        SpinBeginMinute.setOnItemSelectedListener(spinListener);

        SpinEndHour.setOnItemSelectedListener(spinListener);
        SpinEndMinute.setOnItemSelectedListener(spinListener);

        ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, hours);
        SpinBeginHour.setAdapter(hourAdapter);
        SpinEndHour.setAdapter(hourAdapter);

        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, minutes);
        SpinBeginMinute.setAdapter(minuteAdapter);
        SpinEndMinute.setAdapter(minuteAdapter);

        makeOrder = false;
        readParkAd();
        setTimeBar();


    }


    public void setTimeBar() {
        timeBarView = findViewById(R.id.time_bar_view);
        timeBarView.setTopNumber(getDoubleFromTimeString(topHour));
        timeBarView.setBottomNumber(getDoubleFromTimeString(bottomHour));
        System.out.println("DOUBLE TIME = " + getDoubleFromTimeString(bottomHour));
        getSegments();

    }

    public void makeOrder(View view) {
        if (makeOrder) {
            String beginFull = beginHour + ":" + beginMinute;
            String endFull = endHour + ":" + endMinute;
            if (HourInBounds(beginFull, endFull) && !beginFull.matches(endFull)) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
                userID = CurrentUserAuth.getUid();
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Date date = new Date();
                String confirmDate = dateFormat.format(date);
                String parkDate = parkAd.getDate();
                double hourlyRate = parkAd.getHourlyRate();
                Order order = new Order(parkAdID, userID, confirmDate, parkDate, beginFull, endFull, hourlyRate);
                DatabaseReference ordersRef = fbDB.getReference("Orders");
                String key = ordersRef.push().getKey();
                ordersRef.child(key).setValue(order);
                DatabaseReference usersRef = fbDB.getReference("Users");
                usersRef.child(userID).child("Orders").child("Active Orders").child(key).setValue(order);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Order Made!");
                double finalPrice = getPrice(beginFull,endFull,hourlyRate);
                builder.setMessage("Final Price will be: " + finalPrice);
                builder.setPositiveButton("Pay Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Launch Google Pay
                        dialog.cancel();
                    }
                });

                builder.show();


            } else {
                Toast.makeText(this, "Select Hour in Available Range", Toast.LENGTH_SHORT).show();
            }
        } else Toast.makeText(this, "CHOOSE VALID TIMES", Toast.LENGTH_SHORT).show();
    }


    private void getSegments() {
        segments = new ArrayList<>();
        DatabaseReference ordersRef = fbDB.getReference("Orders");
        ordersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    if (order.getParkAdID().matches(parkAdID)) {
                        String beginHourOrder = order.getBeginHour();
                        String endHourOrder = order.getEndHour();
                        TimeBarView.Segment segment = new TimeBarView.Segment(beginHourOrder, endHourOrder);
                        timeBarView.addSegment(segment);
                        segments.add(segment);
                        timeBarView.invalidate();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


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


    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView == SpinBeginHour) {
                beginHour = adapterView.getItemAtPosition(i).toString();
            } else if (adapterView == SpinEndHour) {
                endHour = adapterView.getItemAtPosition(i).toString();
            } else if (adapterView == SpinBeginMinute) {
                beginMinute = adapterView.getItemAtPosition(i).toString();
            } else {
                endMinute = adapterView.getItemAtPosition(i).toString();
            }

            if (i == 0) makeOrder = false;
            else makeOrder = true;
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    public static double getPrice(String startTime, String endTime, double hourlyRate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date1 = sdf.parse(startTime);
            Date date2 = sdf.parse(endTime);
            long diffInMilliseconds = date2.getTime() - date1.getTime();
            double diffInMinutes = (Math.abs(diffInMilliseconds) / (1000 * 60));
            return (diffInMinutes / 60) * hourlyRate;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private boolean HourInBounds(String hour1, String hour2) {
        if (!isHourBetween(hour1, topHour, bottomHour)) {
            if (!(hour1.matches(topHour))) {
                return false;
            }
        }
        if (getDoubleFromTimeString(hour2)<=getDoubleFromTimeString(hour1)) return false;

        if (!isHourBetween(hour2, topHour, bottomHour)) {
            if (!(hour2.matches(bottomHour))) {
                return false;
            }
        }

        for (TimeBarView.Segment segment : segments) {
            System.out.println("RANGE1 = " + hour1 + " - " + hour2);
            System.out.println("RANGE2 = " + segment.getBeginHour() + " - " + segment.getEndHour());
            if (checkOverlap(hour1, hour2, segment.getBeginHour(), segment.getEndHour()))
                return false;
        }
        return true;
    }

    private static boolean isHourBetween(String checkHour, String beginHour, String endHour) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        try {
            Date check = formatter.parse(checkHour);
            Date begin = formatter.parse(beginHour);
            Date end = formatter.parse(endHour);
            if (check.after(begin) && check.before(end)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean checkOverlap(String beginHour, String endHour, String limit1, String limit2) {
        double begin = getHour(beginHour);
        double end = getHour(endHour);
        double limitOne = getHour(limit1);
        double limitTwo = getHour(limit2);

        if (begin > end) {
            return false;
        }
        if (limitOne > limitTwo) {
            double temp = limitOne;
            limitOne = limitTwo;
            limitTwo = temp;
        }
        return (begin >= limitOne && begin < limitTwo) || (end > limitOne && end <= limitTwo) || (begin <= limitOne && end >= limitTwo);
    }

    private static double getHour(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour + minute / 60.0;
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