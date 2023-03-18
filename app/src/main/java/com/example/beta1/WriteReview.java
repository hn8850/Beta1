package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class WriteReview extends AppCompatActivity {
    ImageView star1,star2,star3,star4,star5;
    EditText messageEt;
    FirebaseDatabase fbDB;


    int clickedStarIndex = 0;
    String message = "No Additional Info";
    String currUserUID,parkAdOwnerUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);
        star4 = findViewById(R.id.star4);
        star5 = findViewById(R.id.star5);
        messageEt = findViewById(R.id.reviewMessage);
        currUserUID = "Ga32GBQJKSXDM6WGVyv0L7fxEta2"; // gal
        parkAdOwnerUID = "PtRW2LnyQCXJXiuOtxx0amzlGt23"; //harel

        fbDB = FirebaseDatabase.getInstance();
    }


    public void onStarClick(View view) {
        ImageView clickedStar = (ImageView) view;


        // Set color of clicked star and stars to its left
        clickedStarIndex = Integer.parseInt(clickedStar.getTag().toString());
        for (int i = 1; i <= clickedStarIndex; i++) {
            int starId = getResources().getIdentifier("star" + i, "id", getPackageName());
            ImageView star = findViewById(starId);
            star.setColorFilter(Color.YELLOW);
        }
        for (int i = clickedStarIndex+1; i <= 5; i++) {
            int starId = getResources().getIdentifier("star" + i, "id", getPackageName());
            ImageView star = findViewById(starId);
            star.setColorFilter(null);
        }
    }

    public void submit(View view) {
        if (clickedStarIndex==0){
            Toast.makeText(this,"Choose Some Stars",Toast.LENGTH_SHORT);
        }
        else{
            message = messageEt.getText().toString();
            DatabaseReference currUserPath = fbDB.getReference("Users").child(currUserUID);
            currUserPath.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User currUser = snapshot.getValue(User.class);
                    Review review = new Review(clickedStarIndex,message,currUser.getUserName());
                    DatabaseReference reviewPath = fbDB.getReference("Users").child(parkAdOwnerUID).child("Reviews").child(currUserUID);
                    reviewPath.setValue(review);
                    Toast.makeText(getApplicationContext(),"Review Submitted",Toast.LENGTH_SHORT);

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
}