package com.example.beta1;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.6
 * @since 27 /12/2023 The User Object Class.
 */
public class User {

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
     * Empty Constructor required for reading from FireBase Database.
     */
    public User() {
    }

    /**
     * General complete Constructor for the User Object.
     *
     * @param name          the name
     * @param dateOfBirth   the date of birth
     * @param phoneNumber   the phone number
     * @param profilePicURL the profile pic url
     */
    public User(String name, String dateOfBirth, String phoneNumber, String profilePicURL) {
        Name = name;
        DateOfBirth = dateOfBirth;
        PhoneNumber = phoneNumber;
        ProfilePicURL = profilePicURL;
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


}
