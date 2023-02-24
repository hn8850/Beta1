package com.example.beta1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Order {
    String parkAdID;
    String renterID;
    String confirmDate;
    String parkDate;
    String beginHour;
    String endHour;
    double hourlyRate;
    String sellerId;
    String parkAddress;
    boolean isComplete;
    boolean isCanceled;

    public Order() {

    }

    public Order(String parkAdID, String renterID, String confirmDate, String parkDate, String beginHour, String endHour, double hourlyRate, String sellerId, String parkAddress) {
        this.parkAdID = parkAdID;
        this.renterID = renterID;
        this.confirmDate = confirmDate;
        this.parkDate = parkDate;
        this.beginHour = beginHour;
        this.endHour = endHour;
        this.hourlyRate = hourlyRate;
        this.sellerId = sellerId;
        this.parkAddress = parkAddress;

    }

    public String getParkAdID() {
        return parkAdID;
    }

    public void setParkAdID(String parkAdID) {
        this.parkAdID = parkAdID;
    }

    public String getRenterID() {
        return renterID;
    }

    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }

    public String getConfirmDate() {
        return confirmDate;
    }

    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }

    public String getParkDate() {
        return parkDate;
    }

    public void setParkDate(String parkDate) {
        this.parkDate = parkDate;
    }

    public String getBeginHour() {
        return beginHour;
    }

    public void setBeginHour(String beginHour) {
        this.beginHour = beginHour;
    }

    public String getEndHour() {
        return endHour;
    }

    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    public double getHourlyRate() {
        return hourlyRate;
    }

    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }


    public String getSellerId() {
        return sellerId;
    }

    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    public String getParkAddress() {
        return parkAddress;
    }

    public void setParkAddress(String parkAddress) {
        this.parkAddress = parkAddress;
    }

    public boolean isComplete() {
        return isComplete;
    }

    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    public boolean isCanceled() {
        return isCanceled;
    }

    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    public boolean isActive() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String startDate = parkDate + " " + beginHour;
        String endDate = parkDate + " " + endHour;
        try {
            Date start = dateFormat.parse(startDate);
            Date end = dateFormat.parse(endDate);
            Date now = Calendar.getInstance().getTime();
            return now.after(start) && now.before(end);
        } catch (ParseException e) {
            System.out.println("WHY THO");
            return false;
        }
    }

    public double getPrice() {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            Date date1 = sdf.parse(beginHour);
            Date date2 = sdf.parse(endHour);
            long diffInMilliseconds = date2.getTime() - date1.getTime();
            double diffInMinutes = (Math.abs(diffInMilliseconds) / (1000 * 60));
            return (diffInMinutes / 60) * hourlyRate;
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
    }

}
