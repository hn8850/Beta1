package com.example.beta1;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 27/12/2022
 * The UploadAd Activity is a hub for all of the 3 different Fragments used to collect information
 * about a ParkAd and upload it to the database.
 */

public class UploadAd extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_ad);
        tabLayout = findViewById(R.id.Tabs);
        viewPager = findViewById(R.id.viewpager);

        tabLayout.setupWithViewPager(viewPager);

        FragAdapter fragAdapter = new FragAdapter(getSupportFragmentManager());
        fragAdapter.addFrag(new LocationFrag(),"Location");
        fragAdapter.addFrag(new InfoFrag(),"Info");
        fragAdapter.addFrag(new TimeFrag(),"Time + Price");
        viewPager.setAdapter(fragAdapter);
    }
}