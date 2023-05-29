package com.example.beta1;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

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
    TextView rating;
    ImageView starIv;
    ListView listView;
    String currUserID;
    ArrayList<HashMap<String, String>> reviewHistoryDataList = new ArrayList<>();
    FirebaseDatabase fbDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review_history);
        listView = findViewById(R.id.listview1);
        rating = findViewById(R.id.rating);
        starIv = findViewById(R.id.imageView8);

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
                double sumOfStars = 0;
                double count = 0;
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    Review review = snapshot1.getValue(Review.class);
                    sumOfStars = sumOfStars + review.getStars();
                    count++;
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
                    rating.setVisibility(View.INVISIBLE);
                    starIv.setImageResource(0);
                } else {
                    double average = sumOfStars / count;
                    rating.setText("Average Ratings: " + String.valueOf(average).substring(0,4));
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