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

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 18/3/2023
 * This Activity is designed to show the user its Review history.
 */

public class ReviewHistory extends AppCompatActivity {
    String currUserID;
    FirebaseDatabase fbDB;
    ListView listView;
    ArrayList<HashMap<String, String>> reviewHistoryDataList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_history);
        listView = findViewById(R.id.listview1);

        Intent gi = getIntent();
        currUserID = gi.getStringExtra("UID");

        fbDB = FirebaseDatabase.getInstance();
        readReviewHistory();
    }

    /**
     * Method used to iterate through the Reviews Branch for the current user in the
     * database, and populate the ListView with all of them.
     */
    public void readReviewHistory() {
        DatabaseReference reviewsRef = fbDB.getReference("Users").child(currUserID).child("Reviews");
        reviewsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Review review = snapshot1.getValue(Review.class);
                    HashMap<String, String> data = new HashMap<>();
                    data.put("submitter", review.getReviewerUserName());
                    data.put("content", review.getMessage());
                    data.put("pos", String.valueOf(review.getStars()));
                    reviewHistoryDataList.add(data);
                }
                if (reviewHistoryDataList.size() == 0) {
                    String[] listString = new String[]{"Nothing to see here!"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ReviewHistory.this, android.R.layout.simple_list_item_1, listString);
                    listView.setAdapter(adapter);
                } else {
                    CustomReviewListAdapter adapter = new CustomReviewListAdapter(reviewHistoryDataList);
                    listView.setAdapter(adapter);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}