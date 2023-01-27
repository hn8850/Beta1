package com.example.beta1;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
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
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TimeFrag extends Fragment {
    EditText DateEditText, PriceEditText;
    Button saveButton, finishButton;
    Spinner SpinBeginHour, SpinBeginMinute, SpinEndHour, SpinEndMinute;
    String beginHour, beginMinute, endHour, endMinute;
    String Date, Price;

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
            hours[i] = String.valueOf(i);
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
        DateEditText = view.findViewById(R.id.date);
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

    private View.OnClickListener finishButtonClickListener = new View.OnClickListener() {
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
                System.out.println("TEST = " + i + " --> " + imageUris.get(i));
                if ((imageUris.get(i)).matches("NONE")) {
                    imageUris.remove(i);
                    i--;
                }
            }
            System.out.println("SIZE = " + imageUris.size());
            ArrayList<UploadTask> tasks = new ArrayList<>();
            for (int i = 0; i < imageUris.size(); i++) {
                Uri imageUri = Uri.parse(imageUris.get(i));
                System.out.println("URI + = " + imageUri.toString());
                if (!((imageUri.toString()).matches("NONE"))) {
                    StorageReference imageRef = refPath.child("Image" + i);
                    imageRef.putFile(imageUri).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            System.out.println("ERROR = " + exception);
                        }
                    }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("WHATSTSTS");
                            imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    // URL of the uploaded image was successfully retrieved
                                    String imageUrl = uri.toString();
                                    imageURLS.add(imageUrl);
                                    System.out.println("image url = " + imageUrl);

                                    int c = count.incrementAndGet();
                                    System.out.println("C = " + c);
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

    public void uploadAd(ArrayList<String> imageURLS) {

        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

        String latitude = sharedPrefs.getString("latitude", "0");
        String longitude = sharedPrefs.getString("longitude", "0");

        String path = (latitude + longitude).replace(".", "");

        FirebaseUser newUser = FirebaseAuth.getInstance().getCurrentUser();
        String userUid = newUser.getUid();
        int Active = 1;
        String Date = sharedPrefs.getString("Date", "0");
        String BeginHour = sharedPrefs.getString("BeginHour", "0");
        String FinishHour = sharedPrefs.getString("FinishHour", "0");
        Double HourlyRate = Double.valueOf(sharedPrefs.getString("HourlyRate", "0"));
        String Description = sharedPrefs.getString("Description", "No desc");
        String Address = sharedPrefs.getString("address", "0");

        ParkAd ad = new ParkAd(latitude, longitude, userUid, Active, Date, BeginHour, FinishHour, HourlyRate, imageURLS, Description, Address);
        DatabaseReference adRef = mDb.getReference("ParkAds");
        adRef.child(path).setValue(ad);
        Toast.makeText(getActivity().getApplicationContext(), "AD UPLOADED!", Toast.LENGTH_SHORT).show();
        sharedPrefs.edit().clear().apply();

    }


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


    SharedPreferences.OnSharedPreferenceChangeListener prefsListener2 = new SharedPreferences.OnSharedPreferenceChangeListener() {
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


    private View.OnClickListener saveButtonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            try {
                Date = DateEditText.getText().toString();
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "ENTER VALID DATE", Toast.LENGTH_SHORT).show();
            }

            try {
                Price = PriceEditText.getText().toString();
            } catch (Exception e) {
                Toast.makeText(getActivity().getApplicationContext(), "ENTER VALID PRICE", Toast.LENGTH_SHORT).show();
            }

            if (!ValidInfo()) {
                Toast.makeText(getActivity().getApplicationContext(), "ENTER VALID INFO", Toast.LENGTH_SHORT).show();
            } else {
                sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

                // Get a reference to the Shared Preferences editor
                SharedPreferences.Editor editor = sharedPrefs.edit();

                // Write the data to the Shared Preferences file
                editor.putString(getString(R.string.prefs_date_key), Date);
                editor.putString(getString(R.string.prefs_beginhour_key), beginHour + ":" + beginMinute);
                editor.putString(getString(R.string.prefs_finishhour_key), endHour + ":" + endMinute);
                editor.putString(getString(R.string.prefs_hourlyrate_key), Price);

                // Save the changes to the Shared Preferences file
                editor.apply();
                Toast.makeText(getActivity().getApplicationContext(), "TIME + PRICE SAVED!", Toast.LENGTH_SHORT).show();


            }

        }


    };


    public boolean ValidInfo() {
        if (beginHour.matches("Choose hour") || endHour.matches("Choose hour")) {
            System.out.println(1);
            return false;
        }
        if (beginMinute.matches("Choose minutes") || endMinute.matches("Choose minutes")) {
            System.out.println(2);
            return false;
        }

        String pattern = "^(3[01]|[12][0-9]|0?[1-9])/(1[0-2]|0?[1-9])/[0-9]{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(Date);
        if (!m.find()) {
            System.out.println(3);
            return false;
        }

        if (SpinEndHour.getSelectedItemPosition() < SpinBeginHour.getSelectedItemPosition()) {
            System.out.println(4);
            return false;
        }
        if (SpinEndHour.getSelectedItemPosition() == SpinBeginHour.getSelectedItemPosition()) {
            if (SpinEndMinute.getSelectedItemPosition() <= SpinBeginMinute.getSelectedItemPosition()) {
                System.out.println(5);
                return false;
            }
        }

        if (Price.isEmpty() || Double.valueOf(Price) <= 0) {
            return false;
        }
        return true;
    }


}