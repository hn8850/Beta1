package com.example.beta1;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static boolean isTimeRangeAfterCurrentHour(String startTime, String endTime) {
        // Get the current time as a LocalTime object
        LocalTime currentTime = LocalTime.now();
        // Parse the input start and end times as LocalTime objects
        LocalTime startTimeLocal = LocalTime.parse(startTime);
        LocalTime endTimeLocal = LocalTime.parse(endTime);
        // Check if the time range between the input start and end times is after the current time
        return endTimeLocal.isAfter(currentTime) && startTimeLocal.isAfter(currentTime);
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

    public static String roundToNextQuarterHour(String timeString) {
        String[] parts = timeString.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        // Check if the current minute is greater than or equal to 45.
        // If so, we need to round to the next hour.
        if (minute >= 45) {
            hour += 1;
            minute = 0;
        }
        // Otherwise, round to the next quarter of an hour.
        else {
            int minuteRounded = (int) Math.ceil(minute / 15.0) * 15;
            if (minuteRounded == 60) {
                hour += 1;
                minuteRounded = 0;
            }
            minute = minuteRounded;
        }

        return String.format("%02d:%02d", hour, minute);
    }


}
