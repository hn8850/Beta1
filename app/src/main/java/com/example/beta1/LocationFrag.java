package com.example.beta1;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageException;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;


public class LocationFrag extends Fragment {
    EditText countryEditText, cityEditText, streetEditText, houseNumberEditText;
    Button saveButton, finishButton;

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
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_location, container, false);

        // Set up the edit texts and button
        countryEditText = view.findViewById(R.id.country);
        cityEditText = view.findViewById(R.id.city);
        streetEditText = view.findViewById(R.id.street);
        houseNumberEditText = view.findViewById(R.id.houseNum);
        saveButton = view.findViewById(R.id.next1);
        saveButton.setOnClickListener(saveButtonClickListener1);
        finishButton = view.findViewById(R.id.finish);

        finishButton.setOnClickListener(finishButtonClickListener);

        sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);
        sharedPrefs.registerOnSharedPreferenceChangeListener(prefsListener1);
        if (sharedPrefs.contains(getString(R.string.prefs_FINISH_key))) {
            finishButton.setVisibility(View.VISIBLE);
            finishButton.setClickable(true);
        }

        mDb = FirebaseDatabase.getInstance();
        mStorage = FirebaseStorage.getInstance();


        return view;
    }


    private View.OnClickListener saveButtonClickListener1 = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            String country = countryEditText.getText().toString();
            String city = cityEditText.getText().toString();
            String street = streetEditText.getText().toString();
            String houseNumber = houseNumberEditText.getText().toString();


            if ((country.isEmpty()) || (city.isEmpty()) || (street.isEmpty()) || (houseNumber.isEmpty())) {
                Toast.makeText(getActivity().getApplicationContext(), "ENTER ALL INFO", Toast.LENGTH_SHORT).show();
            } else {

                String address = country + "," + city + "," + street + " " + houseNumber;
                double[] latLng = getLatLngFromAddress(getActivity(), address);
                if (latLng == null) {
                    Toast.makeText(getActivity().getApplicationContext(), "ENTER VALID ADDRESS", Toast.LENGTH_SHORT).show();
                } else {
                    String latitude = String.valueOf(latLng[0]);
                    String longitude = String.valueOf(latLng[1]);


                    sharedPrefs = getActivity().getSharedPreferences(PREFS_NAME, PREFS_MODE);

                    // Get a reference to the Shared Preferences editor
                    SharedPreferences.Editor editor = sharedPrefs.edit();

                    // Write the data to the Shared Preferences file
                    editor.putString(getString(R.string.prefs_address_key), address);
                    editor.putString(getString(R.string.prefs_latitude_key), latitude);
                    editor.putString(getString(R.string.prefs_longitude_key), longitude);


                    // Save the changes to the Shared Preferences file
                    editor.apply();
                    Toast.makeText(getActivity().getApplicationContext(), "LOCATION SAVED!", Toast.LENGTH_SHORT).show();

                }

            }

        }


    };

    SharedPreferences.OnSharedPreferenceChangeListener prefsListener1 = new SharedPreferences.OnSharedPreferenceChangeListener() {
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
        DatabaseReference userAdRef = mDb.getReference("Users").child(userUid).child("ParkAds").child("Active ParkAds").child(path);
        userAdRef.setValue(ad);
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


    public static double[] getLatLngFromAddress(Context context, String address) {
        Geocoder geocoder = new Geocoder(context, Locale.getDefault());
        List<Address> addresses;
        double[] latLng = new double[2];

        try {
            addresses = geocoder.getFromLocationName(address, 1);
            if (addresses.size() > 0) {
                latLng[0] = addresses.get(0).getLatitude();
                latLng[1] = addresses.get(0).getLongitude();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return latLng;
    }

}