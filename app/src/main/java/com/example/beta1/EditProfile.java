package com.example.beta1;

import android.content.Context;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.1
 * @since 30/12/2022
 * This Activity is allow the user to edit their profile information.
 */

public class EditProfile extends AppCompatActivity {

    TextInputEditText IDEt, NameEt, DateEt, PhoneEt;
    Switch sw;

    String userName, name, date, phone, picUrl;
    int active;
    String UID;
    ImageView iv;
    Uri imageUri;
    TextView tVactive, tVNotActive;


    final static int GALLERY_REQUEST_CODE = 1;

    FirebaseAuth mAuth;
    FirebaseDatabase mDb;
    FirebaseStorage mStorage;
    FirebaseUser CurrentUserAuth;
    UploadTask uploadTask;

    boolean signedIn;
    boolean changedPic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        IDEt = findViewById(R.id.etRegID);
        NameEt = findViewById(R.id.etRegName);
        DateEt = findViewById(R.id.etRegDateofBirth);
        PhoneEt = findViewById(R.id.etRegPhone);
        iv = findViewById(R.id.imageView);

        tVactive = findViewById(R.id.ActiveTv);
        tVNotActive = findViewById(R.id.NotActiveTv);

        sw = findViewById(R.id.switch1);
        sw.setOnCheckedChangeListener(swListener);

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();
        CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        Intent gi = getIntent();
        UID = gi.getStringExtra("UID");
        readUser();
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
                if (currentUser != null) {
                    IDEt.setText(currentUser.getUserName());
                    NameEt.setText(currentUser.getName());
                    DateEt.setText(currentUser.getDateOfBirth());
                    PhoneEt.setText(currentUser.getPhoneNumber());
                    picUrl = currentUser.getProfilePicURL();
                    imageUri = Uri.parse(picUrl);
                    downloadImage(picUrl, getApplicationContext());

                    active = currentUser.getActive();
                    if (active == 1) {
                        sw.setChecked(false);
                        tVactive.setTextColor(Color.GREEN);
                        tVactive.setTextSize(20);
                        tVactive.setTypeface(null, Typeface.BOLD);
                    } else {
                        sw.setChecked(true);
                        tVNotActive.setTextColor(Color.RED);
                        tVNotActive.setTextSize(20);
                        tVNotActive.setTypeface(null, Typeface.BOLD);
                    }

                } else
                    Toast.makeText(getApplicationContext(), "LOG IN !!!", Toast.LENGTH_SHORT).show();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }


    /**
     * OnClickMethod for the Update Button.
     * Used to update the existing information about the user in the database, with the new
     * information submitted.
     * @param view: The Update Button.
     */
    public void update(View view) {
        userName = IDEt.getText().toString();
        name = NameEt.getText().toString();
        date = DateEt.getText().toString();
        phone = PhoneEt.getText().toString();
        if (userName.isEmpty()) userName = "lmao";
        if (sw.isChecked()) active = 0;
        else active = 1;

        if (changedPic) {
            StorageReference refStorage = mStorage.getReference("UserPics");
            StorageReference refPic = refStorage.child(UID);
            uploadTask = refPic.putFile(imageUri);
            uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    refPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            picUrl = uri.toString();
                            DatabaseReference userDB = mDb.getReference("Users").child(UID);
                            userDB.child("userName").setValue(userName);
                            userDB.child("name").setValue(name);
                            userDB.child("dateOfBirth").setValue(date);
                            userDB.child("phoneNumber").setValue(phone);
                            userDB.child("profilePicURL").setValue(picUrl);
                            userDB.child("active").setValue(active);
                        }
                    });

                }
            });
        } else {
            DatabaseReference userDB = mDb.getReference("Users").child(UID);
            userDB.child("userName").setValue(userName);
            userDB.child("name").setValue(name);
            userDB.child("dateOfBirth").setValue(date);
            userDB.child("phoneNumber").setValue(phone);
            userDB.child("active").setValue(active);
        }


    }


    /**
     * Switch View Change Listener. Used to change between Active or Not Active User.
     */
    public CompoundButton.OnCheckedChangeListener swListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (!b) {
                tVactive.setTextColor(Color.GREEN);
                tVactive.setTextSize(20);
                tVactive.setTypeface(null, Typeface.BOLD);

                tVNotActive.setTextColor(Color.BLACK);
                tVNotActive.setTextSize(15);
                tVNotActive.setTypeface(null, Typeface.NORMAL);

            } else {
                tVNotActive.setTextColor(Color.RED);
                tVNotActive.setTextSize(20);
                tVNotActive.setTypeface(null, Typeface.BOLD);

                tVactive.setTextColor(Color.BLACK);
                tVactive.setTextSize(15);
                tVactive.setTypeface(null, Typeface.NORMAL);
            }
        }
    };


    /**
     * SubMethod for the ReadUser Method.
     * Used to download the user's profile picture from the Storage database and display it
     * using an ImageView.
     * @param imageUrl: The String containing the URL for the user's profile picture in the
     *                Storage database.
     * @param context: The Activity Context.
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
     * OnClickMethod for the profile picture ImageView. Used to launch the gallery
     * @param view: The profile picture ImageView.
     */
    public void ProfilePic(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

    /**
     * OnActivityResult Method for the ProfilePic Method. Used to update the profile picture
     * ImageView with the newly selected picture.
     * @param requestCode: The GalleryRequestCode.
     * @param resultCode: The GalleryResultCode.
     * @param data: The Intent containing the image URI.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                imageUri = data.getData();
                iv.setImageURI(imageUri);
                picUrl = imageUri.toString();
                changedPic = true;
            }
        }
    }


}