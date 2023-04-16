package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

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
import java.util.Locale;


/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 2.1
 * @since 25/3/2023
 * This Activity is designed to show the user its currently active/future orders,
 * with the option to cancel any order.
 */


public class ActiveOrders extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> activeOrdersDataList = new ArrayList<>();
    ArrayList<String> orderIDs = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_orders);
        listView = findViewById(R.id.listview5);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            /**
             * OnClick Method for the ActiveOrders ListView. Clicking an Order will create an
             * AlertDialog with the option to cancel that order.
             * @param adapterView
             * @param view
             * @param pos
             * @param l
             */
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long l) {
                AlertDialog.Builder adb = new AlertDialog.Builder(ActiveOrders.this);
                adb.setTitle("You Have Selected an Order");
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
                adb.setNegativeButton("Cancel Order", new DialogInterface.OnClickListener() {
                    /** AlertDialog Click Method to cancel an order.
                     *
                     * @param dialogInterface: The AlertDialog Created.
                     * @param i
                     */
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String canceledOrderID = orderIDs.get(pos);
                        DatabaseReference canceledOrderRef = fbDB.getReference("Users").child(currUserID).child("Orders").child(canceledOrderID);
                        canceledOrderRef.child("canceled").setValue(true);
                        Toast.makeText(ActiveOrders.this, "Order Canceled", Toast.LENGTH_SHORT);
                        activeOrdersDataList.remove(pos);
                        CustomOrderListAdapter adapter = new CustomOrderListAdapter(activeOrdersDataList);
                        listView.setAdapter(adapter);
                    }
                });
                AlertDialog dialog = adb.create();
                dialog.show();
            }
        });


        Intent gi = getIntent();
        currUserID = gi.getStringExtra("UID");

        fbDB = FirebaseDatabase.getInstance();
        VerifyDateOfOrders();
    }

    /**
     * Method used to iterate through the Orders Branch of the current User in the database, and
     * update the completion status of each order according to the current date.
     */
    public void VerifyDateOfOrders() {
        DatabaseReference userOrders = fbDB.getReference("Users").child(currUserID).child("Orders");
        userOrders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    String currentDate = sdf.format(new Date());
                    String parkAdDateStr = order.getParkDate();
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("d/M/yyyy")
                            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                            .toFormatter();
                    try {
                        LocalDate current = LocalDate.parse(currentDate, formatter);
                        LocalDate parkAdDate = LocalDate.parse(parkAdDateStr, formatter);
                        if (current.isAfter(parkAdDate)) {
                            UpdateOrderCompleted(orderSnap.getKey()); //OrderDate has passed,hence its completed
                        } else if (current.toString().equals(parkAdDate.toString())) {
                            long currentTimeMillis = System.currentTimeMillis();
                            Date current2 = new Date(currentTimeMillis);
                            String currentHour = sdf2.format(current2);
                            if (!Services.isFirstTimeBeforeSecond(currentHour, order.getBeginHour())) {
                                if (!Services.isHourBetween(currentHour, order.getBeginHour(), order.getEndHour())) {
                                    UpdateOrderCompleted(orderSnap.getKey()); //OrderHour has passed,hence its completed
                                }
                            }
                        }
                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }
                readActiveOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * SubMethod for the VerifyDateOfOrders Method. Used to update the completion status of a given
     * order to 'completed'.
     *
     * @param OrderID: The KeyID in the database for the completed order.
     */
    public void UpdateOrderCompleted(String OrderID) {
        DatabaseReference finishedOrder = fbDB.getReference("Users").child(currUserID).child("Orders").child(OrderID);
        finishedOrder.child("complete").setValue(true);
        finishedOrder.child("active").setValue(false);
        DatabaseReference orderBranch = fbDB.getReference("Orders").child(OrderID);
        orderBranch.setValue(null);
    }

    /**
     * Method used to iterate through the now updated Orders Branch for the current user in the
     * database, and populate the ListView with the active orders.
     */
    public void readActiveOrders() {
        DatabaseReference userAds = fbDB.getReference("Users").child(currUserID).child("Orders");
        userAds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Order order = snapshot1.getValue(Order.class);
                    if (!(order.isComplete()) && !(order.isCanceled())) {
                        orderIDs.add(snapshot1.getKey());
                        String sellerId = order.getSellerId();
                        DatabaseReference sellerRef = fbDB.getReference("Users").child(sellerId);
                        sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                User seller = snapshot.getValue(User.class);
                                saveStringToSharedPref("seller", seller.getName());
                                ContinueReading(order);
                                CustomOrderListAdapter adapter = new CustomOrderListAdapter(activeOrdersDataList);
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

    /**
     * SubMethod for the readActiveOrders Method. Used to create a HashMap for each active/future
     * order and add it to the activeOrdersDataList.
     *
     * @param order: The Order Object that was read from the database.
     */
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
        if (order.isActive()) data.put("status", "Active");
        else data.put("status", "Future Order");
        data.put("confirm", order.getConfirmDate());
        activeOrdersDataList.add(data);
    }





    /**
     * SubMethod for readActiveOrders Method. Used to save information about an Order Object for the
     * activeOrdersDataList.
     *
     * @param key:   The key of the information to be saved.
     * @param value: The value of the information to be saved.
     */
    public void saveStringToSharedPref(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * SubMethod for readActiveOrders Method. Used to delete the SharedPrefs file created for saving
     * Order information,in order to clear up space.
     */
    public void deleteSharedPref() {
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        File sharedPreferencesFile = new File(getApplicationInfo().dataDir + "/shared_prefs/my_shared_prefs.xml");
        sharedPreferencesFile.delete();
    }


}