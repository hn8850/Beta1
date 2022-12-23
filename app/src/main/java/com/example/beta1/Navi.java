package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.beta1.databinding.ActivityNaviBinding;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Navi extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ActivityNaviBinding binding;
    private final static int LOCATION_PERMISSION_CODE = 101;
    private static final String GEO_URI_PREFIX = "geo:";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNaviBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(latLng));
                NaviToMarker(latLng);
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

        if (doAppsExist(appsList)){
            // Create an app chooser using the createChooser method of the Intent class and pass in the Intents for Google Maps and Waze
            Intent chooserIntent = Intent.createChooser(googleMapsIntent, "Navigate using:");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Intent[]{wazeIntent});
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Choose a navigation app:");

            // Start the app chooser using the startActivity method of the Activity class
            startActivity(chooserIntent);
        }
        else{
            Intent playStoreIntent = new Intent(Intent.ACTION_VIEW);
            playStoreIntent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.waze"));
            startActivity(playStoreIntent);
        }

        
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


    @SuppressLint("MissingPermission")
    private void animateCamera() {
        Location location = getLastKnownLocation();
        if (location != null) {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
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
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15));

                }
            }, 2000);
        }
    }

    private Location getLastKnownLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = locationManager.getProviders(true);
        Location bestLocation = null;
        for (String provider : providers) {
            @SuppressLint("MissingPermission") Location l = locationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
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