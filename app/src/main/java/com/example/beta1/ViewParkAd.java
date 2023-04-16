package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.internal.Storage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.4
 * @since 28/1/2023
 * The ViewParkAd Activity.
 * In this Activity, the user can view more information about the ParkAd he selected in the
 * ParkAdQueryListView Activity.
 */

public class ViewParkAd extends AppCompatActivity {

    TextView addressTv, dateTv, priceTv, descTv, titleTv;
    ImageView iv, profilePic;

    Intent gi;
    String parkAdID;
    String picURL;
    String userID;

    User user;
    ParkAd parkAd;

    FirebaseDatabase fbDB;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_park_ad);

        getSupportActionBar().hide();

        addressTv = findViewById(R.id.addressTv);
        priceTv = findViewById(R.id.priceTv);
        descTv = findViewById(R.id.descTv);
        dateTv = findViewById(R.id.dateTv);
        iv = findViewById(R.id.imageView7);
        titleTv = findViewById(R.id.title_text);
        profilePic = findViewById(R.id.right_image);
        profilePic.setOnClickListener(ProfilePicClickListener);


        gi = getIntent();
        parkAdID = gi.getStringExtra("path");

        fbDB = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        readParkAd();

    }

    /**
     * Used to read the information of the current ParkAd and set the Activity's Views accordingly.
     * The Method also reads the User Branch of the Owner of the ParkAd's Space, in order to display
     * their profile as well.
     */
    public void readParkAd() {
        DatabaseReference parkAdref = fbDB.getReference("ParkAds").child(parkAdID);
        parkAdref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkAd = snapshot.getValue(ParkAd.class);
                userID = parkAd.getUserID();
                DatabaseReference userRef = fbDB.getReference("Users").child(userID);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        user = snapshot.getValue(User.class);
                        titleTv.setText(user.getName() + "'s Parking Space");
                        picURL = user.getProfilePicURL();
                        downloadImage(picURL, getApplicationContext(), 1);
                        addressTv.setText("Address: " + parkAd.getAddress());
                        priceTv.setText("Price for hour: " + parkAd.getHourlyRate());
                        descTv.setText("Description: " + parkAd.getDescription());
                        dateTv.setText("Date: " + parkAd.getDate());

                        picURL = parkAd.getPictureUrl().get(0);
                        downloadImage(picURL, getApplicationContext(), 0);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    /**
     * SubMethod for the ReadParkAd Method.
     * Used to download an image using the given URL, linked to the Storage database, and display it
     * using an ImageView.
     *
     * @param imageUrl: The String containing the URL for the image in the Storage database.
     * @param context:  The Activity Context.
     */
    private void downloadImage(String imageUrl, final Context context, int mode) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);

        final long FIVE_MEGABYTE = 5 * 1024 * 1024;
        storageRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                if (mode == 0) iv.setImageBitmap(bitmap);
                else {
                    profilePic.setImageBitmap(bitmap);
                    Bitmap bitmap2 = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                    Bitmap circularBitmap = getCircularBitmap(bitmap2);
                    profilePic.setImageBitmap(circularBitmap);
                }
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
     * SubMethod for the downloadImage Method.
     * Used to display the user's profile picture inside of a circle.
     *
     * @param bitmap: Bitmap describing the user's profile picture.
     * @return: The Method returns the circular version of the given Bitmap.
     */
    public Bitmap getCircularBitmap(Bitmap bitmap) {
        Bitmap output;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            output = Bitmap.createBitmap(bitmap.getHeight(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
        } else {
            output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getWidth(), Bitmap.Config.ARGB_8888);
        }

        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());

        float r = 0;

        if (bitmap.getWidth() > bitmap.getHeight()) {
            r = bitmap.getHeight() / 2;
        } else {
            r = bitmap.getWidth() / 2;
        }

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawCircle(r, r, r, paint);
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);
        return output;
    }


    /**
     * OnClickMethod for the MakeOrder Button.
     * Launches the HourSelect Activity.
     *
     * @param view: The MakeOrder Button.
     */
    public void GoToMakeOrder(View view) {
        Intent si = new Intent(this, HourSelect.class);
        si.putExtra("beginHour", parkAd.getBeginHour());
        si.putExtra("endHour", parkAd.getFinishHour());
        si.putExtra("parkAdID", parkAdID);
        startActivity(si);
    }

    private View.OnClickListener ProfilePicClickListener = new View.OnClickListener() {
        /**
         * OnClickMethod for the profilePic Button.
         * Launches the ViewUser Activity.
         * @param view: The profilePic Button.
         */
        @Override
        public void onClick(View view) {
            Intent si = new Intent(getApplicationContext(), ViewUser.class);
            si.putExtra("UserID", userID);
            startActivity(si);
        }
    };
}