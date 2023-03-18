package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class OrderHistory extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> orderHistoryDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);
        listView = findViewById(R.id.listview3);

        Intent gi = getIntent();
        currUserID = gi.getStringExtra("UID");

        fbDB = FirebaseDatabase.getInstance();
        VerifyDateOfOrders();
    }




    public void VerifyDateOfOrders() {
        DatabaseReference userOrders = fbDB.getReference("Users").child(currUserID).child("Orders");
        userOrders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    System.out.println("WORK");
                    Order order = orderSnap.getValue(Order.class);
                    String currentDate = sdf.format(new Date());
                    String parkAdDateStr = order.getParkDate();
                    System.out.println("THIS IS DATE = " + parkAdDateStr);
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("d/M/yyyy")
                            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                            .toFormatter();
                    try {
//                        Date current2 = sdf.parse(currentDate);
//                        Date parkAdDate = sdf.parse(parkAdDateStr.trim());
                        LocalDate current = LocalDate.parse(currentDate, formatter);
                        LocalDate parkAdDate = LocalDate.parse(parkAdDateStr, formatter);
                        System.out.println("current = " + current.toString() + " parkDate = " + parkAdDate.toString());
                        if (current.isAfter(parkAdDate)) {
                            System.out.println("Bad!");
                            UpdateOrderCompleted(orderSnap.getKey()); //OrderDate has passed,hence its completed
                        } else if (current.toString().equals(parkAdDate.toString())) {
                            System.out.println("good!");
                            long currentTimeMillis = System.currentTimeMillis();
                            Date current2 = new Date(currentTimeMillis);
                            String currentHour = sdf2.format(current2);
                            if (!isFirstTimeBeforeSecond(currentHour, order.getBeginHour())) {
                                if (!isHourBetween(currentHour, order.getBeginHour(), order.getEndHour())) {
                                    UpdateOrderCompleted(orderSnap.getKey()); //OrderHour has passed,hence its completed
                                }
                            }
                        }
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }
                readOrderHistory();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void UpdateOrderCompleted(String OrderID) {
        DatabaseReference finishedOrder = fbDB.getReference("Users").child(currUserID).child("Orders").child(OrderID);
        finishedOrder.child("complete").setValue(true);
        finishedOrder.child("active").setValue(false);
        DatabaseReference orderBranch = fbDB.getReference("Orders").child(OrderID);
        orderBranch.setValue(null);
    }

    public void readOrderHistory() {
        DatabaseReference userAds = fbDB.getReference("Users").child(currUserID).child("Orders");
        userAds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Order order = snapshot1.getValue(Order.class);
                    if (order.isComplete() || order.isCanceled()) {
                        String sellerId = order.getSellerId();
                        DatabaseReference sellerRef = fbDB.getReference("Users").child(sellerId);
                        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User seller = snapshot.getValue(User.class);
                                saveStringToSharedPref("seller", seller.getName());
                                System.out.println("name = " + seller.getName());
                                ContinueReading(order);
                                CustomOrderListAdapter adapter = new CustomOrderListAdapter(orderHistoryDataList);
                                listView.setAdapter(adapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });

                    }
                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        deleteSharedPref();
    }

    public void ContinueReading(Order order) {
        HashMap<String, String> data = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        String sellerName = sharedPreferences.getString("seller", null);
        data.put("seller", sellerName);
        data.put("date", order.getParkDate());
        data.put("begin", order.getBeginHour());
        data.put("end", order.getEndHour());
        data.put("address", order.getParkAddress());
        data.put("price", String.valueOf(order.getPrice()));
        if (order.isComplete()) data.put("status", "Complete");
        else data.put("status", "Canceled");
        data.put("confirm", order.getConfirmDate());
        System.out.println("data =" + data.toString());
        orderHistoryDataList.add(data);
    }


    public static boolean isFirstTimeBeforeSecond(String firstTimeStr, String secondTimeStr) {
        try {
            // Format the input strings with leading zeros for single-digit hours
            firstTimeStr = String.format("%02d", Integer.parseInt(firstTimeStr.substring(0, firstTimeStr.indexOf(":")))) + firstTimeStr.substring(firstTimeStr.indexOf(":"));
            secondTimeStr = String.format("%02d", Integer.parseInt(secondTimeStr.substring(0, secondTimeStr.indexOf(":")))) + secondTimeStr.substring(secondTimeStr.indexOf(":"));

            // Parse the time strings into LocalTime objects
            LocalTime firstTime = LocalTime.parse(firstTimeStr);
            LocalTime secondTime = LocalTime.parse(secondTimeStr);

            // Compare the LocalTime objects and return the result
            return firstTime.isBefore(secondTime);
        } catch (Error e) {
            // Handle any parse errors
            System.err.println("Error parsing time string: " + e.getMessage());
            return false;
        }
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

    private boolean doAppsExist(List<Intent> apps) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(apps.get(0), 0);
        boolean isIntentSafe = activities.size() > 0;
        if (!isIntentSafe) {
            activities = packageManager.queryIntentActivities(apps.get(1), 0);
            isIntentSafe = activities.size() > 0;
            return isIntentSafe;
        }
        return true;
    }


    public void saveStringToSharedPref(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void deleteSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        File sharedPreferencesFile = new File(getApplicationInfo().dataDir + "/shared_prefs/my_shared_prefs.xml");
        sharedPreferencesFile.delete();
    }


}