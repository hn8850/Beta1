package com.example.beta1;

/***
 * @author Harel Navon harelnavon2710@gmail.com
 * @version 1.0
 * @since 24/2/2023
 * The Receipt Object Class.
 * A Receipt is created after an Order goes through.
 */
public class Receipt {

    /**
     * The database KeyID for the user who published the ParkAd that is associated with the Order
     * that is associated with the Receipt.
     */
    String sellerUserID;

    /**
     * The database KeyID for the user who made the Order that is associated with the Receipt.
     */
    String buyerUserID;

    /**
     * The database KeyID for the ParkAd the Order is associated with that is associated with
     * the Receipt.
     */
    String parkAdPath;

    /**
     * The database KeyID for the Order the Receipt is associated with.
     */
    String orderKey;

    /**
     * The final price to pay for the Order that is associated with the Receipt.
     */
    double finalPrice;

    /**
     * The date of confirm for the Order that is associated with the Receipt.
     */
    String dateOfConfirm;

    /**
     * The Payment id for the Receipt.
     */
    String paymentID;

    /**
     * Empty Constructor required for reading from FireBase Database.
     */
    public Receipt() {

    }

    /**
     * General complete Constructor for the Receipt Object.
     *
     * @param sellerUserID  the seller user id
     * @param buyerUserID   the buyer user id
     * @param parkAdPath    the park ad path
     * @param orderKey      the order key
     * @param finalPrice    the final price
     * @param dateOfConfirm the date of confirm
     * @param paymentID     the payment id
     */
    public Receipt(String sellerUserID, String buyerUserID, String parkAdPath, String orderKey, double finalPrice, String dateOfConfirm, String paymentID) {
        this.sellerUserID = sellerUserID;
        this.buyerUserID = buyerUserID;
        this.parkAdPath = parkAdPath;
        this.orderKey = orderKey;
        this.finalPrice = finalPrice;
        this.dateOfConfirm = dateOfConfirm;
        this.paymentID = paymentID;
    }

    /**
     * Gets seller user id.
     *
     * @return the seller user id
     */
    public String getSellerUserID() {
        return sellerUserID;
    }

    /**
     * Sets seller user id.
     *
     * @param sellerUserID the seller user id
     */
    public void setSellerUserID(String sellerUserID) {
        this.sellerUserID = sellerUserID;
    }

    /**
     * Gets buyer user id.
     *
     * @return the buyer user id
     */
    public String getBuyerUserID() {
        return buyerUserID;
    }

    /**
     * Sets buyer user id.
     *
     * @param buyerUserID the buyer user id
     */
    public void setBuyerUserID(String buyerUserID) {
        this.buyerUserID = buyerUserID;
    }

    /**
     * Gets park ad path.
     *
     * @return the park ad path
     */
    public String getParkAdPath() {
        return parkAdPath;
    }

    /**
     * Sets park ad path.
     *
     * @param parkAdPath the park ad path
     */
    public void setParkAdPath(String parkAdPath) {
        this.parkAdPath = parkAdPath;
    }

    /**
     * Gets final price.
     *
     * @return the final price
     */
    public double getFinalPrice() {
        return finalPrice;
    }

    /**
     * Sets final price.
     *
     * @param finalPrice the final price
     */
    public void setFinalPrice(double finalPrice) {
        this.finalPrice = finalPrice;
    }

    /**
     * Gets date of confirm.
     *
     * @return the date of confirm
     */
    public String getDateOfConfirm() {
        return dateOfConfirm;
    }

    /**
     * Sets date of confirm.
     *
     * @param dateOfConfirm the date of confirm
     */
    public void setDateOfConfirm(String dateOfConfirm) {
        this.dateOfConfirm = dateOfConfirm;
    }

    /**
     * Gets order key.
     *
     * @return the order key
     */
    public String getOrderKey() {
        return orderKey;
    }

    /**
     * Sets order key.
     *
     * @param orderKey the order key
     */
    public void setOrderKey(String orderKey) {
        this.orderKey = orderKey;
    }

    /**
     * Gets payment id.
     *
     * @return the payment id
     */
    public String getPaymentID() {
        return paymentID;
    }

    /**
     * Sets payment id.
     *
     * @param paymentID the payment id
     */
    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }
}


