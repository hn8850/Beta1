package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 8/3/2023
 * This Activity is designed to show the user its receipt history.
 */

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

    /**
     * Method used to iterate through the Receipts Branch for the current user in the
     * database, and populate the ListView with all of them.
     */
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
                            ContinueReading(receipt);
                            CustomReceiptListAdapter adapter = new CustomReceiptListAdapter(receiptHistoryDataList);
                            listView.setAdapter(adapter);
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
                if (receiptHistoryDataList.size()==0){
                    String[] listString = new String[]{"Nothing to see here!"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ReceiptHistory.this, android.R.layout.simple_list_item_1, listString);
                    listView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        deleteSharedPref();
    }

    /**
     * SubMethod for the readReceiptHistory Method. Used to create a HashMap for each receipt
     * and add it to the receiptHistoryDataList.
     *
     * @param receipt: The Receipt Object that was read from the database.
     */
    public void ContinueReading(Receipt receipt) {
        HashMap<String, String> data = new HashMap<>();
        SharedPreferences sharedPreferences = getSharedPreferences("my_shared_prefs", MODE_PRIVATE);
        String buyerName = sharedPreferences.getString("buyer", null);
        data.put("seller",receipt.getSellerName());
        data.put("buyer", buyerName);
        data.put("price", String.valueOf(receipt.getFinalPrice()));
        data.put("confirm", receipt.getDateOfConfirm());
        data.put("ID",receipt.getPaymentID());

        receiptHistoryDataList.add(data);
    }


    /**
     * SubMethod for readOrderHistory Method. Used to save information about an Order Object for the
     * orderHistoryDataList.
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
     * SubMethod for readOrderHistory Method. Used to delete the SharedPrefs file created for saving
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