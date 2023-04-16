package com.example.beta1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.3
 * @since 1/2/2023
 * This Activity is the final Activity for the process of making an order.
 * In this Activity, the user selects the most suited hour range that they want to reserve
 * the ParkAd space for.
 */


public class HourSelect extends AppCompatActivity {

    String topHour, bottomHour;
    String parkAdID, userID;
    ParkAd parkAd;
    TextView startTv, endTv;
    String beginHour, beginMinute, endHour, endMinute;

    Spinner SpinBeginHour, SpinBeginMinute, SpinEndHour, SpinEndMinute;

    TimeBarView timeBarView;

    FirebaseDatabase fbDB;
    FirebaseAuth mAuth;

    ArrayList<TimeBarView.Segment> segments = new ArrayList<>();


    Receipt receipt;


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
            if ((i - 1) < 10) hours[i] = "0" + (i - 1);
            else hours[i] = String.valueOf(i - 1);
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

        readParkAd();
        setTimeBar();


    }

    /**
     * Defines the custom TimeBarView in the Activity.
     * Used to show the user the which hour ranges are available for the ParkAd space and which
     * are not.
     */
    public void setTimeBar() {
        timeBarView = findViewById(R.id.time_bar_view);
        timeBarView.setTopNumber(getHour(topHour));
        timeBarView.setBottomNumber(getHour(bottomHour));
        getSegments();

    }

    /**
     * SubMethod for the setTimeBar Method.
     * Used to fill out the TimeBarView with Custom Segments, each describing a time range in which
     * the ParkAd space is already reserved for.
     */
    private void getSegments() {
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

    /**
     * The OnClick Method for the Order Button, which verifies that the time range the user has
     * selected is available, and then creates and Order Object and uploads it to the database.
     * Also launches GooglePay.
     *
     * @param view
     */
    public void makeOrder(View view) {
        if (SpinEndHour.getSelectedItemPosition() == 0 || SpinBeginHour.getSelectedItemPosition() == 0 || SpinBeginMinute.getSelectedItemPosition() == 0 || SpinEndMinute.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "CHOOSE VALID TIMES", Toast.LENGTH_SHORT).show();
        } else {
            String beginFull = beginHour + ":" + beginMinute;
            String endFull = endHour + ":" + endMinute;
            if (HourInBounds(beginFull, endFull) && !beginFull.matches(endFull)) {
                mAuth = FirebaseAuth.getInstance();
                FirebaseUser CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
                userID = CurrentUserAuth.getUid();
                String confirmDate = Services.getCurrentTimeFormatted();
                String parkDate = parkAd.getDate();
                double hourlyRate = parkAd.getHourlyRate();
                String sellerID = parkAd.getUserID();
                String parkAddress = parkAd.getAddress();

                Order order = new Order(parkAdID, userID, confirmDate, parkDate, beginFull, endFull, hourlyRate, sellerID, parkAddress);
                double finalPrice = order.getPrice();

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Nearly Finished!");
                builder.setMessage("Final Price will be: " + finalPrice);
                builder.setPositiveButton("Pay Up", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DatabaseReference ordersRef = fbDB.getReference("Orders");
                        String Orderkey = ordersRef.push().getKey();
                        ordersRef.child(Orderkey).setValue(order);
                        DatabaseReference usersRef = fbDB.getReference("Users");
                        usersRef.child(userID).child("Orders").child(Orderkey).setValue(order);

                        receipt = new Receipt(sellerID, userID, parkAdID, Orderkey, finalPrice, confirmDate, "");
                        DatabaseReference receiptRef = fbDB.getReference("Users").child(sellerID).child("Receipts");
                        String receiptKey = receiptRef.push().getKey();
                        receiptRef.child(receiptKey).setValue(receipt);

                        String[] beginHourParts = beginFull.split(":");
                        beginHourParts[0] = String.valueOf(Integer.valueOf(beginHourParts[0]) - 1);
                        String notiTime = beginHourParts[0] + ":" + beginHourParts[1]; //1 hour before Order Begin Time
                        NotificationScheduler.scheduleNotification(getApplicationContext(), "Spark Alert", "The ParkAd you ordered at " + parkAddress + " will be available in an hour!", parkDate, notiTime, 1); //Begin Noti
                        NotificationScheduler.scheduleNotification(getApplicationContext(), "Spark Alert", "Your time with the ParkAd at " + parkAddress + " has finished!", parkDate, endFull, 2); //Ending Noti
                        // Launch Google Pay

                    }
                });

                builder.show();


            } else {
                Toast.makeText(this, "Select Hour in Available Range", Toast.LENGTH_SHORT).show();
            }
        }
    }


