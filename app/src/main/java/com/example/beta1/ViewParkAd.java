package com.example.beta1;

import android.app.ProgressDialog;
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
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 2.0
 * @since 28/1/2023 The ViewParkAd Activity. In this Activity, the user can view more information about the ParkAd he selected in the ParkAdQueryListView Activity.
 */
public class ViewParkAd extends AppCompatActivity {


    TextView addressTv, priceTv, descTv, titleTv,dateTv;
    ImageView profilePic;
    Intent gi;

    String parkAdID;
    String profilePicURL;
    String userID;


    ImageView bigPic;
    int[] imageViewIds;
    ImageView[] imageViews;

    ProgressDialog progressDialog;
    private final Handler handler = new Handler();
    private Runnable runnable;

    User user;
    ParkAd parkAd;

    FirebaseDatabase fbDB;
    FirebaseStorage storage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_park_ad);


        addressTv = findViewById(R.id.addressTv);
        priceTv = findViewById(R.id.priceTv);
        descTv = findViewById(R.id.descTv);
        dateTv = findViewById(R.id.dateTv);
        titleTv = findViewById(R.id.titletv);

        bigPic = findViewById(R.id.imageView9);
        bigPic.setImageResource(0);
        imageViewIds = new int[5];
        imageViewIds[0] = R.id.imageView10;
        imageViewIds[1] = R.id.imageView11;
        imageViewIds[2] = R.id.imageView12;
        imageViewIds[3] = R.id.imageView13;
        imageViewIds[4] = R.id.imageView14;

        imageViews = new ImageView[imageViewIds.length];
        for (int i = 0; i < imageViewIds.length; i++) {
            imageViews[i] = findViewById(imageViewIds[i]);
            imageViews[i].setImageResource(0);
        }

        profilePic = findViewById(R.id.imageView15);
        profilePic.setImageResource(0);
        profilePic.setOnClickListener(ProfilePicClickListener);


        gi = getIntent();
        parkAdID = gi.getStringExtra("path");

        fbDB = FirebaseDatabase.getInstance();
        storage = FirebaseStorage.getInstance();
        progressDialog = new ProgressDialog(ViewParkAd.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
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
                imageViews[parkAd.getPictureUrl().size() - 1].addOnLayoutChangeListener(picLoaded);
                userID = parkAd.getUserID();
                DatabaseReference userRef = fbDB.getReference("Users").child(userID);
                userRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {

                        user = snapshot.getValue(User.class);
                        titleTv.setText(user.getName() + "'s Parking Space");
                        profilePicURL = user.getProfilePicURL();
                        downloadImage(profilePicURL, 1, 0);
                        addressTv.setText("Address: " + parkAd.getAddress());
                        priceTv.setText("Price for hour: " + parkAd.getHourlyRate() + " NIS");
                        descTv.setText(parkAd.getDescription());
                        dateTv.setText("Date: " + parkAd.getDate());

                        String parkPicURL;
                        for (int i = 0; i < parkAd.getPictureUrl().size(); i++) {
                            parkPicURL = parkAd.getPictureUrl().get(i);
                            downloadImage(parkPicURL,0, i);
                        }
                        for (int i = parkAd.getPictureUrl().size(); i < 5; i++) {
                            imageViews[i].setImageResource(0);
                            imageViews[i].setClickable(false);
                        }

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
     * @param mode: Integer used to determine if the image being downloaded is the ParkAd owner's
     *            profile pic, or one of the ParkAd's images.
     * @param index: Integer that is used in case the image being downloaded is one of the ParkAd's
     *             images. If so and in case of index being equal to 0,the image downloaded will be
     *             the default thumbnail (bigger picture).
     *             If the index is not equal to zero, the image downloaded will be at one of the
     *             smaller list of pictures.
     *
     */
    private void downloadImage(String imageUrl, int mode, int index) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReferenceFromUrl(imageUrl);

        final long FIVE_MEGABYTE = 5 * 1024 * 1024;
        storageRef.getBytes(FIVE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                try {
                    // Load the original image as a bitmap
                    Bitmap originalBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    // Determine the desired maximum image size in bytes (e.g., 500 KB)
                    int desiredMaxSizeInBytes = 500 * 1024;
                    // Calculate the appropriate image quality based on the desired maximum size
                    int quality = 100;
                    if (bytes.length > desiredMaxSizeInBytes) {
                        quality = (int) Math.ceil((float) desiredMaxSizeInBytes / (float) bytes.length * 100);
                    }
                    // Create a new ByteArrayOutputStream to compress the image
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

                    // Compress the image bitmap with the calculated quality and save it to the output stream
                    originalBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
                    // Convert the compressed image to a byte array
                    byte[] compressedImageBytes = outputStream.toByteArray();
                    // Create a bitmap from the compressed byte array
                    Bitmap compressedBitmap = BitmapFactory.decodeByteArray(compressedImageBytes, 0, compressedImageBytes.length);
                    // Set the compressed bitmap to the appropriate ImageView
                    if (mode == 0) {
                        if (index == 0) {
                            bigPic.setImageBitmap(compressedBitmap);
                        }
                        imageViews[index].setImageBitmap(compressedBitmap);
                    } else {
                        profilePic.setImageBitmap(compressedBitmap);
                        Bitmap bitmap2 = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                        Bitmap circularBitmap = getCircularBitmap(bitmap2);
                        profilePic.setImageBitmap(circularBitmap);
                    }

                    // Clean up resources
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
     * @param bitmap : Bitmap describing the user's profile picture.
     * @return the circular bitmap
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
     * @param view : The MakeOrder Button.
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
         * OnClickMethod for the profilePic ImageView.
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

    /**
     * OnClick Method for the different ParkAd pic ImageViews.
     * Used to set the big ImageView to the image that was clicked.
     *
     * @param view : The ImageView that was clicked.
     */
    public void setBigPic(View view) {
        if (view instanceof ImageView) {
            ImageView clickedView = (ImageView) view;
            bigPic.setImageDrawable(clickedView.getDrawable());
        }
    }

    /**
     * Used to verify that the last Imageview to be loaded has in fact loaded, which results in
     * closing the progress bar.
     */
    View.OnLayoutChangeListener picLoaded = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
            if (parkAd != null) {
                // Cancel the previous callback
                if (runnable != null) {
                    handler.removeCallbacks(runnable);
                }

                // Dismiss the progress dialog after a short delay
                runnable = () -> progressDialog.dismiss();
                handler.postDelayed(runnable, 700); // delay of 500ms before dismissing dialog

            }

        }
    };

}
