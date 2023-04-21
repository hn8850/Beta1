package com.example.beta1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
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

    TextInputEditText NameEt, PhoneEt;

    String name, phone, picUrl;
    String UID;
    ImageView iv;
    Uri imageUri;

    final static int GALLERY_REQUEST_CODE = 1;

    FirebaseAuth mAuth;
    FirebaseDatabase mDb;
    FirebaseStorage mStorage;
    FirebaseUser CurrentUserAuth;
    UploadTask uploadTask;

    boolean changedPic = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        NameEt = findViewById(R.id.etRegName);
        PhoneEt = findViewById(R.id.etRegPhone);
        iv = findViewById(R.id.imageView);


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
                    NameEt.setText(currentUser.getName());
                    PhoneEt.setText(currentUser.getPhoneNumber().substring(1));
                    picUrl = currentUser.getProfilePicURL();
                    imageUri = Uri.parse(picUrl);
                    downloadImage(picUrl, getApplicationContext());

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
     *
     * @param view: The Update Button.
     */
    public void update(View view) {
        name = NameEt.getText().toString();
        phone = "0" + PhoneEt.getText().toString();

        if (validInfo()) {
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
                                userDB.child("name").setValue(name);
                                userDB.child("phoneNumber").setValue(phone);
                                userDB.child("profilePicURL").setValue(picUrl);
                            }
                        });

                    }
                });
            } else {
                DatabaseReference userDB = mDb.getReference("Users").child(UID);
                userDB.child("name").setValue(name);
                userDB.child("phoneNumber").setValue(phone);
            }
        }

    }

    /**
     * Boolean SubMethod for the update method.
     * The Method validates all of the user's submitted fields.
     *
     * @return: The Method returns true if all of the information is valid, false otherwise.
     */
    private boolean validInfo() {

        if (TextUtils.isEmpty(name)) {
            NameEt.setError("Name cannot be empty");
            NameEt.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(phone)) {
            PhoneEt.setError("Phone number cannot be empty");
            PhoneEt.requestFocus();
            return false;
        }

        if (!Services.isValidPhoneNumber(phone)) {
            ErrorAlert("Please enter valid phone number");
            return false;
        }

        return true;
    }

    /**
     * SubMethod for the information verification process.
     * Used to handle user errors regarding the information that was submitted, by creating
     * AlertDialog boxes.
     *
     * @param message: The message containing what the user did wrong when submitting information.
     */
    public void ErrorAlert(String message) {
        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("An error occurred when saving your info!");
        adb.setMessage(message);
        adb.setNeutralButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = adb.create();
        dialog.show();
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
                iv.setImageBitmap(bitmap);
                Bitmap bitmap2 = ((BitmapDrawable) iv.getDrawable()).getBitmap();
                Bitmap circularBitmap = getCircularBitmap(bitmap2);
                iv.setImageBitmap(circularBitmap);
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
     * OnClickMethod for the profile picture ImageView. Used to launch the gallery
     *
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
     *
     * @param requestCode: The GalleryRequestCode.
     * @param resultCode:  The GalleryResultCode.
     * @param data:        The Intent containing the image URI.
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