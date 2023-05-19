package com.example.beta1;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalTime;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.6
 * @since 24/2/2023
 * The Services class is a collection of widely used SubMethod across all of the different classes
 * in the project.
 * Mainly used for date and time comparisons/castings to String values.
 */
public class Services {

    /**
     * Boolean Method that validates if a given string is a valid phone number in Israel.
     *
     * @param phoneNumber The phone number to be validated (String).
     * @return: The Method returns true if the phone number is valid, false otherwise.
     */
    public static boolean isValidPhoneNumber(String phoneNumber) {
        String regex = "^0?(([23489]{1}\\d{7})|[5]{1}[012345689]\\d{7})$";
        return phoneNumber.matches(regex);
    }


    /**
     * Boolean SubMethod used within the reading process's of Orders and ParkAds all across the app.
     * Used to check if a given time String describes a hour after the hour described by another
     * time String.
     *
     * @param firstTimeStr:  The time String to be checked if its before the secondTimeStr String ('HH:mm' format).
     * @param secondTimeStr: The time String to be checked if its after the firstTimeStr String ('HH:mm' format).
     * @return: The Method returns true if the time described by the firstTimeStr String is before
     * the time described by the secondTimeStr String, false otherwise.
     */
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

    /**
     * Boolean SubMethod used within the reading process's of Orders and ParkAds all across the app.
     * Used to verify that a given hour is between 2 other hours.
     *
     * @param checkHour: The hour to be checked (String, 'HH:mm' format).
     * @param beginHour: The hour at which the time range that is checked begins (String, 'HH:mm' format).
     * @param endHour:   The hour at which the time range that is checked ends (String, 'HH:mm' format).
     * @return: The Method returns true if the time described by the checkHour String is between or
     * equal to the time's described by the beginHour and endHour Strings. false otherwise.
     */
    public static boolean isHourBetween(String checkHour, String beginHour, String endHour) {
        try {
            // Parse time strings into Date objects using SimpleDateFormat
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date checkTime = sdf.parse(checkHour);
            Date beginTime = sdf.parse(beginHour);
            Date endTime = sdf.parse(endHour);

            // Compare checkTime with beginTime and endTime
            return checkTime.compareTo(beginTime) >= 0 && checkTime.compareTo(endTime) <= 0;
        } catch (ParseException e) {
            // Handle any parsing exceptions (e.g., invalid time format)
            e.printStackTrace();
            return false;
        }
    }


    /**
     * String SubMethod used  within the reading and writing process's of Orders and ParkAds all
     * across the app.
     * Used to unify date String format (to include leading zeroes across database, and for display
     * purposes), as well as used to compare between int values of different date Strings.
     *
     * @param dateString: The date String to receive leading zeros (e.g '1/1/1970' will return as
     *                    '01/01/1970').
     * @param Slashes:    boolean used to identify is the Method is to be used for display or comparison
     *                    purposes.
     *                    A true value will return a String in the 'dd/MM/yyyy' format to display, while
     *                    A false value will return a String in the 'yyyyMMdd', for date comparison.
     * @return: The Method returns a date strings with leading zeroes added to it (if need be).
     */
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

    /**
     * SubMethod used as part of the Receipt Object creation process.
     * Used to document the time at which a Receipt was created.
     *
     * @return: A time String in the  "dd/MM/yyyy 'at' HH:mm" format.
     */
    public static String getCurrentTimeFormatted() {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy 'at' HH:mm");
        Date currentDate = new Date();
        String formattedDate = formatter.format(currentDate);

        return formattedDate;
    }

    /**
     * Boolean SubMethod used within the reading process's of Orders and ParkAds all across the app.
     * Used to check if a given time range is after the current hour.
     *
     * @param startTime: Time String that describes the beginning of the time range ('HH:mm' format).
     * @param endTime:   Time String that describes the ending of the time range ('HH:mm' format).
     * @return: The Method returns true if the time range described by the startTime and endTime
     * Strings is after the current hour. false otherwise.
     */
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


    /**
     * Boolean SubMethod used to verify date inputs by the user.
     *
     * @param dateStr: The date String to be checked.
     * @return: The Method returns true if the date string describes a valid date,false otherwise.
     */
    public static boolean isValidDate2(String dateStr) {
        SimpleDateFormat sdfrmt = new SimpleDateFormat("dd/MM/yyyy");
        sdfrmt.setLenient(false);
        try
        {
            Date javaDate = sdfrmt.parse(dateStr);
        }
        catch (ParseException e)
        {
            return false;
        }
        return true;
    }

    /**
     * Boolean SubMethod used within the reading process's of Orders and ParkAds all across the app.
     * Used to check if a given date is between or equal to the date range described by the other
     * 2 dates given.
     *
     * @param checkDate: The date to be checked for being in range (String, 'dd/MM/yyyy' format).
     * @param date1:     Date String that describes the beginning of the date range ('dd/MM/yyyy' format).
     * @param date2:     Date String that describes the ending of the date range ('dd/MM/yyyy' format).
     * @return: The Method returns true if the date described in the checkDate String is between or
     * equal to the dates described in the date1 and date2 Strings. false otherwise.
     */
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

    /**
     * Boolean SubMethod used in the HourSelect Activity.
     * Used in case the ParkAd viewed by the user corresponds with the current date.
     * In that case, the Method receives the current time and calculates which quarter of an hour is
     * closest to the time given.
     *
     * @param timeString: Time String to be used as the base for the calculation process ('HH:mm' format).
     * @return: The Method returns a time String depicting the closest quarter hour to the timeString
     * given ('HH:mm' format).
     */
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

    /**
     * SubMethod for the information verification process.
     * Used to handle user errors regarding the information that was submitted, by creating
     * AlertDialog boxes.
     *
     * @param message: The message containing what the user did wrong when submitting information.
     */
    public static  void ErrorAlert(String message, Context context) {
        AlertDialog.Builder adb = new AlertDialog.Builder(context);
        adb.setTitle("An error occurred when saving your info!");
        adb.setMessage(message);
        adb.setNeutralButton("Return", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        AlertDialog dialog = adb.create();
        dialog.show();
    }


    /**
     * Checks if a date string in the format "dd/MM/yyyy" describes a date that has not been reached yet.
     *
     * @param dateString the date string to be checked in the format "dd/MM/yyyy"
     * @return {@code true} if the date has not been reached yet, {@code false} otherwise
     */
    public static boolean isDateNotReached(String dateString) {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        dateFormat.setLenient(false); // Disable lenient parsing (e.g., 31/02/2023 will throw ParseException)

        try {
            Date currentDate = new Date(); // Get the current date
            Date inputDate = dateFormat.parse(dateString); // Parse the input date string

            // Compare the dates
            return currentDate.before(inputDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false; // Invalid date string
        }
    }


}
