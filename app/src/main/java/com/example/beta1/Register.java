package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnPausedListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.2
 * @since 23/12/2022
 * In this Activity, new users of the app can register themselves!
 */

public class Register extends AppCompatActivity {

    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextInputEditText NameEt, PhoneEt;
    EditText dd, mm, yyyy;
    String email, password;
    String name, date, phone, picUrl;
    String UID;
    ImageView iv;
    Uri imageUri;

    final static int GALLERY_REQUEST_CODE = 1;

    TextView tvLoginHere;
    Button btnRegister;

    FirebaseAuth mAuth;
    FirebaseDatabase mDb;
    FirebaseStorage mStorage;
    UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPass);
        NameEt = findViewById(R.id.etRegName);
        PhoneEt = findViewById(R.id.etRegPhone);
        dd = findViewById(R.id.edit_text_dd_1);
        mm = findViewById(R.id.edit_text_mm_1);
        yyyy = findViewById(R.id.edit_text_yyyy_1);
        tvLoginHere = findViewById(R.id.tvLoginHere);
        btnRegister = findViewById(R.id.btnRegister);
        iv = findViewById(R.id.imageView);
        int drawableId = R.drawable.userpic;
        imageUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getResources().getResourcePackageName(drawableId)
                + '/' + getResources().getResourceTypeName(drawableId)
                + '/' + getResources().getResourceEntryName(drawableId));
        picUrl = imageUri.toString();

        mAuth = FirebaseAuth.getInstance();
        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        btnRegister.setOnClickListener(view -> {
            createUser();
        });

        tvLoginHere.setOnClickListener(view -> {
            Intent si = new Intent(this, Login.class);
            si.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(si);
        });
    }

    /**
     * Creates a user in the database using FireBase Auth and the user submitted information.
     */
    public void createUser() {
        email = etRegEmail.getText().toString().trim();
        password = etRegPassword.getText().toString().trim();
        name = NameEt.getText().toString();
        date = dd.getText().toString() + "/" + mm.getText().toString() + "/" + yyyy.getText().toString();
        phone = "0" + PhoneEt.getText().toString();

        if (validInfo()) {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    System.out.println("CRASH");
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
                        newUser.sendEmailVerification();
                        UID = newUser.getUid();
                        StorageReference refStorage = mStorage.getReference("UserPics");
                        StorageReference refPic = refStorage.child(UID);
                        uploadTask = refPic.putFile(imageUri);
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                            }
                        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                System.out.println("FAIL");
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                refPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        System.out.println("WIN");
                                        picUrl = uri.toString();
                                        User userDB = new User(1, name, date, phone, picUrl);
                                        DatabaseReference refDb = mDb.getReference("Users");
                                        refDb.child(UID).setValue(userDB);
                                    }
                                });
                            }
                        });

                        Toast.makeText(Register.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        Intent si = new Intent(Register.this, Login.class);
                        si.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(si);
                    } else {
                        ErrorAlert("Registration Error: " + task.getException().getMessage());
                    }
                }
            });
        }
    }

    /**
     * Boolean SubMethod for the CreateUser method.
     * The Method validates all of the user's submitted fields.
     *
     * @return: The Method returns true if all of the information is valid, false otherwise.
     */
    private boolean validInfo() {
        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();
            return false;
        }
        if (TextUtils.isEmpty(password)) {
            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();
            return false;
        }
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
        if (TextUtils.isEmpty(date)) {
            dd.setError("Date cannot be empty");
            dd.requestFocus();
            return false;
        }
        if (!Services.isValidPhoneNumber(phone)) {
            ErrorAlert("Please enter valid phone number");
            return false;
        }
        if (!Services.isValidDate2(date)) {
            ErrorAlert("Enter a valid date!");
            return false;
        }
        if (!isAge16AndAbove(date)) {
            ErrorAlert("User must be above the age of 16!");
            return false;
        }

        System.out.println("TRUE");
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
     * Boolean SubMethod for the ValidInfo Method.
     * Used to verify if the user submitted date of birth String describes an age above 16 (the
     * necessary minimum age to use the app).
     *
     * @param birthDateString: The user submitted birthDate String.
     * @return: The Method returns true if the user is above 16 years of age,false otherwise.
     */
    public static boolean isAge16AndAbove(String birthDateString) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        Date birthDate;
        try {
            // Parse the birth date string into a Date object
            birthDate = sdf.parse(birthDateString);
        } catch (ParseException e) {
            // Invalid date format
            e.printStackTrace();
            return false;
        }

        // Create a calendar instance for the current date
        Calendar currentDate = Calendar.getInstance();

        // Create a calendar instance for the birth date
        Calendar birthDateCalendar = Calendar.getInstance();
        birthDateCalendar.setTime(birthDate);

        // Calculate the age based on year difference
        int age = currentDate.get(Calendar.YEAR) - birthDateCalendar.get(Calendar.YEAR);

        // If birth date month is greater than current month,
        // or birth date month is same as current month but day is greater,
        // then decrement age by 1
        if (birthDateCalendar.get(Calendar.MONTH) > currentDate.get(Calendar.MONTH)
                || (birthDateCalendar.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH)
                && birthDateCalendar.get(Calendar.DAY_OF_MONTH) > currentDate.get(Calendar.DAY_OF_MONTH))) {
            age--;
        }

        // Check if age is 16 or above
        return age >= 16;
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
            }
        }
    }

}