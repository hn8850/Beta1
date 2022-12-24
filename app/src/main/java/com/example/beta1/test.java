package com.example.beta1;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.location.Address;
import android.location.Geocoder;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class test extends AppCompatActivity {

    private EditText mEditTextAddress;
    private TextView mTextViewLatitude;
    private TextView mTextViewLongitude;
    private Button mButtonTranslate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mEditTextAddress = findViewById(R.id.edit_text_address);
        mTextViewLatitude = findViewById(R.id.text_view_latitude);
        mTextViewLongitude = findViewById(R.id.text_view_longitude);
        mButtonTranslate = findViewById(R.id.button_translate);

        mButtonTranslate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// get the address from the EditText
                String address = mEditTextAddress.getText().toString();

                // translate the address and display the latitude and longitude
                double[] latLng = getLatLngFromAddress(test.this, address);
                if (latLng != null) {
                    double latitude = latLng[0];
                    double longitude = latLng[1];

                    mTextViewLatitude.setText(String.format("Latitude: %f", latitude));
                    mTextViewLongitude.setText(String.format("Longitude: %f", longitude));
                }            }
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