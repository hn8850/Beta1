package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.2
 * @since 23/12/2022
 * This Activity is the first Activity a new user sees. Here the user can login and begin using the
 * app!
 */
public class Login extends AppCompatActivity {

    CheckBox rember;
    TextInputEditText etLoginEmail;
    TextInputEditText etLoginPassword;
    TextView tvRegisterHere;
    Button btnLogin;

    private static final int PREFS_MODE = Context.MODE_PRIVATE;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        etLoginEmail = findViewById(R.id.etLoginEmail);
        etLoginPassword = findViewById(R.id.etLoginPass);
        tvRegisterHere = findViewById(R.id.tvRegisterHere);
        btnLogin = findViewById(R.id.btnLogin);
        rember = findViewById(R.id.rember);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isUsingNetworkTime()) {
            showTimeWarningDialog();
            saveStringToSharedPref("remember", "0");
        } else {
            mAuth = FirebaseAuth.getInstance();
            if (mAuth.getCurrentUser() != null) {
                SharedPreferences sharedPreferences = getSharedPreferences("rember", PREFS_MODE);
                if (sharedPreferences.getString("remember", "-1").matches("1")) {
                    Intent si = new Intent(Login.this, Navi.class);
                    startActivity(si);
                }
            }

            btnLogin.setOnClickListener(view -> {
                loginUser();
            });
            tvRegisterHere.setOnClickListener(view -> {
                startActivity(new Intent(Login.this, Register.class));
            });
        }
    }

    /**
     * OnClickMethod for the login Button. Checks if the user credentials exist in the database and
     * logs in accordingly.
     */
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
                        FirebaseUser currUser = mAuth.getCurrentUser();
                        if (currUser.isEmailVerified()) {
                            Toast.makeText(Login.this, "User logged in successfully", Toast.LENGTH_SHORT).show();
                            if (rember.isChecked()) {
                                saveStringToSharedPref("remember", "1");
                            } else {
                                saveStringToSharedPref("remember", "0");
                            }
                            Intent si = new Intent(getApplicationContext(), Navi.class);
                            startActivity(si);

                        } else {
                            AlertDialog.Builder adb = new AlertDialog.Builder(Login.this);
                            adb.setTitle("Can't login!");
                            adb.setMessage("Please verify your user! Check your email for verification email");
                            adb.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    dialogInterface.dismiss();
                                }
                            });
                            adb.create().show();
                        }


                    } else {
                        AlertDialog.Builder adb = new AlertDialog.Builder(Login.this);
                        adb.setTitle("Can't login!");
                        adb.setMessage("Log in Error:" + task.getException().getMessage());
                        adb.setNeutralButton("Close", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                        adb.create().show();
                    }
                }
            });
        }
    }


    /**
     * OnClickMethod for the 'forgot your password?' TextView.
     * Sends a password reset email to the email the user has submitted.
     *
     * @param view: The 'forgot your password?' TextView.
     */
    public void resetPass(View view) {
        String email = etLoginEmail.getText().toString();
        if (!(isValidEmail(email))) {
            ErrorAlert("An error occurred!", "Please enter a Valid Email!");
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
                                        ErrorAlert("Password reset email sent.", "Check your email for password reset instructions!  \n  Please note that you cant login until setting a new password!");
                                    } else {
                                        ErrorAlert("An error occurred!", "Email not registered!");
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

    /**
     * Boolean SubMethod for the LoginUser Method's data verification process.
     *
     * @param email: The email the user has inputted (String).
     * @return: The Method will return true if the email given is a valid email. false otherwise.
     */
    public static boolean isValidEmail(String email) {
        String check = "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" + "\\@" + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" + "("
                + "\\." + "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" + ")+";
        Pattern pattern = Pattern.compile(check);
        Matcher matcher = pattern.matcher(email);

        return matcher.matches();
    }


    /**
     * SubMethod for the information verification process.
     * Used to notify the user about their login status, by creating
     * AlertDialog boxes.
     *
     * @param message: The message containing what the user did wrong when submitting information.
     */
    public void ErrorAlert(String title, String message) {
        android.app.AlertDialog.Builder adb = new android.app.AlertDialog.Builder(Login.this);
        adb.setTitle(title);
        adb.setMessage(message);
        adb.setNeutralButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        android.app.AlertDialog dialog = adb.create();
        dialog.show();
    }


    /**
     * SubMethod for Remember Me CheckBox.
     * Used to save information about an Order Object for the
     * activeOrdersDataList.
     *
     * @param key:   The key of the information to be saved.
     * @param value: The value of the information to be saved.
     */
    public void saveStringToSharedPref(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("rember", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }


    /**
     * Boolean Method used to verify if the user is manipulating their time settings.
     * Must return true to access the app's services.
     *
     * @return: The Method returns true if the user has activated "Use Network Provided Time" in
     * their system settings. false otherwise.
     */
    private boolean isUsingNetworkTime() {
        return Settings.Global.getInt(getContentResolver(), Settings.Global.AUTO_TIME, 0) == 1;
    }

    /**
     * SubMethod used to display the user with a warning message in case the isUsingNetworkTime Method
     * returns false.
     */
    private void showTimeWarningDialog() {
        new android.app.AlertDialog.Builder(this)
                .setTitle("Time Warning")
                .setMessage("Your device is not using network-provided time. Please enable network time in your device settings to ensure accurate app functionality.")
                .setPositiveButton("Open Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_DATE_SETTINGS));
                    }
                })
                .setNegativeButton("Close App", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finishAffinity();
                    }
                })
                .show();
    }


}