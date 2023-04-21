package com.example.beta1;

import java.util.ArrayList;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.6
 * @since 27 /12/2023 The User Object Class.
 */
public class User {


    /**
     * Integer that is reflective of the User's active status. 1 if active. 0 if not.
     */
    int active;

    /**
     * The User's actual name.
     */
    String Name;

    /**
     * The User's date of birth.
     */
    String DateOfBirth;

    /**
     * The User's phone number.
     */
    String PhoneNumber;

    /**
     * The URL for the User's profile pic, which is stored in the database.
     */
    String ProfilePicURL;

    /**
     * The Orders the User has made.
     */
    ArrayList<Order> orders;

    /**
     * The Park ads the User has posted.
     */
    ArrayList<ParkAd> parkAds;

    /**
     * The Receipts the User has collected.
     */
    ArrayList<Receipt> receipts;

    /**
     * The Reviews that have been made about the User.
     */
    ArrayList<Review> reviews;

    /**
     * Empty Constructor required for reading from FireBase Database.
     */
    public User() {
    }

    /**
     * General complete Constructor for the User Object.
     * @param active        the active
     * @param name          the name
     * @param dateOfBirth   the date of birth
     * @param phoneNumber   the phone number
     * @param profilePicURL the profile pic url
     */
    public User(int active, String name, String dateOfBirth, String phoneNumber, String profilePicURL) {
        this.active = active;
        Name = name;
        DateOfBirth = dateOfBirth;
        PhoneNumber = phoneNumber;
        ProfilePicURL = profilePicURL;
        this.orders = new ArrayList<>();
        this.parkAds = new ArrayList<>();
        this.receipts = new ArrayList<>();
        this.reviews = new ArrayList<>();

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
     * Gets name.
     *
     * @return the name
     */
    public String getName() {
        return Name;
    }

    /**
     * Sets name.
     *
     * @param name the name
     */
    public void setName(String name) {
        Name = name;
    }

    /**
     * Gets date of birth.
     *
     * @return the date of birth
     */
    public String getDateOfBirth() {
        return DateOfBirth;
    }

    /**
     * Sets date of birth.
     *
     * @param dateOfBirth the date of birth
     */
    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    /**
     * Gets phone number.
     *
     * @return the phone number
     */
    public String getPhoneNumber() {
        return PhoneNumber;
    }

    /**
     * Sets phone number.
     *
     * @param phoneNumber the phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    /**
     * Gets profile pic url.
     *
     * @return the profile pic url
     */
    public String getProfilePicURL() {
        return ProfilePicURL;
    }

    /**
     * Sets profile pic url.
     *
     * @param profilePicURL the profile pic url
     */
    public void setProfilePicURL(String profilePicURL) {
        ProfilePicURL = profilePicURL;
    }

    /**
     * Gets orders.
     *
     * @return the orders
     */
    public ArrayList<Order> getOrders() {
        return orders;
    }

    /**
     * Sets orders.
     *
     * @param orders the orders
     */
    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    /**
     * Gets park ads.
     *
     * @return the park ads
     */
    public ArrayList<ParkAd> getParkAds() {
        return parkAds;
    }

    /**
     * Sets park ads.
     *
     * @param parkAds the park ads
     */
    public void setParkAds(ArrayList<ParkAd> parkAds) {
        this.parkAds = parkAds;
    }

    /**
     * Gets receipts.
     *
     * @return the receipts
     */
    public ArrayList<Receipt> getReceipts() {
        return receipts;
    }

    /**
     * Sets receipts.
     *
     * @param receipts the receipts
     */
    public void setReceipts(ArrayList<Receipt> receipts) {
        this.receipts = receipts;
    }

    /**
     * Gets reviews.
     *
     * @return the reviews
     */
    public ArrayList<Review> getReviews() {
        return reviews;
    }

    /**
     * Sets reviews.
     *
     * @param reviews the reviews
     */
    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }
}
