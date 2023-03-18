package com.example.beta1;

public class Review {
    int stars;
    String message;
    String reviewerUserName;

    public Review (){

    }

    public Review(int stars, String message, String reviewerUserName) {
        this.stars = stars;
        this.message = message;
        this.reviewerUserName = reviewerUserName;
    }

    public int getStars() {
        return stars;
    }

    public void setStars(int stars) {
        this.stars = stars;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getReviewerUserName() {
        return reviewerUserName;
    }

    public void setReviewerUserName(String reviewerUserName) {
        this.reviewerUserName = reviewerUserName;
    }
}
