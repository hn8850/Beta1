package com.example.beta1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
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
    TextInputEditText etRegPassword,etSecondPass;
    TextInputEditText NameEt, PhoneEt;
    EditText dd, mm, yyyy;
    String email, password,password2;
    String name, date, phone, picUrl;
    String UID;
    ImageView iv;
    Uri imageUri;

    File photoFile;
    final static int GALLERY_REQUEST_CODE = 1;
    private static final int REQUEST_IMAGE_CAPTURE = 2;
    private static final int RESULT_OK = -1;

    TextView tvLoginHere;
    Button btnRegister;

    FirebaseAuth mAuth;
    FirebaseDatabase mDb;
    FirebaseStorage mStorage;
    UploadTask uploadTask;

    ProgressDialog progressDialog;

    private static final int PREFS_MODE = Context.MODE_PRIVATE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etRegEmail = findViewById(R.id.etRegEmail);
        etRegPassword = findViewById(R.id.etRegPass);
        etSecondPass = findViewById(R.id.etRegPass2);
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
        password2 = etSecondPass.getText().toString().trim();
        name = NameEt.getText().toString();
        date = dd.getText().toString() + "/" + mm.getText().toString() + "/" + yyyy.getText().toString();
        phone = "0" + PhoneEt.getText().toString();

        if (validInfo()) {
            progressDialog = new ProgressDialog(Register.this);
            progressDialog.setMessage("Registering...");
            progressDialog.setCancelable(false);
            progressDialog.show();
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
                        newUser.sendEmailVerification();
                        UID = newUser.getUid();
                        StorageReference refStorage = mStorage.getReference("UserPics");
                        StorageReference refPic = refStorage.child(UID);
                        uploadTask = refPic.putFile(imageUri);
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                progressDialog.dismiss();
                                Services.ErrorAlert(exception.getMessage(),Register.this);
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                refPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        progressDialog.dismiss();
                                        picUrl = uri.toString();
                                        date = Services.addLeadingZerosToDate(date,true);
                                        User userDB = new User(name, date, phone, picUrl);
                                        DatabaseReference refDb = mDb.getReference("Users");
                                        refDb.child(UID).setValue(userDB);
                                        AlertDialog.Builder adb = new AlertDialog.Builder(Register.this);
                                        adb.setTitle("User created successfully");
                                        adb.setMessage("A Verification Email has been sent to you!");
                                        adb.setNeutralButton("Return", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialogInterface, int i) {
                                                Intent si = new Intent(Register.this, Login.class);
                                                si.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                                SharedPreferences sharedPreferences = getSharedPreferences("rember",PREFS_MODE);
                                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                                editor.putString("remember","0");
                                                editor.apply();
                                                mAuth.signOut();
                                                startActivity(si);
                                                dialogInterface.dismiss();
                                            }
                                        });
                                        AlertDialog dialog = adb.create();
                                        dialog.show();
                                    }
                                });
                            }
                        });

                    } else {
                        progressDialog.dismiss();
                        Services.ErrorAlert("Registration Error: " + task.getException().getMessage(),Register.this);
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
        if (TextUtils.isEmpty(password2)) {
            etSecondPass.setError("You must renter password");
            etSecondPass.requestFocus();
            return false;
        }

        if (!password2.matches(password)){
            Services.ErrorAlert("Passwords do not match!",Register.this);
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
            Services.ErrorAlert("Please enter valid phone number",Register.this);
            return false;
        }
        if (!Services.isValidDate2(date)) {
            Services.ErrorAlert("Please enter a valid date",Register.this);
            return false;
        }

        if (Services.isDateNotReached(date)){
            Services.ErrorAlert("You cant be born in the future!",Register.this);
            return false;
        }

        if (!isAge16AndAbove(date)) {
            Services.ErrorAlert("User must be above the age of 16!",Register.this);
            return false;
        }

        return true;
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
     * OnClickMethod for the profile picture ImageView. Used to launch the gallery/camera.
     *
     * @param view: The profile picture ImageView.
     */
    public void openGalleryOrCamera(View view) {
        CharSequence options[] = new CharSequence[]{"Take Photo", "Choose from Gallery"};
        AlertDialog.Builder builder = new AlertDialog.Builder(Register.this);
        builder.setTitle("Select Option");
        builder.setItems(options, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    photoFile = null;
                    try {
                        photoFile = createImageFile();
                    } catch (IOException ex) {
                        // Error occurred while creating the File
                    }
                    if (photoFile != null) {
                        imageUri = FileProvider.getUriForFile(Register.this, "com.mydomain.fileprovider", photoFile);
                        takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                    }


                } else if (which == 1) {
                    Intent pickGalleryIntent = new Intent(Intent.ACTION_PICK);
                    pickGalleryIntent.setType("image/*");
                    startActivityForResult(pickGalleryIntent, GALLERY_REQUEST_CODE);

                }
            }
        });
        builder.show();
    }

    /**
     * OnActivityResult Method for the ProfilePic Method. Used to update the profile picture
     * ImageView with the newly selected picture.
     *
     * @param requestCode: The GalleryRequestCode/CameraRequestCode.
     * @param resultCode:  The ResultCode.
     * @param data:        The Intent containing the image URI.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                imageUri = data.getData();
                iv.setImageURI(imageUri);
                picUrl = imageUri.toString();
            } else if (requestCode == REQUEST_IMAGE_CAPTURE) {
                imageUri = Uri.fromFile(photoFile);
                iv.setImageURI(imageUri);
                picUrl = imageUri.toString();
            }
            Bitmap bitmap2 = ((BitmapDrawable) iv.getDrawable()).getBitmap();
            Bitmap circularBitmap = getCircularBitmap(bitmap2);
            iv.setImageBitmap(circularBitmap);
        }

    }

    /**
     * SubMethod for the profile pic ImageView OnClickMethod.
     * Used to create a File from any image selected (which will then be converted to a URI).
     *
     * @throws IOException
     * @return: Image File.
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getApplicationContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
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

}