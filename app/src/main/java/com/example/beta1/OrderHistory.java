package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
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
import java.util.ArrayList;
import java.util.HashMap;

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
        readOrderHistory();
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

    public void ContinueReading(Order order){
        HashMap<String, String> data = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs",MODE_PRIVATE);
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

    public void saveStringToSharedPref(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void deleteSharedPref(){
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs",MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        File sharedPreferencesFile = new File(getApplicationInfo().dataDir + "/shared_prefs/my_shared_prefs.xml");
        sharedPreferencesFile.delete();
    }


}