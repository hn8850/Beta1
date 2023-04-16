package com.example.beta1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.2
 * @since 27/12/2022
 * This Fragment is part of the UploadAd Activity.
 * This Fragment is responsible for collection of  information regarding time and price about the
 * ParkAd (date,hour ranges and hourly rate in NIS).
 */

public class TimeFrag extends Fragment {
    EditText dayET, monthET, yearET, PriceEditText;
    Button saveButton, finishButton;
    Spinner SpinBeginHour, SpinBeginMinute, SpinEndHour, SpinEndMinute;
    String beginHour, beginMinute, endHour, endMinute;
    String Date, Price;
    String beginTime, endTime;


    Calendar calendar = Calendar.getInstance();
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    String formattedDate = dateFormat.format(calendar.getTime());

    private final String PREFS_NAME = "ParkAd" + formattedDate;
    private static final int PREFS_MODE = Context.MODE_PRIVATE;

    FirebaseDatabase mDb;
    FirebaseStorage mStorage;

    private SharedPreferences sharedPrefs;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time, container, false);

        String[] hours = new String[25];
        hours[0] = "Choose hour";
        for (int i = 1; i < 25; i++) {
            if ((i - 1) < 10) hours[i] = "0" + (i - 1);
            else hours[i] = String.valueOf(i - 1);
        }

        String[] minutes = {"Choose minutes", "00", "15", "30", "45"};

        beginMinute = "Choose minutes";
        endMinute = "Choose minutes";
        beginHour = "Choose hour";
        endHour = "Choose hour";
        Date = ";";
        Price = "0";

        finishButton = view.findViewById(R.id.finish2);
        finishButton.setOnClickListener(finishButtonClickListener);
        saveButton = view.findViewById(R.id.next2);
        saveButton.setOnClickListener(saveButtonClickListener);
        dayET = view.findViewById(R.id.day);
        monthET = view.findViewById(R.id.month);
        yearET = view.findViewById(R.id.year);
        PriceEditText = view.findViewById(R.id.price);

        SpinBeginHour = view.findViewById(R.id.hour1);
        SpinEndHour = view.findViewById(R.id.hour2);
        SpinBeginMinute = view.findViewById(R.id.minute1);
        SpinEndMinute = view.findViewById(R.id.minute2);

        SpinBeginHour.setOnItemSelectedListener(spinListener);
        SpinBeginMinute.setOnItemSelectedListener(spinListener);

        SpinEndHour.setOnItemSelectedListener(spinListener);
        SpinEndMinute.setOnItemSelectedListener(spinListener);

        ArrayAdapter<String> hourAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, hours);
        SpinBeginHour.setAdapter(hourAdapter);
        SpinEndHour.setAdapter(hourAdapter);

        ArrayAdapter<String> minuteAdapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), android.R.layout.simple_spinner_item, minutes);
        SpinBeginMinute.setAdapter(minuteAdapter);
        SpinEndMinute.setAdapter(minuteAdapter);

        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener2);

        if (sharedPrefs.contains(getString(R.string.prefs_FINISH_key))) {
            finishButton.setVisibility(View.VISIBLE);
            finishButton.setClickable(true);
        }

        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();

        return view;
    }

    private View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        /**
         * OnClickMethod for the save Button.
         * This method saves the information submitted by the user to a SharedPrefs file.
         * @param view: The save Button.
         */
        @Override
        public void onClick(View view) {
            try {
                Date = dayET.getText().toString() + '/' + monthET.getText().toString() + '/' + yearET.getText().toString();
            } catch (Exception e) {
                ErrorAlert("Fill out all of the fields!");
                return;
            }

            try {
            } catch (Exception e) {
                ErrorAlert("Fill out all of the fields!");
                return;
            }

            Price = PriceEditText.getText().toString();
            beginTime = beginHour + ":" + beginMinute;
            endTime = endHour + ":" + endMinute;
            if (ValidInfo()) {
                sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

                // Get a reference to the Shared Preferences editor
                SharedPreferences.Editor editor = sharedPrefs.edit();

                // Write the data to the Shared Preferences file
                editor.putString(getString(R.string.prefs_date_key), Date);
                editor.putString(getString(R.string.prefs_beginhour_key), beginTime);
                editor.putString(getString(R.string.prefs_finishhour_key), endTime);
                editor.putString(getString(R.string.prefs_hourlyrate_key), Price);

                // Save the changes to the Shared Preferences file
                editor.apply();
                Toast.makeText(getActivity().getApplicationContext(), "TIME + PRICE SAVED!", Toast.LENGTH_SHORT).show();
            }
        }
    };


    private View.OnClickListener finishButtonClickListener = new View.OnClickListener() {
        /**
         * OnClickMethod for the finish Button.
         * Only clickable and visible after all the necessary params for a ParkAd have been filled
         * throughout all of the Fragments.
         * Uploads all of the images selected to the Storage database and calls the UploadAd Method.
         * @param view: The finish Button.
         */
        @Override
        public void onClick(View view) {
            sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
            String latitude = sharedPrefs.getString("latitude", "0");
            String longitude = sharedPrefs.getString("longitude", "0");

            List<String> imageUris = new ArrayList<>();
            if (sharedPrefs.contains(getString(R.string.prefs_URI1_key))) {
                imageUris.add(sharedPrefs.getString("URI1", null));
                imageUris.add(sharedPrefs.getString("URI2", null));
                imageUris.add(sharedPrefs.getString("URI3", null));
                imageUris.add(sharedPrefs.getString("URI4", null));
                imageUris.add(sharedPrefs.getString("URI5", null));
            }

            String path = (latitude + longitude).replace(".", "");
            StorageReference refStorage = mStorage.getReference("ParkAdPics");
            StorageReference refPath = refStorage.child(path);
            ReCreateFolder(path);

            final AtomicInteger count = new AtomicInteger();
            ArrayList<String> imageURLS = new ArrayList<>();
            for (int i = 0; i < imageUris.size(); i++) {
                if ((imageUris.get(i)).matches("NONE")) {
                    imageUris.remove(i);
                    i--;
                }
            }
            ArrayList<UploadTask> tasks = new ArrayList<>();
            for (int i = 0; i < imageUris.size(); i++) {
                Uri imageUri = Uri.parse(imageUris.get(i));
                if (!((imageUri.toString()).matches("NONE"))) {
                    StorageReference imageRef = refPath.child("Image" + i);
                    imageRef.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // URL of the uploaded image was successfully retrieved
                                    String imageUrl = uri.toString();
                                    imageURLS.add(imageUrl);
                                    int c = count.incrementAndGet();
                                    // check if all images have been uploaded
                                    if (c == imageUris.size()) {
                                        // all images have been uploaded
                                        // execute the callback function
                                        uploadAd(imageURLS);
                                    }
                                }
                            });
                        }
                    });

                }
            }
        }
    };

    /**
     * Creates a ParkAd Object with all of the information from the SharedPrefs file, and then
     * uploads the ParkAd to the database.
     *
     * @param imageURLS: ArrayList containing the String URL's for each image that was uploaded.
     */
    public void uploadAd(ArrayList<String> imageURLS) {
        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

        String latitude = sharedPrefs.getString("latitude", "0");
        String longitude = sharedPrefs.getString("longitude", "0");

        String path = (latitude + longitude).replace(".", "");

        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUid = newUser.getUid();
        int Active = 1;
        String Date = sharedPrefs.getString("Date", "0");
        String dateParam = Services.addLeadingZerosToDate(Date, true);
        String BeginHour = sharedPrefs.getString("BeginHour", "0");
        String FinishHour = sharedPrefs.getString("FinishHour", "0");
        Double HourlyRate = Double.valueOf(sharedPrefs.getString("HourlyRate", "0"));
        String Description = sharedPrefs.getString("Description", "No desc");
        String Address = sharedPrefs.getString("address", "0");


        String beginHourKey = "B" + BeginHour.substring(0, 2) + BeginHour.substring(3);
        String endHourKey = "E" + FinishHour.substring(0, 2) + FinishHour.substring(3);
        String hourRangeKey = beginHourKey + endHourKey;
        String dateKey = "D" + Services.addLeadingZerosToDate(Date, false);
        String parkAdKey = path + dateKey + hourRangeKey;

        ParkAd ad = new ParkAd(latitude, longitude, userUid, Active, dateParam, BeginHour, FinishHour, HourlyRate, imageURLS, Description, Address);
        DatabaseReference adRef = mDb.getReference("ParkAds");
        adRef.child(parkAdKey).setValue(ad);
        DatabaseReference userAdRef = mDb.getReference("Users").child(userUid).child("ParkAds").child(parkAdKey);
        userAdRef.setValue(ad);
        Toast.makeText(getActivity().getApplicationContext(), "AD UPLOADED!", Toast.LENGTH_SHORT).show();
        sharedPrefs.edit().clear().apply();

    }

    SharedPreferences.OnSharedPreferenceChangeListener prefsListener2 = new SharedPreferences.OnSharedPreferenceChangeListener() {
        /**
         * SharedPrefsChange Listener. Used to verify that all of the required params for a ParkAd have
         * been filled out and saved by the user.
         * If so, the finish Button becomes visible and clickable.
         * @param sharedPreferences: The SharedPrefs file.
         * @param key:
         */
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if (sharedPreferences.contains("address") && sharedPreferences.contains("latitude") && sharedPreferences.contains("longitude")) {
                if (sharedPreferences.contains("Date") && sharedPreferences.contains("BeginHour") && sharedPreferences.contains("FinishHour") && sharedPreferences.contains("HourlyRate")) {
                    if (sharedPreferences.contains("Description") && sharedPreferences.contains("URI1")) {
                        finishButton.setVisibility(View.VISIBLE);
                        finishButton.setClickable(true);
                        SharedPreferences.Editor editor = sharedPrefs.edit();
                        editor.putString(getString(R.string.prefs_FINISH_key), "gogogo");
                        editor.apply();

                    } else {
                        finishButton.setVisibility(View.INVISIBLE);
                        finishButton.setClickable(false);
                    }
                } else {
                    finishButton.setVisibility(View.INVISIBLE);
                    finishButton.setClickable(false);
                }

            } else {
                finishButton.setVisibility(View.INVISIBLE);
                finishButton.setClickable(false);
            }

        }
    };

    /**
     * Used to wipe an existing images folder in the Storage database for a ParkAd in order to
     * upload new images.
     *
     * @param path: String containing the locationKey for the ParkAd.
     */
    public void ReCreateFolder(String path) {
        StorageReference refStorage = mStorage.getReference("ParkAdPics");
        StorageReference refPath = refStorage.child(path);

// Call listAll() to get a list of all items in the folder
        refPath.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                // Check if the folder exists by checking if it is present in the list
                if (listResult.getItems().size() > 0) {
                    // The folder exists, so you can do something here
                    // For example, you can delete its contents by calling delete() on each of the items in the list
                    for (StorageReference item : listResult.getItems()) {
                        item.delete();
                    }
                } else {
                    return;
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                // Handle errors
            }
        });

    }

    /**
     * ItemSelectedListener for the hour/minute Spinners.
     * Used to update the time String params for the ParkAd.
     */
    AdapterView.OnItemSelectedListener spinListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            if (adapterView == SpinBeginHour) {
                beginHour = adapterView.getItemAtPosition(i).toString();
            } else if (adapterView == SpinEndHour) {
                endHour = adapterView.getItemAtPosition(i).toString();
            } else if (adapterView == SpinBeginMinute) {
                beginMinute = adapterView.getItemAtPosition(i).toString();
            } else {
                endMinute = adapterView.getItemAtPosition(i).toString();
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };


    /**
     * SubMethod for the save Button OnClickMethod.
     * Used to validate the information the user has submitted (possible dates and time ranges etc.)
     *
     * @return: The Method returns true if the following conditions are met:
     * 1. All 4 Spinners are not on their default state.
     * 2. The date submitted is actually a date.
     * 3. The date submitted is not a relic of the past.
     * 4. The beginHour is not after/equal to the endHour
     * 5. in case of the current date matching the submitted one, the time range described by the
     * begin and end hour Strings has not already passed.
     * 6. The hourly rate is not a positive number.
     */
    public boolean ValidInfo() {
        if (beginHour.matches("Choose hour") || endHour.matches("Choose hour")) {
            ErrorAlert("Choose an actual hour!");
            return false;
        }
        if (beginMinute.matches("Choose minutes") || endMinute.matches("Choose minutes")) {
            ErrorAlert("Choose an actual hour!");

            return false;
        }

        String pattern = "^(3[01]|[12][0-9]|0?[1-9])/(1[0-2]|0?[1-9])/[0-9]{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(Date);
        if (!m.find()) {
            ErrorAlert("Choose an actual date!");

            return false;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
        String currentDate = dateFormat.format(new Date());
        currentDate = Services.addLeadingZerosToDate(currentDate, false);
        String DateTemp = Services.addLeadingZerosToDate(Date, false);
        if (Integer.valueOf(DateTemp) - Integer.valueOf(currentDate) < 0) {
            ErrorAlert("That date has passed!");
            return false;
        }


        if (SpinEndHour.getSelectedItemPosition() < SpinBeginHour.getSelectedItemPosition()) {
            ErrorAlert("BeginHour cant be after EndHour");
            return false;
        }
        if (SpinEndHour.getSelectedItemPosition() == SpinBeginHour.getSelectedItemPosition()) {
            if (SpinEndMinute.getSelectedItemPosition() <= SpinBeginMinute.getSelectedItemPosition()) {
                ErrorAlert("BeginHour cant be after EndHour");
                return false;
            }
        }

        if (Integer.valueOf(DateTemp) - Integer.valueOf(currentDate) == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (!Services.isTimeRangeAfterCurrentHour(beginTime, endTime)) {
                    ErrorAlert("That TimeRange has passed");
                    return false;
                }
            }
        }

        if (Price.isEmpty() || Double.valueOf(Price) <= 0) {
            ErrorAlert("Price per hour cant be zero!");
            return false;
        }
        return true;
    }

    /**
     * SubMethod for the information verification proccess.
     * Used to handle user errors regarding the information that was submitted, by creating
     * AlertDialog boxes.
     *
     * @param message: The message containing what the user did wrong when submitting information.
     */
    public void ErrorAlert(String message) {
        AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
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


}