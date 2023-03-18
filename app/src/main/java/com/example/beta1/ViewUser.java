package com.example.beta1;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

public class ViewUser extends AppCompatActivity {

    TextInputEditText IDEt, NameEt, DateEt, PhoneEt;
    ImageView iv;
    TextView tVactive,rating;

    String  picUrl;
    int active;
    String UID;
    Uri imageUri;

    FirebaseDatabase mDb;
    FirebaseStorage mStorage;

    Intent gi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        IDEt = findViewById(R.id.etRegID);
        NameEt = findViewById(R.id.etRegName);
        DateEt = findViewById(R.id.etRegDateofBirth);
        PhoneEt = findViewById(R.id.etRegPhone);
        iv = findViewById(R.id.imageView);

        tVactive = findViewById(R.id.ActiveTv);

        rating = findViewById(R.id.rating);

        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        gi = getIntent();
        UID = gi.getStringExtra("UserID");
        readUser();
    }

    public void readUser() {
        DatabaseReference userDB = mDb.getReference("Users").child(UID);
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currentUser = snapshot.getValue(User.class);
                IDEt.setText(currentUser.getUserName());
                NameEt.setText(currentUser.getName());
                setTitle(currentUser.getUserName() + "'s Profile Page");
                DateEt.setText(currentUser.getDateOfBirth());
                PhoneEt.setText(currentUser.getPhoneNumber());
                picUrl = currentUser.getProfilePicURL();
                imageUri = Uri.parse(picUrl);
                System.out.println("WHAT = " + picUrl);
                downloadImage(picUrl, getApplicationContext());

                active = currentUser.getActive();
                if (active == 1) {
                    tVactive.setTextColor(Color.GREEN);
                    tVactive.setTextSize(20);
                    tVactive.setTypeface(null, Typeface.BOLD);
                    tVactive.setText("Active");

                } else {
                    tVactive.setTextColor(Color.RED);
                    tVactive.setTextSize(20);
                    tVactive.setTypeface(null, Typeface.BOLD);
                    tVactive.setText("Not Active");
                }

                DataSnapshot reviewsSnapshot = snapshot.child("Reviews");


                int sumOfStars = 0;
                int count = 0;
                for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    sumOfStars = sumOfStars + review.getStars();
                    count++;
                }

                if (count == 0){
                    rating.setText("No Reviews Yet!");
                }
                else{
                    double average = sumOfStars/count;
                    rating.setText("Average Ratings: " + average);
                }



            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void downloadImage(String imageUrl, final Context context) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);

        final long FIVE_MEGABYTE = 5 * 1024 * 1024;
        storageRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                iv.setImageBitmap(bitmap);
                File file = new File(context.getCacheDir(), "tempImage");
                file.delete();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors
                System.out.println("Error occured while downloading image");
            }
        });
    }

    public void goToReviewHistory(View view) {
        Intent si = new Intent(this,ReviewHistory.class);
        si.putExtra("UID",UID);
        startActivity(si);
    }


}