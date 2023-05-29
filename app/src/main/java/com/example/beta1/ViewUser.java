package com.example.beta1;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import java.util.HashMap;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.4
 * @since 28/1/2023
 * The ViewUser Activity.
 * In this Activity, the user can view more information about the seller of the ParkAd he selected
 * in the ParkAdQueryListView Activity.
 */

public class ViewUser extends AppCompatActivity {

    TextView NameEt;
    ImageView profilePicIv, starIv;
    TextView rating;
    Button allReviewsBtn;
    ProgressDialog progressDialog;

    String picUrl;
    String UID;
    Uri imageUri;

    FirebaseDatabase mDb;
    FirebaseStorage mStorage;

    ListView recentReviewsList;
    ArrayList<HashMap<String, String>> recentReviewDataList = new ArrayList<>();


    Intent gi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user);
        NameEt = findViewById(R.id.etRegName);
        recentReviewsList = findViewById(R.id.reviewsList);

        TextInputLayout textInputLayout;
        EditText editText;
        int[] textInputLayoutIDs = {R.id.text2};
        for (int textInputLayoutID : textInputLayoutIDs) {
            textInputLayout = findViewById(textInputLayoutID);
            editText = textInputLayout.getEditText();
            editText.setTextColor(Color.BLACK);
            editText.setEnabled(false);
        }


        allReviewsBtn = findViewById(R.id.button10);
        profilePicIv = findViewById(R.id.imageView);
        starIv = findViewById(R.id.imageView8);
        rating = findViewById(R.id.rating);

        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        gi = getIntent();
        UID = gi.getStringExtra("UserID");
        progressDialog = new ProgressDialog(ViewUser.this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCancelable(false);
        progressDialog.show();
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
                NameEt.setText(currentUser.getName());
                setTitle(currentUser.getName() + "'s Profile Page");

                picUrl = currentUser.getProfilePicURL();
                imageUri = Uri.parse(picUrl);
                downloadImage(picUrl, getApplicationContext());

                DataSnapshot reviewsSnapshot = snapshot.child("Reviews");

                double sumOfStars = 0;
                double count = 0;
                for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
                    Review review = reviewSnapshot.getValue(Review.class);
                    if (recentReviewDataList.size() < 3) {
                        HashMap<String, String> data = new HashMap<>();
                        data.put("submitter", review.getReviewerUserName());
                        data.put("content", review.getMessage());
                        data.put("pos", String.valueOf(review.getStars()));
                        recentReviewDataList.add(data);
                    }
                    sumOfStars = sumOfStars + review.getStars();
                    count++;
                }

                if (recentReviewDataList.size() != 0) {
                    CustomReviewListAdapter adapter = new CustomReviewListAdapter(recentReviewDataList);
                    recentReviewsList.setAdapter(adapter);
                }

                if (count == 0) {
                    String[] listString = new String[]{"No Reviews Yet!"};
                    ArrayAdapter<String> adapter = new ArrayAdapter<String>(ViewUser.this, android.R.layout.simple_list_item_1, listString);
                    recentReviewsList.setAdapter(adapter);
                    rating.setVisibility(View.INVISIBLE);
                    starIv.setImageResource(0);
                    allReviewsBtn.setVisibility(View.INVISIBLE);
                    allReviewsBtn.setClickable(false);
                } else {
                    double average = sumOfStars / count;
                    rating.setText("Average Ratings: " + String.valueOf(average).substring(0,4));
                }

                progressDialog.dismiss();

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
                profilePicIv.setImageBitmap(bitmap);
                Bitmap bitmap2 = ((BitmapDrawable) profilePicIv.getDrawable()).getBitmap();
                Bitmap circularBitmap = getCircularBitmap(bitmap2);
                profilePicIv.setImageBitmap(circularBitmap);
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
     * Launches the ReviewHistory Activity.
     *
     * @param view: The ReviewHistory Button.
     */
    public void goToReviewHistory(View view) {
        Intent si = new Intent(this, ReviewHistory.class);
        si.putExtra("UID", UID);
        startActivity(si);
    }


}