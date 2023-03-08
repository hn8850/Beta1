package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

public class ReceiptHistory extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> receiptHistoryDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt_history);
        listView = findViewById(R.id.listview4);

        Intent gi = getIntent();
        currUserID = gi.getStringExtra("UID");
        fbDB = FirebaseDatabase.getInstance();

        readReceiptHistory();

    }

    public void readReceiptHistory() {
        DatabaseReference userAds = fbDB.getReference("Users").child(currUserID).child("Receipts");
        userAds.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Receipt receipt = snapshot1.getValue(Receipt.class);
                    String buyerUserID = receipt.getBuyerUserID();
                    DatabaseReference sellerRef = fbDB.getReference("Users").child(buyerUserID);
                    sellerRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            User buyer = snapshot.getValue(User.class);
                            saveStringToSharedPref("buyer", buyer.getName());
                            System.out.println("name = " + buyer.getName());
                            ContinueReading(receipt);
                            CustomReceiptListAdapter adapter = new CustomReceiptListAdapter(receiptHistoryDataList);
                            listView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });


                }

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        deleteSharedPref();
    }

    public void ContinueReading(Receipt receipt) {
        HashMap<String, String> data = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        String buyerName = sharedPreferences.getString("buyer", null);
        data.put("buyer", buyerName);
        data.put("price", String.valueOf(receipt.getFinalPrice()));
        data.put("confirm", receipt.getDateOfConfirm());

        System.out.println("data =" + data.toString());
        receiptHistoryDataList.add(data);
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