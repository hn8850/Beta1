package com.example.beta1;

public class Receipt {
    String sellerUserID;
    String buyerUserID;
    String parkAdPath;
    String orderKey;
    double finalPrice;
    String dateOfConfirm;
    String paymentID;

    public Receipt() {

    }

    public Receipt(String sellerUserID, String buyerUserID, String parkAdPath, String orderKey, double finalPrice, String dateOfConfirm, String paymentID) {
        this.sellerUserID = sellerUserID;
        this.buyerUserID = buyerUserID;
        this.parkAdPath = parkAdPath;
        this.orderKey = orderKey;
        this.finalPrice = finalPrice;
        this.dateOfConfirm = dateOfConfirm;
        this.paymentID = paymentID;
    }

    public String getSellerUserID() {
        return sellerUserID;
    }

    public void setSellerUserID(String sellerUserID) {
        this.sellerUserID = sellerUserID;
    }

    public String getBuyerUserID() {
        return buyerUserID;
    }

    public void setBuyerUserID(String buyerUserID) {
        this.buyerUserID = buyerUserID;
    }

    public String getParkAdPath() {
        return parkAdPath;
    }

    public void setParkAdPath(String parkAdPath) {
        this.parkAdPath = parkAdPath;
    }

    public double getFinalPrice() {
        return finalPrice;
    }

    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    public String getDateOfConfirm() {
        return dateOfConfirm;
    }

    public void setDateOfConfirm(String dateOfConfirm) {
        this.dateOfConfirm = dateOfConfirm;
    }

    public String getOrderKey() {
        return orderKey;
    }

    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }
}


