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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.4
 * @since 28/1/2023
 * The ViewUser Activity.
 * In this Activity, the user can view more information about the seller of the ParkAd he selected
 * in the ParkAdQueryListView Activity.
 */

public class ViewUser extends AppCompatActivity {

    TextView IDEt, NameEt, DateEt, PhoneEt;
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

        TextInputLayout textInputLayout;
        EditText editText;
        int[] textInputLayoutIDs = {R.id.text1,R.id.text2,R.id.text3,R.id.text4};
        for (int i=0;i<textInputLayoutIDs.length;i++){
            textInputLayout = findViewById(textInputLayoutIDs[i]);
            editText = textInputLayout.getEditText();
            editText.setTextColor(Color.BLACK);
            editText.setEnabled(false);
        }


        iv = findViewById(R.id.imageView);

        tVactive = findViewById(R.id.ActiveTv);

        rating = findViewById(R.id.rating);

        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        gi = getIntent();
        UID = gi.getStringExtra("UserID");
        readUser();
    }

    /**
     * The Method reads the  user's information from the database and sets the views in the
     * Activity according to that information.
     */
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

    /**
     * SubMethod for the ReadUser Method.
     * Used to download an image using the given URL, linked to the Storage database, and display it
     * using an ImageView.
     *
     * @param imageUrl: The String containing the URL for the image in the Storage database.
     * @param context:  The Activity Context.
     */
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
            }
        });
    }

    /**
     * Launches the ReviewHistory Activity.
     *
     * @param view: The ReviewHistory Button.
     */
    public void goToReviewHistory(View view) {
        Intent si = new Intent(this,ReviewHistory.class);
        si.putExtra("UID",UID);
        startActivity(si);
    }


}