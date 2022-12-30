package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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

public class Register extends AppCompatActivity {

    TextInputEditText etRegEmail;
    TextInputEditText etRegPassword;
    TextInputEditText IDEt, NameEt, DateEt, PhoneEt;
    String userName, name, date, phone, picUrl;
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
        IDEt = findViewById(R.id.etRegID);
        NameEt = findViewById(R.id.etRegName);
        DateEt = findViewById(R.id.etRegDateofBirth);
        PhoneEt = findViewById(R.id.etRegPhone);
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
            startActivity(new Intent(Register.this, Login.class));
        });
    }

    public void createUser() {
        String email = etRegEmail.getText().toString().trim();
        String password = etRegPassword.getText().toString().trim();
        userName = IDEt.getText().toString();
        name = NameEt.getText().toString();
        date = DateEt.getText().toString();
        phone = PhoneEt.getText().toString();
        if (userName.isEmpty()) userName = "lmao";

        if (TextUtils.isEmpty(email)) {
            etRegEmail.setError("Email cannot be empty");
            etRegEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            etRegPassword.setError("Password cannot be empty");
            etRegPassword.requestFocus();
        } else {
            mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
                        UID = newUser.getUid();
                        StorageReference refStorage = mStorage.getReference("UserPics");
                        StorageReference refPic = refStorage.child(UID);
                        uploadTask = refPic.putFile(imageUri);
                        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                                System.out.println("Upload is " + progress + "% done");
                            }
                        }).addOnPausedListener(new OnPausedListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onPaused(UploadTask.TaskSnapshot taskSnapshot) {
                                System.out.println("Upload is paused");
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                System.out.println("Upload failed");
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                System.out.println("Upload successful");
                                refPic.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        picUrl = uri.toString();
                                        User userDB = new User(userName, 1, name, date, phone, picUrl);
                                        DatabaseReference refDb = mDb.getReference("Users");
                                        refDb.child(UID).setValue(userDB);
                                    }
                                });

                            }
                        });

                        Toast.makeText(Register.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(Register.this, Login.class));
                    } else {
                        Toast.makeText(Register.this, "Registration Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    public void ProfilePic(View view) {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, GALLERY_REQUEST_CODE);
    }

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