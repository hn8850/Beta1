package com.example.beta1;

public class Receipt {
    String sellerUserID;
    String buyerUserID;
    String parkAdLocationKey;
    String parkAdDateKey;
    String parkAdHourRangeKey;
    double finalPrice;
    String dateOfConfirm;

    public Receipt(String sellerUserID, String buyerUserID, String parkAdLocationKey, String parkAdDateKey, String parkAdHourRangeKey, double finalPrice, String dateOfConfirm) {
        this.sellerUserID = sellerUserID;
        this.buyerUserID = buyerUserID;
        this.parkAdLocationKey = parkAdLocationKey;
        this.parkAdDateKey = parkAdDateKey;
        this.parkAdHourRangeKey = parkAdHourRangeKey;
        this.finalPrice = finalPrice;
        this.dateOfConfirm = dateOfConfirm;
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

    public String getParkAdLocationKey() {
        return parkAdLocationKey;
    }

    public void setParkAdLocationKey(String parkAdLocationKey) {
        this.parkAdLocationKey = parkAdLocationKey;
    }

    public String getParkAdDateKey() {
        return parkAdDateKey;
    }

    public void setParkAdDateKey(String parkAdDateKey) {
        this.parkAdDateKey = parkAdDateKey;
    }

    public String getParkAdHourRangeKey() {
        return parkAdHourRangeKey;
    }

    public void setParkAdHourRangeKey(String parkAdHourRangeKey) {
        this.parkAdHourRangeKey = parkAdHourRangeKey;
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

    public String getPathForParkAd(){
        return parkAdLocationKey  + "/" + parkAdDateKey + "/" + parkAdHourRangeKey;
    }


}


