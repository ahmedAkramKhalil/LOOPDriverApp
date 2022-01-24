package com.model.deliverAll;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by Esite on 21-05-2018.
 */

public class orderDetailDataModel implements Serializable {

    ArrayList<orderItemDetailDataModel> orderItemDetailList = new ArrayList<orderItemDetailDataModel>();
    String orderID = "";
    String vOrderNo = "";
    String resturantPayAmount = "";
    String userPayAmount = "";
    String totalAmount = "";
    String totalAmountWithSymbol = "";
    String userPhone = "";
    String userAddress = "";
    String userLatitude = "";
    String userLongitude = "";
    String userName = "";
    String userDistance = "";


    String ePaid = "";
    String eConfirm = "";
    String ePaymentOption = "";
    String orderDate_Time = "";
    String totalItems = "";
    String currencySymbol = "";
    String restaurantName = "";
    String restaurantAddress = "";
    String restaurantId = "";
    String restaurantImage = "";


    String restaurantNumber = "";
    String restaurantLattitude = "";
    String orderState = "";
    String isPhotoUploaded = "";
    String vVehicleType = "";

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public String geteConfirm() {
        return eConfirm;
    }

    public void seteConfirm(String eConfirm) {
        this.eConfirm = eConfirm;
    }

    public String getvVehicleType() {
        return vVehicleType;
    }

    public void setvVehicleType(String vVehicleType) {
        this.vVehicleType = vVehicleType;
    }

    public String getCurrencySymbol() {
        return currencySymbol;
    }

    public void setCurrencySymbol(String currencySymbol) {
        this.currencySymbol = currencySymbol;
    }

    public String getTotalAmountWithSymbol() {
        return totalAmountWithSymbol;
    }

    public void setTotalAmountWithSymbol(String totalAmountWithSymbol) {
        this.totalAmountWithSymbol = totalAmountWithSymbol;
    }

    public String getUserPhone() {
        return userPhone;
    }

    public void setUserPhone(String userPhone) {
        this.userPhone = userPhone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserDistance() {
        return userDistance;
    }

    public void setUserDistance(String userDistance) {
        this.userDistance = userDistance;
    }

    public String getePaid() {
        return ePaid;
    }

    public void setePaid(String ePaid) {
        this.ePaid = ePaid;
    }

    public String getePaymentOption() {
        return ePaymentOption;
    }

    public void setePaymentOption(String ePaymentOption) {
        this.ePaymentOption = ePaymentOption;
    }

    public String getvOrderNo() {
        return vOrderNo;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserLatitude() {
        return userLatitude;
    }

    public void setUserLatitude(String userLatitude) {
        this.userLatitude = userLatitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public void setvOrderNo(String vOrderNo) {
        this.vOrderNo = vOrderNo;
    }

    public String getIsPhotoUploaded() {
        return isPhotoUploaded;
    }

    public void setIsPhotoUploaded(String isPhotoUploaded) {
        this.isPhotoUploaded = isPhotoUploaded;
    }


    public String getRestaurantLattitude() {
        return restaurantLattitude;
    }

    public void setRestaurantLattitude(String restaurantLattitude) {
        this.restaurantLattitude = restaurantLattitude;
    }

    public String getRestaurantLongitude() {
        return restaurantLongitude;
    }

    public void setRestaurantLongitude(String restaurantLongitude) {
        this.restaurantLongitude = restaurantLongitude;
    }

    String restaurantLongitude;

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }


    public String getRestaurantNumber() {
        return restaurantNumber;
    }

    public void setRestaurantNumber(String restaurantNumber) {
        this.restaurantNumber = restaurantNumber;
    }


    public String getResturantPayAmount() {
        return resturantPayAmount;
    }

    public void setResturantPayAmount(String resturantPayAmount) {
        this.resturantPayAmount = resturantPayAmount;
    }

    public String getUserPayAmount() {
        return userPayAmount;
    }

    public void setUserPayAmount(String userPayAmount) {
        this.userPayAmount = userPayAmount;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }


    public ArrayList<orderItemDetailDataModel> getorderItemDetailList() {
        return orderItemDetailList;
    }

    public void setorderItemDetailList(ArrayList<orderItemDetailDataModel> orderItemDetailDataModelArrayList) {
        this.orderItemDetailList = orderItemDetailDataModelArrayList;
    }

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }


    public String getOrderDate_Time() {
        return orderDate_Time;
    }

    public void setOrderDate_Time(String oredrDate_Time) {
        this.orderDate_Time = oredrDate_Time;
    }

    public String getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(String totalItems) {
        this.totalItems = totalItems;
    }

    public String getOrderState() {
        return orderState;
    }

    public void setOrderState(String orderState) {
        this.orderState = orderState;
    }
}
