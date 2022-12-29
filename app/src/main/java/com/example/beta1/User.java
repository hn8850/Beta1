package com.example.beta1;

public class User {

    String userName;
    int Active;
    String Name;
    String DateOfBirth;
    String PhoneNumber;
    String ProfilePicURL;

    public User() {
    }

    public User(String userName, int active, String name, String dateOfBirth, String phoneNumber, String profilePicURL) {
        this.userName = userName;
        Active = active;
        Name = name;
        DateOfBirth = dateOfBirth;
        PhoneNumber = phoneNumber;
        ProfilePicURL = profilePicURL;
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
    //    Reviews ReviewsFromUser
//    Reviews ReviewsAboutUser
//    Order[] OrdersHistory
//    Order[] RentalHistory


}
