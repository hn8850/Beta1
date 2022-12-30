package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Login extends AppCompatActivity {

    TextInputEditText etLoginEmail;
    TextInputEditText etLoginPassword;
    TextView tvRegisterHere;
    Button btnLogin;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPass);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);
        btnLogin = findViewById(R.id.btnLogin);


        mAuth = FirebaseAuth.getInstance();
        //mAuth.signOut();

        btnLogin.setOnClickListener(view -> {
            loginUser();
        });
        tvRegisterHere.setOnClickListener(view -> {
            startActivity(new Intent(Login.this, Register.class));
        });
    }

    public void loginUser() {
        String email = etLoginEmail.getText().toString().trim();
        String password = etLoginPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etLoginEmail.setError("Email cannot be empty");
            etLoginEmail.requestFocus();
        } else if (TextUtils.isEmpty(password)) {
            etLoginPassword.setError("Password cannot be empty");
            etLoginPassword.requestFocus();
        } else {
            mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if (task.isSuccessful()) {
                        Toast.makeText(Login.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                        //startActivity(new Intent(Login.this, activity_2.class));
                    } else {
                        Toast.makeText(Login.this, "Log in Error: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }


    public void resetPass(View view) {
        String email = etLoginEmail.getText().toString();
        if (!(isValidEmail(email))) {
            Toast.makeText(Login.this, "Not a Valid Email!", Toast.LENGTH_SHORT).show();
        } else {
            AlertDialog.Builder adb = new AlertDialog.Builder(this);
            adb.setTitle("Send Email?");
            adb.setMessage("Press Confirm to send password reset email to " + email);
            adb.setPositiveButton("Confirm", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(etLoginEmail.getText().toString())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(Login.this, "Check your email for password reset instructions!", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(Login.this, "Email not registered", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                }
            });
            adb.setNegativeButton("Return", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });
            AlertDialog ad = adb.create();
            ad.show();
        }
    }

    public static boolean isValidEmail(String email) {
        String check = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "("
                + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+";
        Pattern pattern = Pattern.compile(check);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        String st = item.getTitle().toString();

        if (st.equals("Login")) {
            Toast.makeText(this, "You're in this Activity!!", Toast.LENGTH_SHORT).show();
        }

        if (st.equals("Navi")) {
            Intent si = new Intent(this, Navi.class);
            startActivity(si);
        }
        if (st.equals("test")) {
            Intent si = new Intent(this, test.class);
            startActivity(si);
        }

        if (st.equals("Post Ad")) {
            Intent si = new Intent(this, UploadAd.class);
            startActivity(si);
        }

        if (st.equals("Edit Profile")) {
            Intent si = new Intent(this, EditProfile.class);
            startActivity(si);
        }
/*
        if (st.equals("Notifications")){
            Intent si = new Intent(this, notifs.class);
            startActivity(si);
        }

        if (st.equals("Google Pay")) {
            Intent si = new Intent(this, GooglePay.class);
            startActivity(si);
        }

        if (st.equals("Maps")) {
            Intent si = new Intent(this, mapa.class);
            startActivity(si);
        }
         */
        return true;
    }


}