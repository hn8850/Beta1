package com.example.beta1;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 18/3/2023
 * The WriteReview Activity.
 * In this Activity, the user can submit a Review about another user.
 */

public class WriteReview extends AppCompatActivity {
    ImageView star1, star2, star3, star4, star5;
    EditText messageEt;
    FirebaseDatabase fbDB;


    int clickedStarIndex = 0;
    String message = "No Additional Info";
    String currUserUID, parkAdOwnerUID;

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
        Intent gi = getIntent();
        currUserUID = gi.getStringExtra("UID");
        parkAdOwnerUID = gi.getStringExtra("SellerID");

        fbDB = FirebaseDatabase.getInstance();
    }


    /**
     * OnClickMethod for all of the star ImageViews.
     * Clicking a star will update the ImageView's colors to yellow (from left to right).
     * This will also update the clickedStarIndex (which is basically the count of stars to be given
     * in the review).
     *
     * @param view: All of the star ImageViews.
     */
    public void onStarClick(View view) {
        ImageView clickedStar = (ImageView) view;
        // Set color of clicked star and stars to its left
        clickedStarIndex = Integer.parseInt(clickedStar.getTag().toString());
        for (int i = 1; i <= clickedStarIndex; i++) {
            int starId = getResources().getIdentifier("star" + i, "id", getPackageName());
            ImageView star = findViewById(starId);
            star.setColorFilter(Color.YELLOW);
        }
        for (int i = clickedStarIndex + 1; i <= 5; i++) {
            int starId = getResources().getIdentifier("star" + i, "id", getPackageName());
            ImageView star = findViewById(starId);
            star.setColorFilter(null);
        }
    }

    /**
     * OnClickMethod for the submit Button.
     * Used to upload the review the user has wrote to the database.
     *
     * @param view: The submit Button.
     */
    public void submit(View view) {
        if (clickedStarIndex == 0) {
            Services.ErrorAlert("Please choose some stars",WriteReview.this);
        } else {
            message = messageEt.getText().toString();
            DatabaseReference currUserPath = fbDB.getReference("Users").child(currUserUID);
            currUserPath.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    User currUser = snapshot.getValue(User.class);
                    Review review = new Review(clickedStarIndex, message, currUser.getName());
                    DatabaseReference reviewPath = fbDB.getReference("Users").child(parkAdOwnerUID).child("Reviews").child(currUserUID);
                    reviewPath.setValue(review);
                    AlertDialog.Builder adb = new AlertDialog.Builder(WriteReview.this);
                    adb.setTitle("Review Submitted!");
                    adb.setMessage("You may now return to the home screen");
                    adb.setPositiveButton("Return", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Intent si = new Intent(WriteReview.this, Navi.class);
                            startActivity(si);
                        }
                    });
                    adb.create().show();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        }
    }
}