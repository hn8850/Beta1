package com.example.beta1;

import java.util.ArrayList;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.4
 * @since 24/12/2022
 * The ParkAd Object Class.
 */

public class ParkAd {

    /**
     * The latitude of the parkAd's location.
     */
    String latitude;

    /**
     * The longitude of the parkAd's location.
     */
    String longitude;

    /**
     * The database KeyID for the user who published the ParkAd.
     */
    String userID;

    /**
     * Integer that is reflective of the ParkAd's active status. 1 if active. 0 if not.
     */
    int active;

    /**
     * The date that the ParkAd's space will be available at.
     */
    String Date;

    /**
     * The hour at which the ParkAd's space becomes available to rent.(in accordance to the Date str)
     */
    String BeginHour;

    /**
     * The hour at which the ParkAd's space becomes unavailable to rent.(in accordance to the Date str)
     */
    String FinishHour;

    /**
     * The price per hour of renting the ParkAd Space
     */
    Double HourlyRate;

    /**
     * An ArrayList containing URL's to images describing the ParkAd, stored in the database.
     */
    ArrayList<String> PictureUrl;

    /**
     * Text description for the ParkAd.
     */
    String Description;

    /**
     * The address at which the ParkAd is located.
     */
    String Address;


    /**
     * Empty Constructor required for reading from FireBase Database.
     */
    public ParkAd() {

    }

    /**
     * General complete Constructor for the ParkAd Object.
     *
     * @param latitude    the latitude
     * @param longitude   the longitude
     * @param userID      the user id
     * @param active      the active
     * @param date        the date
     * @param beginHour   the begin hour
     * @param finishHour  the finish hour
     * @param hourlyRate  the hourly rate
     * @param pictureUrl  the picture url
     * @param description the description
     * @param address     the address
     */
    public ParkAd(String latitude, String longitude, String userID, int active, String date, String beginHour, String finishHour, Double hourlyRate, ArrayList<String> pictureUrl, String description, String address) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.userID = userID;
        this.active = active;
        Date = date;
        BeginHour = beginHour;
        FinishHour = finishHour;
        HourlyRate = hourlyRate;
        this.PictureUrl = pictureUrl;
        Description = description;
        Address = address;
    }

    /**
     * Gets latitude.
     *
     * @return the latitude
     */
    public String getLatitude() {
        return latitude;
    }

    /**
     * Sets latitude.
     *
     * @param latitude the latitude
     */
    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    /**
     * Gets longitude.
     *
     * @return the longitude
     */
    public String getLongitude() {
        return longitude;
    }

    /**
     * Sets longitude.
     *
     * @param longitude the longitude
     */
    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    /**
     * Gets address.
     *
     * @return the address
     */
    public String getAddress() {
        return Address;
    }

    /**
     * Sets address.
     *
     * @param address the address
     */
    public void setAddress(String address) {
        Address = address;
    }

    /**
     * Gets user id.
     *
     * @return the user id
     */
    public String getUserID() {
        return userID;
    }

    /**
     * Sets user id.
     *
     * @param userID the user id
     */
    public void setUserID(String userID) {
        this.userID = userID;
    }

    /**
     * Gets active.
     *
     * @return the active
     */
    public int getActive() {
        return active;
    }

    /**
     * Sets active.
     *
     * @param active the active
     */
    public void setActive(int active) {
        this.active = active;
    }

    /**
     * Gets date.
     *
     * @return the date
     */
    public String getDate() {
        return Date;
    }

    /**
     * Sets date.
     *
     * @param date the date
     */
    public void setDate(String date) {
        Date = date;
    }

    /**
     * Gets begin hour.
     *
     * @return the begin hour
     */
    public String getBeginHour() {
        return BeginHour;
    }

    /**
     * Sets begin hour.
     *
     * @param beginHour the begin hour
     */
    public void setBeginHour(String beginHour) {
        BeginHour = beginHour;
    }

    /**
     * Gets finish hour.
     *
     * @return the finish hour
     */
    public String getFinishHour() {
        return FinishHour;
    }

    /**
     * Sets finish hour.
     *
     * @param finishHour the finish hour
     */
    public void setFinishHour(String finishHour) {
        FinishHour = finishHour;
    }

    /**
     * Gets hourly rate.
     *
     * @return the hourly rate
     */
    public Double getHourlyRate() {
        return HourlyRate;
    }

    /**
     * Sets hourly rate.
     *
     * @param hourlyRate the hourly rate
     */
    public void setHourlyRate(Double hourlyRate) {
        HourlyRate = hourlyRate;
    }

    /**
     * Gets picture url.
     *
     * @return the picture url
     */
    public ArrayList<String> getPictureUrl() {
        return PictureUrl;
    }

    /**
     * Sets picture url.
     *
     * @param pictureUrl the picture url
     */
    public void setPictureUrl(ArrayList<String> pictureUrl) {
        PictureUrl = pictureUrl;
    }

    /**
     * Gets description.
     *
     * @return the description
     */
    public String getDescription() {
        return Description;
    }

    /**
     * Sets description.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        Description = description;
    }


}
