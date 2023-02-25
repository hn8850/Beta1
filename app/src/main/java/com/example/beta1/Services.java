package com.example.beta1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Services {
    public static String addLeadingZerosToDate(String dateString, boolean Slashes) {
        String[] dateParts = dateString.split("/");
        String day = dateParts[0];
        String month = dateParts[1];
        String year = dateParts[2];

        if (day.length() == 1) {
            day = "0" + day;
        }

        if (month.length() == 1) {
            month = "0" + month;
        }

        if (Slashes) return day + '/' + month + '/' + year; //inside ParkAD
        return year + month + day; //ParkAdDateKey
    }

    //for receipt
    public static String getCurrentTimeFormatted() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
        Date currentDate = new Date();
        String formattedDate = formatter.format(currentDate);

        return formattedDate;
    }

    public static boolean isValidDate(String dd, String mm, String yyyy) {
        if (dd == null || mm == null | yyyy == null) return false;
        try {
            int day = Integer.parseInt(dd);
            int month = Integer.parseInt(mm);
            int year = Integer.parseInt(yyyy);
            if (day < 0) return false;
            if (day > 31) return false;
            if (month < 0) return false;
            if (month > 12) return false;
            //year will be addressed later
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean isValidDate2(String date) {
        String pattern = "^(3[01]|[12][0-9]|0?[1-9])/(1[0-2]|0?[1-9])/[0-9]{4}$";
        Pattern r = Pattern.compile(pattern);
        Matcher m = r.matcher(date);
        if (!m.find()) {
            System.out.println(3);
            return false;
        }
        return true;
    }

    public static boolean isDateBetween(String checkDate, String date1, String date2) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date checkDateObj = sdf.parse(checkDate);
            Date date1Obj = sdf.parse(date1);
            Date date2Obj = sdf.parse(date2);

            // Check if checkDate is between date1 and date2 (inclusive)
            return (checkDateObj.compareTo(date1Obj) >= 0 && checkDateObj.compareTo(date2Obj) <= 0);
        } catch (Exception e) {
            return false;
        }

    }


    public String keyToTime(String parkAdKey, String param) {
        return "";
    }


}
