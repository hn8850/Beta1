package com.example.beta1;

import java.util.ArrayList;

public class User {

    String userName;
    int Active;
    String Name;
    String DateOfBirth;
    String PhoneNumber;
    String ProfilePicURL;
    ArrayList<Order> orders;
    ArrayList<ParkAd> parkAds;
    ArrayList<Receipt> receipts;
    ArrayList<Review> reviews;

    public User() {
    }

    public User(String userName, int active, String name, String dateOfBirth, String phoneNumber, String profilePicURL) {
        this.userName = userName;
        Active = active;
        Name = name;
        DateOfBirth = dateOfBirth;
        PhoneNumber = phoneNumber;
        ProfilePicURL = profilePicURL;
        this.orders = new ArrayList<>();
        this.parkAds = new ArrayList<>();
        this.receipts = new ArrayList<>();
        this.reviews = new ArrayList<>();

    }




    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public int getActive() {
        return Active;
    }

    public void setActive(int active) {
        Active = active;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getDateOfBirth() {
        return DateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth) {
        DateOfBirth = dateOfBirth;
    }

    public String getPhoneNumber() {
        return PhoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        PhoneNumber = phoneNumber;
    }

    public String getProfilePicURL() {
        return ProfilePicURL;
    }

    public void setProfilePicURL(String profilePicURL) {
        ProfilePicURL = profilePicURL;
    }

    public ArrayList<Order> getOrders() {
        return orders;
    }

    public void setOrders(ArrayList<Order> orders) {
        this.orders = orders;
    }

    public ArrayList<ParkAd> getParkAds() {
        return parkAds;
    }

    public void setParkAds(ArrayList<ParkAd> parkAds) {
        this.parkAds = parkAds;
    }

    public ArrayList<Receipt> getReceipts() {
        return receipts;
    }

    public void setReceipts(ArrayList<Receipt> receipts) {
        this.receipts = receipts;
    }

    public ArrayList<Review> getReviews() {
        return reviews;
    }

    public void setReviews(ArrayList<Review> reviews) {
        this.reviews = reviews;
    }
}
