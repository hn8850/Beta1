package com.example.beta1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.beta1.databinding.Navi2Binding;
import com.google.android.gms.common.util.MapUtils;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 3.1
 * @since 23/12/2022
 * The Navi Activity is the main Activity of the Spark app.
 * In this Activity, the user can view all of the available ParkAds in the GoogleMap View.
 * The user can also search and filter ParkAds for more specific results.
 */

public class Navi extends AppCompatActivity implements OnMapReadyCallback {
    Button filter;
    Button search;
    EditText searchBar;
    ImageButton postAdButton, profileButton;

    private GoogleMap mMap;
    private Navi2Binding binding;
    private final static int LOCATION_PERMISSION_CODE = 101;
    FirebaseDatabase fbDB;
    ArrayList<ParkAd> parkAds;
    List<MarkerOptions> parkAdMarkerOptions;
    List<Marker> parkAdMarkers;
    ArrayList<String> parkAdIDs;
    ArrayList<ParkAd> sortedAds = new ArrayList<>();
    ArrayList<String> sortedIDs = new ArrayList<>();
    List<MarkerOptions> sortedParkAdMarkerOptions = new ArrayList<>();
    List<Marker> sortedParkAdMarkers = new ArrayList<>();

    HashMap<String, String> query = new HashMap<>();

    SupportMapFragment mapFragment;
    ValueEventListener parkAdUpdateListener;
    FirebaseAuth mAuth;
    String currUserID;
    String searchQuery;

    LatLng searchedParkAdLocation;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = Navi2Binding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        fbDB = FirebaseDatabase.getInstance();

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        currUserID = CurrentUserAuth.getUid();

        query.put("date1", "NONE");
        query.put("date2", "NONE");
        parkAdMarkerOptions = new ArrayList<>();
        parkAdMarkers = new ArrayList<>();
        parkAdIDs = new ArrayList<>();
        parkAds = new ArrayList<>();


        mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder adb = new AlertDialog.Builder(Navi.this);
        adb.setTitle("Want to leave?");
        adb.setMessage("Press Close App to leave the app, or Dismiss to stay!");
        adb.setNegativeButton("Close App", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishAffinity();
            }
        });
        adb.setPositiveButton("Dismiss", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = adb.create();
        dialog.show();
    }

    /**
     * Sets Up the Map View and all of the Views that depend on it.
     *
     * @param googleMap: The GoogleMap Object linked to the Map View.
     */
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(this);
        mMap = googleMap;
        if (isLocationPermissionGranted()) {
            mMap.setMyLocationEnabled(true);
            animateCamera();
        } else {
            requestLocationPermission();
        }
        SetParkAdMarkers();
        filter = findViewById(R.id.filter);
        filter.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickMethod for the filter Button.
             * Calls the createFilterDialog.
             * @param view: The filter Button.
             */
            @Override
            public void onClick(View view) {
                createFilterDialog();
            }
        });
        searchBar = findViewById(R.id.searchBar);
        search = findViewById(R.id.search2);
        search.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickMethod for the search Button.
             * Calls the searchQueryMethod.
             * @param view: The search Button.
             */
            @Override
            public void onClick(View view) {
                searchQuery();
            }
        });

        postAdButton = findViewById(R.id.imageButton);
        postAdButton.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickMethod for the postAd Button.
             * Launches the UploadAd Activity.
             * @param view: The postAd Button.
             */
            @Override
            public void onClick(View view) {
                Intent si = new Intent(Navi.this, UploadAd.class);
                startActivity(si);
            }
        });

        profileButton = findViewById(R.id.imageButton3);
        profileButton.setOnClickListener(new View.OnClickListener() {
            /**
             * OnClickMethod for the profile Button.
             * Launches the Settings Activity.
             * @param view: The profile Button.
             */
            @Override
            public void onClick(View view) {
                Intent si = new Intent(Navi.this, SettingsScreen.class);
                startActivity(si);
            }
        });


        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            /**
             * OnClickMethod for the ParkAd Markers on the Map View.
             * Launches the ParkAdQueryListView Activity.
             * @param marker: The ParkAd Marker that was Clicked
             * @return: always returns true.
             */
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Intent si = new Intent(getApplicationContext(), ParkAdQueryListView.class);
                String latitude = String.valueOf(marker.getPosition().latitude);
                String longitude = String.valueOf(marker.getPosition().longitude);
                si.putExtra("lat", latitude);
                si.putExtra("long", longitude);
                si.putExtra("date1", query.get("date1"));
                si.putExtra("date2", query.get("date2"));

                startActivity(si);
                return true;
            }
        });

    }

    /**
     * Reads the ParkAds Branch of the database, updates the completion status of all ParkAds
     * according to the current date and time, and then adds Markers on the Map View for each active
     * ParkAd.
     */
    public void SetParkAdMarkers() {
        DatabaseReference AdsDB = fbDB.getReference("ParkAds");
        Query parkAdUpdateQuery = AdsDB;
        parkAdUpdateListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat dateFormat = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String currentDate = dateFormat.format(new Date());
                currentDate = Services.addLeadingZerosToDate(currentDate, false);
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ParkAd parkAd = snapshot1.getValue(ParkAd.class);
                    if (!currUserID.matches(parkAd.getUserID())) {
                        String parkAdDateStr = parkAd.getDate();
                        parkAdDateStr = Services.addLeadingZerosToDate(parkAdDateStr, false);
                        try {
                            if (Integer.valueOf(currentDate) > Integer.valueOf(parkAdDateStr)) {
                                UpdateParkAdCompleted(snapshot1.getKey()); //parkDate has passed,hence its completed
                            } else if (parkAdDateStr.matches(currentDate)) {
                                long currentTimeMillis = System.currentTimeMillis();
                                Date current2 = new Date(currentTimeMillis);
                                String currentHour = hourFormat.format(current2);
                                if (!Services.isFirstTimeBeforeSecond(currentHour, parkAd.getFinishHour())) {
                                    UpdateParkAdCompleted(snapshot1.getKey()); //ParkHour has passed,hence its completed
                                } else {
                                    parkAds.add(parkAd);
                                    parkAdIDs.add(snapshot1.getKey());
                                }
                            } else {
                                parkAds.add(parkAd);
                                parkAdIDs.add(snapshot1.getKey());
                            }
                        } catch (Error e) {
                            Services.ErrorAlert(e.getMessage(),Navi.this);
                        }
                    }

                }
                BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                for (ParkAd parkAd : parkAds) {
                    LatLng location = new LatLng(Double.parseDouble(parkAd.getLatitude()), Double.parseDouble(parkAd.getLongitude()));
                    MarkerOptions markerOptions = new MarkerOptions().icon(blueMarkerIcon)
                            .position(location);
                    Marker marker = mMap.addMarker(markerOptions);
                    marker.showInfoWindow();

                    parkAdMarkerOptions.add(markerOptions);
                    parkAdMarkers.add(marker);
                    sortedAds.add(parkAd);
                    sortedIDs.add(parkAdIDs.get(parkAds.indexOf(parkAd)));
                    sortedParkAdMarkerOptions.add(markerOptions);
                    sortedParkAdMarkers.add(marker);
                }
                VerifyDateOfOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        parkAdUpdateQuery.addValueEventListener(parkAdUpdateListener);
    }

    /**
     * Method used to iterate through the Orders Branch of the current User in the database, and
     * update the completion status of each order according to the current date.
     */
    public void VerifyDateOfOrders() {
        DatabaseReference userOrders = fbDB.getReference("Users").child(currUserID).child("Orders");
        userOrders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    String currentDate = sdf.format(new Date());
                    String parkAdDateStr = order.getParkDate();
                    DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                            .appendPattern("d/M/yyyy")
                            .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                            .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                            .toFormatter();
                    try {
                        if (!(order.isComplete || order.isCanceled)) {
                            LocalDate current = LocalDate.parse(currentDate, formatter);
                            LocalDate parkAdDate = LocalDate.parse(parkAdDateStr, formatter);
                            if (current.isAfter(parkAdDate)) {
                                UpdateOrderCompleted(orderSnap.getKey(), order); //OrderDate has passed,hence its completed
                            } else if (current.toString().equals(parkAdDate.toString())) {
                                long currentTimeMillis = System.currentTimeMillis();
                                Date current2 = new Date(currentTimeMillis);
                                String currentHour = sdf2.format(current2);

                                if (!Services.isFirstTimeBeforeSecond(currentHour, order.getBeginHour())) {
                                    if (Services.isHourBetween(currentHour, order.getBeginHour(), order.getEndHour())) {
                                        UpdateOrderActive(order,orderSnap.getKey());
                                    } else {
                                        UpdateOrderCompleted(orderSnap.getKey(), order); //OrderHour has passed,hence its completed
                                    }
                                }
                            }
                        }

                    } catch (Error e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    /**
     * SubMethod for the VerifyDateOfOrders Method. Used to update the completion status of a given
     * order to 'completed'.
     *
     * @param OrderID: The KeyID in the database for the completed order.
     */
    public void UpdateOrderCompleted(String OrderID, Order order) {
        DatabaseReference finishedOrder = fbDB.getReference("Users").child(currUserID).child("Orders").child(OrderID);
        finishedOrder.child("complete").setValue(true);
        finishedOrder.child("active").setValue(false);
        DatabaseReference orderBranch = fbDB.getReference("Orders").child(OrderID);
        orderBranch.setValue(null);

        AlertDialog.Builder adb = new AlertDialog.Builder(this);
        adb.setTitle("Order Completed!");
        adb.setMessage("Want to leave a Review?");
        adb.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Intent si = new Intent(getApplicationContext(), WriteReview.class);
                si.putExtra("SellerID", order.getSellerId());
                si.putExtra("UID", currUserID);
                startActivity(si);
                dialogInterface.dismiss();
            }
        });
        adb.setNegativeButton("No!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        adb.create().show();

    }

    /**
     * SubMethod for the VerifyDateOfOrders Method. Used to update the completion status of a given
     * order to 'active'.
     * @param order: The Order Object that corresponds to the active order.
     * @param orderID: The KeyID in the database for the order.

     */
    public void UpdateOrderActive(Order order,String orderID) {
        String parkAdID = order.getParkAdID();
        int pos = -1;
        for (String parkAdIDtemp : sortedIDs) {
            if (parkAdIDtemp.matches(parkAdID)) {
                pos = sortedIDs.indexOf(parkAdIDtemp);
                break;
            }
        }
        Marker activeMarker = sortedParkAdMarkers.get(pos);
        BitmapDescriptor activeParkAdMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);

        activeMarker.setIcon(activeParkAdMarkerIcon);

        DatabaseReference generalOrderRef = fbDB.getReference("Orders").child(orderID);
        generalOrderRef.child("active").setValue(true);
        DatabaseReference userOrderRef = fbDB.getReference("Users").child(order.getRenterID()).child("Orders").child(orderID);
        userOrderRef.child("active").setValue(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("You Have An Active Parking Space");
        builder.setMessage("Want to navigate to the location?");
        builder.setPositiveButton("Navigate", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                NaviToMarker(activeMarker.getPosition());
            }
        });
        builder.setNegativeButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.show();
    }

    /**
     * SubMethod for the SetParkAdMarkers Method. Used to update the completion status of a given
     * ParkAd to 'completed'.
     *
     * @param ParkAdID: The KeyID in the database for the completed ParkAd.
     */
    public void UpdateParkAdCompleted(String ParkAdID) {
        DatabaseReference ExpiredAd = fbDB.getReference("ParkAds").child(ParkAdID);
        ExpiredAd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ParkAd completeAd = snapshot.getValue(ParkAd.class);
                completeAd.setActive(0);
                DatabaseReference completeBranch = fbDB.getReference("Users").child(completeAd.getUserID()).child("ParkAds");
                completeBranch.child(ParkAdID).setValue(completeAd);
                ExpiredAd.setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    /**
     * SubMethod for the OnClickMethod of the filter Button.
     * Used to create a Dialog box that updates the filter query with a date range that the user
     * submits, and then calls the sortParkAds Method.
     */
    public void createFilterDialog() {
        final Dialog dialog = new Dialog(Navi.this);
        dialog.setContentView(R.layout.query_dialog_box);
        // Get references to the EditText fields
        final EditText editTextDD1 = dialog.findViewById(R.id.edit_text_dd_1);
        final EditText editTextMM1 = dialog.findViewById(R.id.edit_text_mm_1);
        final EditText editTextYYYY1 = dialog.findViewById(R.id.edit_text_yyyy_1);
        final EditText editTextDD2 = dialog.findViewById(R.id.edit_text_dd_2);
        final EditText editTextMM2 = dialog.findViewById(R.id.edit_text_mm_2);
        final EditText editTextYYYY2 = dialog.findViewById(R.id.edit_text_yyyy_2);

        if (!query.get("date1").matches("NONE")) {
            String date1 = query.get("date1");
            String[] dateComponents = date1.split("/");
            editTextDD1.setText(dateComponents[0]);
            editTextMM1.setText(dateComponents[1]);
            editTextYYYY1.setText(dateComponents[2]);

            String date2 = query.get("date2");
            dateComponents = date2.split("/");
            editTextDD2.setText(dateComponents[0]);
            editTextMM2.setText(dateComponents[1]);
            editTextYYYY2.setText(dateComponents[2]);

        }
        // Get a reference to the "Submit" button
        Button submitButton = dialog.findViewById(R.id.dialog_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the user input

                    String date1 = editTextDD1.getText().toString() + "/" +
                            editTextMM1.getText().toString() + "/" +
                            editTextYYYY1.getText().toString();

                    String date2 = editTextDD2.getText().toString() + "/" +
                            editTextMM2.getText().toString() + "/" +
                            editTextYYYY2.getText().toString();

                    if (date1.matches("//") && date2.matches("//")){
                        query.put("date1", "NONE");
                        query.put("date2", "NONE");
                        sortParkAds();
                        dialog.dismiss();
                    }
                    else{
                        try {
                            date1 = Services.addLeadingZerosToDate(date1, true);
                            date2 = Services.addLeadingZerosToDate(date2, true);
                            if (Services.isValidDate2(date1) && Services.isValidDate2(date2)) {
                                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                                Date Date1 = sdf.parse(date1);
                                Date Date2 = sdf.parse(date2);
                                if (Date2.after(Date1) || Date2.compareTo(Date1) == 0) {
                                    query.put("date1", date1);
                                    query.put("date2", date2);
                                    sortParkAds();
                                    dialog.dismiss();


                                } else {
                                    Services.ErrorAlert("Dates must be in order!", Navi.this);
                                }
                            } else {
                                Services.ErrorAlert("Please enter valid dates!", Navi.this);
                            }

                        } catch (Exception e) {
                            Services.ErrorAlert(e.getMessage(),Navi.this);
                        }
                    }
            }
        });

        Button clearButton = dialog.findViewById(R.id.clearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                editTextDD1.setText("");
                editTextMM1.setText("");
                editTextYYYY1.setText("");
                editTextDD2.setText("");
                editTextMM2.setText("");
                editTextYYYY2.setText("");
            }
        });


        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams();
            layoutParams.copyFrom(window.getAttributes());
            layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
            window.setAttributes(layoutParams);
            window.setGravity(Gravity.CENTER);
        }
        // Show the dialog
        dialog.show();

    }

    /**
     * Used to sort the general ParkAd ArrayList's according to the user submitted query, and update
     * the Map View accordingly.
     */
    public void sortParkAds() {
        mMap.clear();
        sortedAds.clear();
        sortedIDs.clear();
        sortedParkAdMarkers.clear();
        sortedParkAdMarkerOptions.clear();

        int pos;
        String adDate;
        if (!query.get("date1").matches("NONE")) {
            for (ParkAd parkAd : parkAds) {
                adDate = parkAd.getDate();
                if (Services.isDateBetween(adDate, query.get("date1"), query.get("date2"))) {
                    pos = parkAds.indexOf(parkAd);
                    sortedAds.add(parkAd);
                    sortedIDs.add(parkAdIDs.get(pos));
                }
            }
        } else {
            for (ParkAd parkAd : parkAds) {
                pos = parkAds.indexOf(parkAd);
                sortedAds.add(parkAd);
                sortedIDs.add(parkAdIDs.get(pos));
            }
        }

        BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        BitmapDescriptor greenMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
        for (ParkAd parkAd : sortedAds) {
            LatLng location = new LatLng(Double.parseDouble(parkAd.getLatitude()), Double.parseDouble(parkAd.getLongitude()));
            MarkerOptions markerOptions;
            if (searchQuery!=null)     System.out.println("QUERY:" + searchQuery);
            else             System.out.println("QUERY:NULL");
            System.out.println("TEXT:" + searchBar.getText().toString());


            if (searchQuery!=null &&searchQuery.matches(searchBar.getText().toString()) && location.latitude == searchedParkAdLocation.latitude && location.longitude == searchedParkAdLocation.longitude) {
                markerOptions = new MarkerOptions().icon(greenMarkerIcon)
                        .position(location)
                        .snippet(String.valueOf(parkAd.getHourlyRate()));
            } else {
                markerOptions = new MarkerOptions().icon(blueMarkerIcon)
                        .position(location)
                        .snippet(String.valueOf(parkAd.getHourlyRate()));
            }
            Marker marker = mMap.addMarker(markerOptions);
            marker.showInfoWindow();
            sortedParkAdMarkerOptions.add(markerOptions);
            sortedParkAdMarkers.add(marker);
        }
    }

    /**
     * SubMethod for the OnClickMethod of the search Button.
     * Used to highlight ParkAd Markers that correspond with the address/city/country the user has
     * submitted.
     */
    public void searchQuery() {
        BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        BitmapDescriptor greenMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);

        for (Marker marker : sortedParkAdMarkers) {
            marker.setIcon(blueMarkerIcon);

        }
        try {
            searchQuery = searchBar.getText().toString().trim();
        } catch (Exception e) {
            Services.ErrorAlert("Enter Valid Address", Navi.this);
            return;
        }
        String[] addressComponents;
        int pos;
        for (ParkAd parkAd : sortedAds) {
            pos = sortedAds.indexOf(parkAd);
            addressComponents = parkAd.getAddress().split(",");
            if (searchQuery.matches(parkAd.getAddress())) {
                searchedParkAdLocation = new LatLng(Double.parseDouble(parkAd.getLatitude()), Double.parseDouble(parkAd.getLongitude()));
                zoomToAddress(this, mMap, parkAd.getAddress());
                Marker marker = sortedParkAdMarkers.get(pos);
                marker.setIcon(greenMarkerIcon);
                break;
            }
            for (String component : addressComponents) {
                if ((searchQuery.toLowerCase(Locale.ROOT)).matches(component.toLowerCase(Locale.ROOT))) {
                    Marker marker = sortedParkAdMarkers.get(pos);
                    zoomToLatLng(mMap, marker.getPosition(), 10);
                    marker.setIcon(greenMarkerIcon);
                    searchedParkAdLocation = new LatLng(Double.parseDouble(parkAd.getLatitude()), Double.parseDouble(parkAd.getLongitude()));
                }
            }
        }
    }

    /**
     * Used to launch a navigation app (Waze or Google Maps) and navigate to the currently active
     * order's location, if there is one.
     *
     * @param latLng: The location of the ParkAd associated with the order.
     */
    public void NaviToMarker(@NonNull LatLng latLng) {
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;
        String GoogleURL = "google.navigation:q=" + latitude + "," + longitude;
        Intent googleMapsIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(GoogleURL));

        // Add the CATEGORY_BROWSABLE category to the Intent
        googleMapsIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        // Set the package of the Intent to com.google.android.apps.maps (the package name for Google Maps)
        googleMapsIntent.setPackage("com.google.android.apps.maps");

        // Create an Intent for Waze with the ACTION_VIEW action and set the data for the location you want to navigate to
        String WazeURL = "waze://?q=" + latitude + "," + longitude + "&navigate=yes";
        Intent wazeIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WazeURL));

        // Add the CATEGORY_BROWSABLE category to the Intent
        wazeIntent.addCategory(Intent.CATEGORY_BROWSABLE);

        // Set the package of the Intent to com.waze (the package name for Waze)
        wazeIntent.setPackage("com.waze");

        List<Intent> appsList = new ArrayList<>();
        appsList.add(wazeIntent);
        appsList.add(googleMapsIntent);

        if (doAppsExist(appsList)) {
            // Create an app chooser using the createChooser method of the Intent class and pass in the Intents for Google Maps and Waze
              Intent chooserIntent = Intent.createChooser(googleMapsIntent, "Navigate using:");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{wazeIntent});
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose a navigation app:");

            // Start the app chooser using the startActivity method of the Activity class
            startActivity(chooserIntent);
        } else {
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            playStoreIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.waze"));
            startActivity(playStoreIntent);
        }


    }


    /**
     * Boolean SubMethod for the NaviToMarker Method.
     *
     * @param apps: A List of Intents containing the navigation apps.
     * @return: The Method returns true if at least one navigation app exists on the current device.
     * false otherwise.
     */
    private boolean doAppsExist(List<Intent> apps) {
        PackageManager packageManager = getPackageManager();
        List<ResolveInfo> activities = packageManager.queryIntentActivities(apps.get(0), 0);
        boolean isIntentSafe = activities.size() > 0;
        if (!isIntentSafe) {
            activities = packageManager.queryIntentActivities(apps.get(1), 0);
            isIntentSafe = activities.size() > 0;
            return isIntentSafe;
        }
        return true;
    }


    /**
     * SubMethod for the OnMapReady Method.
     * Used to animate a zoom in on the user's current location.
     */
    @SuppressLint("MissingPermission")
    private void animateCamera() {
        Location location = getLastKnownLocation();
        if (location != null) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            //delay is for after map loaded animation starts
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));

                }
            }, 2000);
        }
    }


    /**
     * SubMethod for the animateCamera Method.
     *
     * @return: the last known location of the user's device.
     */
    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            Log.d("TAG", "Latitude: " + location.getLatitude() +
                    " Longitude: " + location.getLongitude());
        }
        return location;
    }


    /**
     * Boolean Method used to determine if the user has given location permissions to the app.
     *
     * @return: The Method returns true if the user has given location permissions to the app.
     * false if not.
     */
    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Used to request location permissions from the user (in case they arent given yet).
     */
    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    /**
     * onRequestPermissionsResult Method for the requestLocationPermission Method.
     * Used to issue an AlertDialog box to the user notifying him the app requires location
     * permissions in order work properly.
     *
     * @param requestCode:  The LocationRequestCode.
     * @param permissions:  String[] containing the needed permissions.
     * @param grantResults: int[] containing the user feedback.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mMap.setMyLocationEnabled(true);
                animateCamera();
            } else {
                // Permission denied, show an explanation
                new AlertDialog.Builder(this)
                        .setTitle("Permission needed")
                        .setMessage("This permission is needed to access your location.")
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

            }
        }


    }


    /**
     * SubMethod for the SearchQuery Method.
     * Used to zoom the camera of the Map View on the address the user has submitted.
     * (This Method is only there's a ParkAd Marker at that location!).
     *
     * @param context:   The application's Context.
     * @param googleMap: The GoogleMap Object linked to the MapView.
     * @param address:   The address the user has submitted (String).
     */
    public static void zoomToAddress(Context context, GoogleMap googleMap, String address) {
        String TAG = MapUtils.class.getSimpleName();
        Geocoder geocoder = new Geocoder(context);
        try {
            List<Address> addresses = geocoder.getFromLocationName(address, 1);
            if (addresses != null && addresses.size() > 0) {
                Address resultAddress = addresses.get(0);
                double lat = resultAddress.getLatitude();
                double lon = resultAddress.getLongitude();
                LatLng latLng = new LatLng(lat, lon);
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, 15);
                googleMap.animateCamera(cameraUpdate);
            } else {
                Log.e(TAG, "No addresses found for the given address string: " + address);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error geocoding address: " + e.getMessage());
        }
    }

    /**
     * SubMethod for the SearchQuery Method.
     * Used to zoom the camera of the Map View on the LatLng of an address's component the user has
     * submitted (country/city/street etc).
     * (This Method is only there's a ParkAd Marker at that location!).
     *
     * @param googleMap: The GoogleMap Object linked to the MapView.
     * @param latLng:    The latLng of the location the user has submitted (String).
     * @param zoomLevel: The zoom level for the animation.
     */
    public static void zoomToLatLng(GoogleMap googleMap, LatLng latLng, float zoomLevel) {
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel);
        googleMap.animateCamera(cameraUpdate);
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        mAuth.signOut();
        Intent si = new Intent(Navi.this, Login.class);
        startActivity(si);

        return true;
    }

}