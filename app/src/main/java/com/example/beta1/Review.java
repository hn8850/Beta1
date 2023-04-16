package com.example.beta1;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 18/3/2023
 * The Review Object Class.
 */
public class Review {

    /**
     * Integer count ranging from 1 (the lowest score) to 5 (the highest score).
     * Used to appraise the experience a user has with another user's ParkAd space.
     */
    int stars;

    /**
     * Details the reviewer can provide about their review.
     */
    String message;

    /**
     * The Reviewer user name.
     */
    String reviewerUserName;


    /**
     * Empty Constructor required for reading from FireBase Database.
     */
    public Review() {

    }

    /**
     * General complete Constructor for the Review Object.
     *
     * @param stars            the stars
     * @param message          the message
     * @param reviewerUserName the reviewer user name
     */
    public Review(int stars, String message, String reviewerUserName) {
        this.stars = stars;
        this.message = message;
        this.reviewerUserName = reviewerUserName;
    }

    /**
     * Gets stars.
     *
     * @return the stars
     */
    public int getStars() {
        return stars;
    }

    /**
     * Sets stars.
     *
     * @param stars the stars
     */
    public void setStars(int stars) {
        this.stars = stars;
    }

    /**
     * Gets message.
     *
     * @return the message
     */
    public String getMessage() {
        return message;
    }

    /**
     * Sets message.
     *
     * @param message the message
     */
    public void setMessage(String message) {
        this.message = message;
    }

    /**
     * Gets reviewer user name.
     *
     * @return the reviewer user name
     */
    public String getReviewerUserName() {
        return reviewerUserName;
    }

    /**
     * Sets reviewer user name.
     *
     * @param reviewerUserName the reviewer user name
     */
    public void setReviewerUserName(String reviewerUserName) {
        this.reviewerUserName = reviewerUserName;
    }
}
