package com.example.beta1;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.1
 * @since 1/2/2023
 * The Order Object Class.
 */

public class Order {

    /**
     * The database KeyID for the ParkAd the Order is associated with.
     */
    String parkAdID;

    /**
     * The database KeyID for the user who made the Order.
     */
    String renterID;

    /**
     * The date at which the Order was made.
     */
    String confirmDate;

    /**
     * The date at which the ParkAd associated with the Order is scheduled to.
     */
    String parkDate;

    /**
     * The hour at which the user's rental period of the ParkAd begins.
     */
    String beginHour;

    /**
     * The hour at which the user's rental period of the ParkAd ends.
     */
    String endHour;

    /**
     * The price per hour of renting the ParkAd Space, at the time the Order was made.
     */
    double hourlyRate;

    /**
     * The database KeyID for the user who published the ParkAd that is associated with the Order.
     */
    String sellerId;

    /**
     * The address of ParkAd that is associated with the Order.
     */
    String parkAddress;

    /**
     * The Is complete.
     */
    boolean isComplete;

    /**
     * boolean that is reflective of the Order's cancellation status. true if canceled. false if not.
     */
    boolean isCanceled;

    /**
     * Empty Constructor required for reading from FireBase Database.
     */
    public Order() {
    }

    /**
     * General complete Constructor for the Order Object.
     *
     * @param parkAdID    the park ad id
     * @param renterID    the renter id
     * @param confirmDate the confirm date
     * @param parkDate    the park date
     * @param beginHour   the begin hour
     * @param endHour     the end hour
     * @param hourlyRate  the hourly rate
     * @param sellerId    the seller id
     * @param parkAddress the park address
     */
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

    /**
     * Gets park ad id.
     *
     * @return the park ad id
     */
    public String getParkAdID() {
        return parkAdID;
    }

    /**
     * Sets park ad id.
     *
     * @param parkAdID the park ad id
     */
    public void setParkAdID(String parkAdID) {
        this.parkAdID = parkAdID;
    }

    /**
     * Gets renter id.
     *
     * @return the renter id
     */
    public String getRenterID() {
        return renterID;
    }

    /**
     * Sets renter id.
     *
     * @param renterID the renter id
     */
    public void setRenterID(String renterID) {
        this.renterID = renterID;
    }

    /**
     * Gets confirm date.
     *
     * @return the confirm date
     */
    public String getConfirmDate() {
        return confirmDate;
    }

    /**
     * Sets confirm date.
     *
     * @param confirmDate the confirm date
     */
    public void setConfirmDate(String confirmDate) {
        this.confirmDate = confirmDate;
    }

    /**
     * Gets park date.
     *
     * @return the park date
     */
    public String getParkDate() {
        return parkDate;
    }

    /**
     * Sets park date.
     *
     * @param parkDate the park date
     */
    public void setParkDate(String parkDate) {
        this.parkDate = parkDate;
    }

    /**
     * Gets begin hour.
     *
     * @return the begin hour
     */
    public String getBeginHour() {
        return beginHour;
    }

    /**
     * Sets begin hour.
     *
     * @param beginHour the begin hour
     */
    public void setBeginHour(String beginHour) {
        this.beginHour = beginHour;
    }

    /**
     * Gets end hour.
     *
     * @return the end hour
     */
    public String getEndHour() {
        return endHour;
    }

    /**
     * Sets end hour.
     *
     * @param endHour the end hour
     */
    public void setEndHour(String endHour) {
        this.endHour = endHour;
    }

    /**
     * Gets hourly rate.
     *
     * @return the hourly rate
     */
    public double getHourlyRate() {
        return hourlyRate;
    }

    /**
     * Sets hourly rate.
     *
     * @param hourlyRate the hourly rate
     */
    public void setHourlyRate(double hourlyRate) {
        this.hourlyRate = hourlyRate;
    }


    /**
     * Gets seller id.
     *
     * @return the seller id
     */
    public String getSellerId() {
        return sellerId;
    }

    /**
     * Sets seller id.
     *
     * @param sellerId the seller id
     */
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }

    /**
     * Gets park address.
     *
     * @return the park address
     */
    public String getParkAddress() {
        return parkAddress;
    }

    /**
     * Sets park address.
     *
     * @param parkAddress the park address
     */
    public void setParkAddress(String parkAddress) {
        this.parkAddress = parkAddress;
    }

    /**
     * Is complete boolean.
     *
     * @return the boolean
     */
    public boolean isComplete() {
        return isComplete;
    }

    /**
     * Sets complete.
     *
     * @param complete the complete
     */
    public void setComplete(boolean complete) {
        isComplete = complete;
    }

    /**
     * Is canceled boolean.
     *
     * @return the boolean
     */
    public boolean isCanceled() {
        return isCanceled;
    }

    /**
     * Sets canceled.
     *
     * @param canceled the canceled
     */
    public void setCanceled(boolean canceled) {
        isCanceled = canceled;
    }

    /**
     * @return The Method returns true if the current date and time is between the begin and
     * end hours of the parkDate. false otherwise.
     */
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
            return false;
        }
    }

    /**
     * The Method calculates the finalPrice for an order using its hourly rate and the time range
     * between the begin and end hours.
     * @return: The Method returns the final price to be paid.
     */
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