//    private void handlePaymentSuccess(PaymentData paymentData) {
//        // Perform post-payment tasks, such as updating the order status, sending a confirmation email, etc.
//        String paymentId = paymentData.getPaymentMethodToken().getToken();
//        DatabaseReference receiptRef = fbDB.getReference("Users").child(receipt.getSellerUserID()).child("Receipts");
//        String reciptKey = receiptRef.push().getKey();
//        receipt.setPaymentID(paymentId);
//        receiptRef.child(reciptKey).setValue(receipt);
//
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("Order Made!");
//        builder.setPositiveButton("Return to Home", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent si = new Intent(getApplicationContext(), Navi.class);
//                startActivity(si);
//            }
//        });
//
//        builder.show();
//
//    }
//
//    private void handlePaymentError(Status status) {
//        // Handle the error appropriately
//        int statusCode = status.getStatusCode();
//        String errorMessage = status.getStatusMessage();
//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setTitle("uh oh! an error has occurred");
//        builder.setMessage("Error code:" + statusCode + ",Error Message:" + errorMessage);
//        builder.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
//            @Override
//            public void onClick(DialogInterface dialog, int which) {
//                Intent si = new Intent(getApplicationContext(), HourSelect.class);
//                startActivity(si);
//            }
//        });
//
//        builder.show();
//    }

    /**
     * SubMethod for the MakeOrder Method.
     * Used to read the information of the current ParkAd and copy parameters to the Order Object.
     * Also updates the TimeBarView with a Segment if the current date and the ParkAd date match.
     * (In order to make sure orders can't pass for times that have already passed).
     */
    public void readParkAd() {
        DatabaseReference parkAdref = fbDB.getReference("ParkAds").child(parkAdID);
        parkAdref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkAd = snapshot.getValue(ParkAd.class);
                SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());
                currentDate = Services.addLeadingZerosToDate(currentDate, false);
                String parkAdDateStr = parkAd.getDate();
                parkAdDateStr = Services.addLeadingZerosToDate(parkAdDateStr, false);

                if (parkAdDateStr.matches(currentDate)) {
                    long currentTimeMillis = System.currentTimeMillis();
                    Date current2 = new Date(currentTimeMillis);
                    String currentHour = hourFormat.format(current2);
                    if (Services.isHourBetween(currentHour, parkAd.getBeginHour(), parkAd.getFinishHour())) {
                        TimeBarView.Segment segment = new TimeBarView.Segment(parkAd.getBeginHour(), Services.roundToNextQuarterHour(currentHour));
                        segments.add(segment);
                        timeBarView.addSegment(segment);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * ItemSelectedListener for the hour/minute Spinners.
     * Used to update the time String params for the Order.
     */
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

        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };



    /**
     * Boolean SubMethod for the time verification process of the MakeOrder Method.
     *
     * @param hour1: Time String
     * @param hour2: Time String
     * @return: The Method returns true if the following conditions are met:
     * 1.The time described in the hour1 String is before the time described in the hour2 String.
     * 2.The time described in the hour1 String is between the The time described in the top and
     * bottom hour String of the TimeBarView.
     * 3.The time described in the hour3 String is between the The time described in the top and
     * bottom hour String of the TimeBarView.
     */
    private boolean HourInBounds(String hour1, String hour2) {
        if (!Services.isHourBetween(hour1, topHour, bottomHour)) {
            if (!(hour1.matches(topHour))) {
                return false;
            }
        }
        if (getHour(hour2) <= getHour(hour1)) return false;

        if (!Services.isHourBetween(hour2, topHour, bottomHour)) {
            if (!(hour2.matches(bottomHour))) {
                return false;
            }
        }

        for (TimeBarView.Segment segment : segments) {
            if (checkOverlap(hour1, hour2, segment.getBeginHour(), segment.getEndHour()))
                return false;
        }
        return true;
    }


    /**
     * Boolean SubMethod for the time verification process of the HourInBounds Method.
     *
     * @param beginHour: Time String
     * @param endHour:   Time String
     * @param limit1:    Time String
     * @param limit2:    Time String
     * @return: The Method returns true if the time range between the time range described in the
     * beginHour and the endHour Strings overlaps with the time range between the time range
     * described in the limit1 and limit2 Strings.
     */
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

    /**
     * Double SubMethod for the time verification process of the CheckOverlap Method.
     *
     * @param time: Time String
     * @return the double value of the given time String (e.g: the input "16:45" will return 1645).
     */
    private static double getHour(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);
        return hour + minute / 60.0;
    }


}