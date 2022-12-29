package com.example.beta1;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ParkAd {
    String latitude;
    String longitude;
    String userID;
    int Active;
    String Date;
    String BeginHour;
    String FinishHour;
    Double HourlyRate;
    //ArrayList<String> PictureUrl;
    String Description;
    String Address;

    public ParkAd(String latitude,String longitude,String userID, int active,String date, String beginHour, String finishHour, Double hourlyRate, String description, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.userID = userID;
        Active = active;
        Date = date;
        BeginHour = beginHour;
        FinishHour = finishHour;
        HourlyRate = hourlyRate;
        //this.PictureUrl = PictureUrl;
        Description = description;
        Address = address;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public int getActive() {
        return Active;
    }

    public void setActive(int active) {
        Active = active;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getBeginHour() {
        return BeginHour;
    }

    public void setBeginHour(String beginHour) {
        BeginHour = beginHour;
    }

    public String getFinishHour() {
        return FinishHour;
    }

    public void setFinishHour(String finishHour) {
        FinishHour = finishHour;
    }

    public Double getHourlyRate() {
        return HourlyRate;
    }

    public void setHourlyRate(Double hourlyRate) {
        HourlyRate = hourlyRate;
    }

//    public ArrayList<String> getPictureUrl() {
//        return PictureUrl;
//    }
//
//    public void setPictureUrl(ArrayList<String> pictureUrl) {
//        PictureUrl = pictureUrl;
//    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }


    /**
     * Checks if the given ID is valid according to Israeli standards.
     *
     * @param str The string of the given ID.
     * @return True or false according to the validity of the ID.
     */
    public static boolean valid_id(String str) {
        if (str.length() > 9) return false;
        int x;
        int sum = 0;
        int len = 9 - str.length();
        for (int i = 0; i < len; i++) {
            str = "0" + str;
        }
        for (int i = 0; i < str.length(); i++) {
            try {
                x = Integer.parseInt(str.substring(i, i + 1));
            } catch (Exception e) {
                return false;
            }
            if (i % 2 == 1) x = x * 2;
            if (x > 9) x = x % 10 + x / 10;
            sum += x;
        }
        return sum % 10 == 0;
    }


}
