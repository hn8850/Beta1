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
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 2.1
 * @since 24/2/2023
 * This Activity servers as a hub for Activities and processes regarding personal user information -
 * (Activities such as the different histories and EditProfile).
 */

public class SettingsScreen extends AppCompatActivity {

    String picUrl;
    String UID;
    ImageView profilePic;
    Uri imageUri;
    TextView nameTv, emailTv;
    ProgressDialog progressDialog;


    FirebaseAuth mAuth;
    FirebaseDatabase mDb;
    FirebaseStorage mStorage;
    FirebaseUser CurrentUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        profilePic = findViewById(R.id.profilePic);
        profilePic.setImageResource(0);
        nameTv = findViewById(R.id.nameTv);
        nameTv.setText("");
        emailTv = findViewById(R.id.emailTv);
        emailTv.setText("");

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
            for (UserInfo userInfo : CurrentUserAuth.getProviderData()) {
                if (userInfo.getProviderId().equals("password")) {
                    UID = CurrentUserAuth.getUid();
                    progressDialog = new ProgressDialog(SettingsScreen.this);
                    progressDialog.setMessage("Loading...");
                    progressDialog.setCancelable(false);
                    progressDialog.show();
                    readUser();
                }
            }
    }

    @Override
    protected void onResume() {
        super.onResume();
        readUser();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent si = new Intent(this, Navi.class);
        startActivity(si);
    }

    /**
     * The Method reads the current user's information from the database and sets the views in the
     * Activity according to that information.
     */
    public void readUser() {
        DatabaseReference userDB = mDb.getReference("Users").child(UID);
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currentUser = snapshot.getValue(User.class);
                nameTv.setText(currentUser.getName());
                emailTv.setText(CurrentUserAuth.getEmail());
                picUrl = currentUser.getProfilePicURL();
                imageUri = Uri.parse(picUrl);
                downloadImage(picUrl, getApplicationContext());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * SubMethod for the ReadUser Method.
     * Used to download the user's profile picture from the Storage database and display it
     * using an ImageView.
     *
     * @param imageUrl: The String containing the URL for the user's profile picture in the
     *                  Storage database.
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
                profilePic.setImageBitmap(bitmap);
                Bitmap bitmap2 = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                Bitmap circularBitmap = getCircularBitmap(bitmap2);
                profilePic.setImageBitmap(circularBitmap);
                File file = new File(context.getCacheDir(), "tempImage");
                file.delete();
                progressDialog.dismiss();
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
     * Launches the EditProfile Activity.
     *
     * @param view: The EditProfile Button.
     */
    public void goToEditProfile(View view) {
        Intent si = new Intent(this, EditProfile.class);
        si.putExtra("UID", UID);
        startActivity(si);

    }

    /**
     * Launches the ActiveOrders Activity.
     *
     * @param view: The ActiveOrders Button.
     */
    public void goToActiveOrders(View view) {
        Intent si = new Intent(this, ActiveOrders.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }

    /**
     * Launches the ActiveParkAds Activity.
     *
     * @param view: The ActiveParkAds Button.
     */
    public void goToActiveParkAds(View view) {
        Intent si = new Intent(this, ActiveParkAds.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }

    /**
     * Launches the OrderHistory Activity.
     *
     * @param view: The OrderHistory Button.
     */
    public void goToOrderHistory(View view) {
        Intent si = new Intent(this, OrderHistory.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }

    /**
     * Launches the ParkAdHistory Activity.
     *
     * @param view: The ParkAdHistory Button.
     */
    public void goToParkAdHistory(View view) {
        Intent si = new Intent(this, ParkAdHistory.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }


    /**
     * Launches the ReceiptHistory Activity.
     *
     * @param view: The ReceiptHistory Button.
     */
    public void goToReceiptHistory(View view) {
        Intent si = new Intent(this, ReceiptHistory.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }

    /**
     * Launches the ReviewHistory Activity.
     *
     * @param view: The ReviewHistory Button.
     */
    public void goToReviewHistory(View view) {
        Intent si = new Intent(this, ReviewHistory.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        mAuth.signOut();
        Intent si = new Intent(SettingsScreen.this,Login.class);
        startActivity(si);
        return  true;
    }


}