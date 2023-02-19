package com.example.beta1;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.example.beta1.databinding.ActivityNaviBinding;
import com.example.beta1.databinding.Navi2Binding;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
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
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Navi extends FragmentActivity implements OnMapReadyCallback {


    private GoogleMap mMap;
    private Navi2Binding binding;
    private ActivityNaviBinding binding2;
    private final static int LOCATION_PERMISSION_CODE = 101;
    FirebaseDatabase fbDB;
    ArrayList<ParkAd> parkAds = new ArrayList<>();
    List<MarkerOptions> parkAdMarkerOptions = new ArrayList<>();
    List<Marker> parkAdMarkers;
    ArrayList<String> parkAdIDs;

    FirebaseAuth mAuth;
    String currUserID;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        binding = Navi2Binding.inflate(getLayoutInflater());
//        setContentView(binding.getRoot());

        binding2 = ActivityNaviBinding.inflate(getLayoutInflater());
        setContentView(binding2.getRoot());
        fbDB = FirebaseDatabase.getInstance();


        if (isLocationPermissionGranted()) {
            // Obtain the SupportMapFragment and get notified when the map is ready to be used.
            SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.map);
            mapFragment.getMapAsync(this);
        } else {
            requestLocationPermission();
        }


    }

    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(this);
        mMap = googleMap;
        animateCamera();
        SetParkAdMarkers(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                mMap.clear();
                for (MarkerOptions specificMarker : parkAdMarkerOptions) {
                    Marker marker = mMap.addMarker(specificMarker);
                    marker.showInfoWindow();
                }
                MarkerOptions marker = new MarkerOptions().position(latLng).draggable(false);
                mMap.addMarker(marker);
                NaviToMarker(latLng);

                System.out.println("onMapClick: Latitude = " + latLng.latitude + " , Longitude = " + latLng.longitude);
            }
        });

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                Intent si = new Intent(getApplicationContext(), ViewParkAd.class);
                si.putExtra("lat", marker.getPosition().latitude);
                si.putExtra("long", marker.getPosition().longitude);
                startActivity(si);
                return true;
            }
        });


    }


    public void CheckDateOfOrders() {
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser CurrentUserAuth = FirebaseAuth.getInstance().getCurrentUser();
        currUserID = CurrentUserAuth.getUid();
        DatabaseReference userOrders = fbDB.getReference("Users").child(currUserID).child("Orders").child("Active Orders");
        userOrders.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("d/M/yyyy")
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                        .toFormatter();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Order order = orderSnap.getValue(Order.class);
                    String currentDate = sdf.format(new Date());
                    String parkAdDateStr = order.getParkDate();
                    System.out.println("THIS IS DATE = " + parkAdDateStr);

                    try {
//                        Date current2 = sdf.parse(currentDate);
//                        Date parkAdDate = sdf.parse(parkAdDateStr.trim());
                        LocalDate current = LocalDate.parse(currentDate, formatter);
                        LocalDate parkAdDate = LocalDate.parse(parkAdDateStr, formatter);
                        System.out.println("current = " + current.toString() + " parkDate = " + parkAdDate.toString());

                        if (current.isAfter(parkAdDate)) {
                            System.out.println("Bad!");

                            UpdateOrderCompleted(orderSnap.getKey()); //OrderDate has passed,hence its completed
                        } else if (current.toString().equals(parkAdDate.toString())) {
                            System.out.println("good!");

                            long currentTimeMillis = System.currentTimeMillis();
                            Date current2 = new Date(currentTimeMillis);
                            String currentHour = sdf2.format(current2);
                            if (!isFirstTimeBeforeSecond(currentHour, order.getBeginHour())) {
                                if (isHourBetween(currentHour, order.getBeginHour(), order.getEndHour())) {
                                    System.out.println("great!");
                                    UpdateOrderActive(order.getParkAdID());
                                } else {
                                    UpdateOrderCompleted(orderSnap.getKey()); //OrderHour has passed,hence its completed

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

    public void UpdateOrderCompleted(String OrderID) {
        DatabaseReference finishedOrder = fbDB.getReference("Users").child(currUserID).child("Orders").child("Active Orders").child(OrderID);
        System.out.println("USER = " + currUserID);
        System.out.println("Order = " + OrderID);
        finishedOrder.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Order completeOrd = snapshot.getValue(Order.class);
                System.out.println(completeOrd.toString());
                DatabaseReference completeBranch = fbDB.getReference("Users").child(currUserID).child("Orders").child("Completed Orders").child(OrderID);
                completeBranch.setValue(completeOrd);
                System.out.println("GOOODDD");
                DatabaseReference orderBranch = fbDB.getReference("Orders").child(OrderID);
                orderBranch.setValue(null);
                finishedOrder.setValue(null);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }


    public void UpdateOrderActive(String ParkAdID) {

        int pos = -1;
        for (String parkAdIDtemp : parkAdIDs) {
            if (parkAdIDtemp.matches(ParkAdID)) pos = parkAdIDs.indexOf(parkAdIDtemp);
        }
        Marker activeMarker = parkAdMarkers.get(pos);
        activeMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET));

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

    public void SetParkAdMarkers(Context context) {
        DatabaseReference AdsDB = fbDB.getReference("ParkAds");
        AdsDB.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                parkAdMarkerOptions = new ArrayList<>();
                parkAdMarkers = new ArrayList<>();
                parkAdIDs = new ArrayList<>();
                parkAds.clear();
                SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy", Locale.getDefault());
                SimpleDateFormat sdf2 = new SimpleDateFormat("HH:mm", Locale.getDefault());
                DateTimeFormatter formatter = new DateTimeFormatterBuilder()
                        .appendPattern("d/M/yyyy")
                        .parseDefaulting(ChronoField.DAY_OF_MONTH, 1)
                        .parseDefaulting(ChronoField.MONTH_OF_YEAR, 1)
                        .toFormatter();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ParkAd parkAd = snapshot1.getValue(ParkAd.class);
                    String currentDate = sdf.format(new Date());
                    String parkAdDateStr = parkAd.getDate();
                    try {
                        LocalDate current = LocalDate.parse(currentDate, formatter);
                        LocalDate parkAdDate = LocalDate.parse(parkAdDateStr, formatter);

                        System.out.println("current2 = " + current.toString() + " parkDate2 = " + parkAdDate.toString());


                        if (current.isAfter(parkAdDate)) {
                            UpdateParkAdCompleted(snapshot1.getKey()); //parkDate has passed,hence its completed
                            System.out.println("Bad2!");
                        } else if (current.toString().equals(parkAdDate.toString())) {
                            System.out.println("good2!");
                            long currentTimeMillis = System.currentTimeMillis();
                            Date current2 = new Date(currentTimeMillis);
                            String currentHour = sdf2.format(current2);
                            if (!isHourBetween(currentHour, parkAd.getBeginHour(), parkAd.getFinishHour()) && !isFirstTimeBeforeSecond(currentHour, parkAd.getBeginHour())) {
                                UpdateParkAdCompleted(snapshot1.getKey()); //ParkHour has passed,hence its completed
                            } else {
                                System.out.println("great2!");
                                parkAds.add(parkAd);
                                parkAdIDs.add(snapshot1.getKey());
                                System.out.println("PARK AD ADDED2");
                            }


                        } else {
                            System.out.println("great2!");
                            parkAds.add(parkAd);
                            parkAdIDs.add(snapshot1.getKey());
                            System.out.println("PARK AD ADDED2");
                        }
                    } catch (Error e) {
                        System.out.println("CHECK THIS");
                    }


                }

                BitmapDescriptor blueMarkerIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                CustomInfoWindowAdapter customInfoWindow = new CustomInfoWindowAdapter(context);
                mMap.setInfoWindowAdapter(customInfoWindow);

                for (ParkAd parkAd : parkAds) {
                    LatLng location = new LatLng(Double.parseDouble(parkAd.getLatitude()), Double.parseDouble(parkAd.getLongitude()));
                    MarkerOptions markerOptions = new MarkerOptions().icon(blueMarkerIcon)
                            .position(location)
                            .title(parkAd.getHourlyRate().toString());
                    // Add the marker to the map
                    Marker marker = mMap.addMarker(markerOptions);
                    customInfoWindow.getInfoContents(marker);
                    marker.showInfoWindow();
                    parkAdMarkerOptions.add(markerOptions);
                    parkAdMarkers.add(marker);
                    System.out.println("LOCATION = " + location);
                    System.out.println("ADDress = " + parkAd.getAddress());
                }

                CheckDateOfOrders();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    public void UpdateParkAdCompleted(String ParkAdID) {
        DatabaseReference ExpiredAd = fbDB.getReference("ParkAds").child(ParkAdID);
//        System.out.println("USER = " + currUserID);
//        System.out.println("Order = " + OrderID);
        ExpiredAd.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ParkAd completeAd = snapshot.getValue(ParkAd.class);
                DatabaseReference completeBranch = fbDB.getReference("Users").child(completeAd.getUserID()).child("ParkAds").child("Completed ParkAds");
                String key = completeBranch.push().getKey();
                completeBranch.child(key).setValue(completeAd);
                System.out.println("GOOODDD");
                DatabaseReference activeAdBranch = fbDB.getReference("Users").child(completeAd.getUserID()).child("ParkAds").child("Active ParkAds").child(ParkAdID);
                activeAdBranch.setValue(null);
                ExpiredAd.setValue(null);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

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

    public static boolean isFirstTimeBeforeSecond(String firstTimeStr, String secondTimeStr) {
        try {
            // Format the input strings with leading zeros for single-digit hours
            firstTimeStr = String.format("%02d", Integer.parseInt(firstTimeStr.substring(0, firstTimeStr.indexOf(":")))) + firstTimeStr.substring(firstTimeStr.indexOf(":"));
            secondTimeStr = String.format("%02d", Integer.parseInt(secondTimeStr.substring(0, secondTimeStr.indexOf(":")))) + secondTimeStr.substring(secondTimeStr.indexOf(":"));

            // Parse the time strings into LocalTime objects
            LocalTime firstTime = LocalTime.parse(firstTimeStr);
            LocalTime secondTime = LocalTime.parse(secondTimeStr);

            // Compare the LocalTime objects and return the result
            return firstTime.isBefore(secondTime);
        } catch (Error e) {
            // Handle any parse errors
            System.err.println("Error parsing time string: " + e.getMessage());
            return false;
        }
    }

    private static boolean isHourBetween(String checkHour, String beginHour, String endHour) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm");
        try {
            Date check = formatter.parse(checkHour);
            Date begin = formatter.parse(beginHour);
            Date end = formatter.parse(endHour);
            if (check.after(begin) && check.before(end)) {
                return true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

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
     * to use later... (custom markers)
     */
/*
   public void addCustomMarker(GoogleMap googleMap, LatLng latLng, String title) {
        // create a blue circle marker icon
        Drawable circleDrawable = getResources().getDrawable(R.drawable.blue_circle);
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);

        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(latLng);
        markerOptions.title(title);
        markerOptions.icon(markerIcon);

        Marker marker = googleMap.addMarker(markerOptions);
        marker.setVisible(true); // set the marker's title to always be visible
    }

    private static BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
 */
    @SuppressLint("MissingPermission")
    private void animateCamera() {
        System.out.println("00000");
        Location location = getLastKnownLocation();
        if (location != null) {
            System.out.println("11111");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                System.out.println("22222");
                return;
            }
            System.out.println("333333");
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            //delay is for after map loaded animation starts
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("444444");
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 10));

                }
            }, 2000);
        }
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        @SuppressLint("MissingPermission") Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            Log.d("TAG", "Latitude: " + location.getLatitude() +
                    " Longitude: " + location.getLongitude());
        }
        try {
            System.out.println(location.toString());
        } catch (Exception e) {
            System.out.println("WHYYY");
        }
        return location;
    }


    private boolean isLocationPermissionGranted() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_CODE);
    }

    /*
    public void NaviwithADB(@NonNull LatLng latLng) {
        double latitude = latLng.latitude;
        double longitude = latLng.longitude;

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a map app");

        builder.setPositiveButton("Google Maps", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Launch Google Maps
                Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            }
        });

        builder.setNeutralButton("NO NAVIGATION", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });

        builder.setNegativeButton("Waze", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Launch Waze
                try {
                    String url = "waze://?q=" + latitude + "," + longitude;
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    // If Waze is not installed, open it in Google Play:
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"));
                    startActivity(intent);
                }
            }
        });
        builder.show();
    }
    */


}