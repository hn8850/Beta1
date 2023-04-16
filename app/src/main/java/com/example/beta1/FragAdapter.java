package com.example.beta1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 27/12/2022
 * This Class is used to manage the 3 different Fragments, which are part of the UploadAd Activity.
 */

public class FragAdapter extends FragmentPagerAdapter {

    private final ArrayList<Fragment> frags = new ArrayList<>();
    private final ArrayList<String> titles = new ArrayList<>();

    public FragAdapter(@NonNull FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return frags.get(position);
    }

    @Override
    public int getCount() {
        return frags.size();
    }

    public void addFrag(Fragment fragment,String title){
        frags.add(fragment);
        titles.add(title);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return titles.get(position);
    }
}
