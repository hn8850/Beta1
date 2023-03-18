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
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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

public class Settings extends AppCompatActivity {

    String picUrl;
    String UID;
    ImageView profilePic;
    Uri imageUri;
    TextView nameTv, emailTv;
    boolean signedIn;


    FirebaseAuth mAuth;
    FirebaseDatabase mDb;
    FirebaseStorage mStorage;
    FirebaseUser CurrentUserAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        profilePic = findViewById(R.id.profilePic);
        nameTv = findViewById(R.id.nameTv);
        emailTv = findViewById(R.id.emailTv);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        signedIn = false;
        if (CurrentUserAuth != null) {
            for (UserInfo userInfo : CurrentUserAuth.getProviderData()) {
                if (userInfo.getProviderId().equals("password")) {
                    UID = CurrentUserAuth.getUid();
                    signedIn = true;
                    readUser();
                }
            }
            if (!signedIn) Toast.makeText(this, "LOG IN !!!", Toast.LENGTH_SHORT).show();

        } else Toast.makeText(this, "LOG IN !!!", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        readUser();
    }

    public void readUser() {
        DatabaseReference userDB = mDb.getReference("Users").child(UID);
        userDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User currentUser = snapshot.getValue(User.class);
                if (currentUser != null) {
                    nameTv.setText(currentUser.getName());
                    emailTv.setText(CurrentUserAuth.getEmail());
                    picUrl = currentUser.getProfilePicURL();
                    imageUri = Uri.parse(picUrl);
                    System.out.println("WHAT = " + picUrl);
                    downloadImage(picUrl, getApplicationContext());

                } else
                    Toast.makeText(getApplicationContext(), "LOG IN !!!", Toast.LENGTH_SHORT).show();
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
                profilePic.setImageBitmap(bitmap);
                Bitmap bitmap2 = ((BitmapDrawable) profilePic.getDrawable()).getBitmap();
                Bitmap circularBitmap = getCircularBitmap(bitmap2);
                profilePic.setImageBitmap(circularBitmap);
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


    public void goToParkAdHistory(View view) {
        Intent si = new Intent(this,ParkAdHistory.class);
        si.putExtra("UID",UID);
        startActivity(si);
    }

    public void goToEditProfile(View view) {
        Intent si = new Intent(this,EditProfile.class);
        si.putExtra("UID",UID);
        startActivity(si);

    }

    public void goToOrderHistory(View view) {
        Intent si = new Intent(this,OrderHistory.class);
        si.putExtra("UID",UID);
        startActivity(si);
    }

    public void goToReceiptHistory(View view) {
        Intent si = new Intent(this,ReceiptHistory.class);
        si.putExtra("UID",UID);
        startActivity(si);
    }

    public void goToReviewHistory(View view) {
        Intent si = new Intent(this,ReviewHistory.class);
        si.putExtra("UID",UID);
        startActivity(si);
    }
}