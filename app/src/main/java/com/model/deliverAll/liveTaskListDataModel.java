package com.model.deliverAll;

import java.io.Serializable;

/**
 * Created by Esite on 17-05-2018.
 */

public class liveTaskListDataModel implements Serializable {

    String pickedFromRes = "";
    String orderNumber = "";
    String isPhotoUploaded = "";
    String isRestaurant = "";

    /*Restaurant Detail*/

    String restaurantAddress = "";
    String restaurantName = "";
    String restaurantLattitude = "";
    String restaurantLongitude = "";
    String restaurantNumber = "";
    String restaurantId = "";
    String restaurantImage = "";

    /*Deliver Order Detail*/

    String userName = "";
    String userAddress = "";
    String userNumber = "";
    String userLattitude = "";
    String userLongitude = "";

    public String getvVehicleType() {
        return vVehicleType;
    }

    public void setvVehicleType(String vVehicleType) {
        this.vVehicleType = vVehicleType;
    }

    String vVehicleType = "";

    /*Lables Txt*/

    String LBL_CALL_TXT, LBL_NAVIGATE, LBL_PICKUP, LBL_DELIVER, LBL_CURRENT_TASK_TXT, LBL_NEXT_TASK_TXT;

    public String getIsPhotoUploaded() {
        return isPhotoUploaded;
    }

    public void setIsPhotoUploaded(String isPhotoUploaded) {
        this.isPhotoUploaded = isPhotoUploaded;
    }

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

    public String isRestaurant() {
        return isRestaurant;
    }

    public void setIsRestaurant(String isRestaurant) {
        this.isRestaurant = isRestaurant;
    }

    public String getPickedFromRes() {
        return pickedFromRes;
    }

    public void setPickedFromRes(String orderStatus) {
        this.pickedFromRes = orderStatus;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }

    public String getRestaurantAddress() {
        return restaurantAddress;
    }

    public void setRestaurantAddress(String restaurantAddress) {
        this.restaurantAddress = restaurantAddress;
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

    public String getRestaurantNumber() {
        return restaurantNumber;
    }

    public void setRestaurantNumber(String restaurantNumber) {
        this.restaurantNumber = restaurantNumber;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserAddress() {
        return userAddress;
    }

    public void setUserAddress(String userAddress) {
        this.userAddress = userAddress;
    }

    public String getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(String userNumber) {
        this.userNumber = userNumber;
    }

    public String getUserLattitude() {
        return userLattitude;
    }

    public void setUserLattitude(String userLattitude) {
        this.userLattitude = userLattitude;
    }

    public String getUserLongitude() {
        return userLongitude;
    }

    public void setUserLongitude(String userLongitude) {
        this.userLongitude = userLongitude;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        this.restaurantName = restaurantName;
    }

    public String getLBL_CALL_TXT() {
        return LBL_CALL_TXT;
    }

    public void setLBL_CALL_TXT(String LBL_CALL_TXT) {
        this.LBL_CALL_TXT = LBL_CALL_TXT;
    }

    public String getLBL_NAVIGATE() {
        return LBL_NAVIGATE;
    }

    public void setLBL_NAVIGATE(String LBL_NAVIGATE) {
        this.LBL_NAVIGATE = LBL_NAVIGATE;
    }

    public String getLBL_PICKUP() {
        return LBL_PICKUP;
    }

    public void setLBL_PICKUP(String LBL_PICKUP) {
        this.LBL_PICKUP = LBL_PICKUP;
    }

    public String getLBL_DELIVER() {
        return LBL_DELIVER;
    }

    public String getLBL_CURRENT_TASK_TXT() {
        return LBL_CURRENT_TASK_TXT;
    }

    public void setLBL_CURRENT_TASK_TXT(String LBL_CURRENT_TASK_TXT) {
        this.LBL_CURRENT_TASK_TXT = LBL_CURRENT_TASK_TXT;
    }

    public String getLBL_NEXT_TASK_TXT() {
        return LBL_NEXT_TASK_TXT;
    }

    public void setLBL_NEXT_TASK_TXT(String LBL_NEXT_TASK_TXT) {
        this.LBL_NEXT_TASK_TXT = LBL_NEXT_TASK_TXT;
    }

    public void setLBL_DELIVER(String LBL_DELIVER) {
        this.LBL_DELIVER = LBL_DELIVER;
    }

}
