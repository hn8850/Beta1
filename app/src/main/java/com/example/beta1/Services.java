package com.example.beta1;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Services {
    public static String addLeadingZerosToDate(String dateString, boolean Slashes) {
        String[] dateParts = dateString.split("/");
        String day = dateParts[0];
        String month = dateParts[1];
        String year = dateParts[2];

        // Check if day is a single digit and add a leading zero if necessary
        if (day.length() == 1) {
            day = "0" + day;
        }

        // Check if month is a single digit and add a leading zero if necessary
        if (month.length() == 1) {
            month = "0" + month;
        }

        if (Slashes) return day + '/' + month + '/' + year; //inside ParkAD
        return year + month + day; //ParkAdDateKey
    }

    public static String getCurrentTimeFormatted() {
        // Get the current date and time
        LocalDateTime currentDateTime = LocalDateTime.now();

        // Create a DateTimeFormatter for the desired output format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'at' hh:mm a");

        // Format the current date and time using the formatter
        String formattedDateTime = currentDateTime.format(formatter);

        // Return the formatted date and time string
        return formattedDateTime;
    }


}
