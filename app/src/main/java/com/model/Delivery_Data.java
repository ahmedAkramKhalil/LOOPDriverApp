package com.model;

/**
 * Created by Esite on 19-04-2018.
 */

public class Delivery_Data {
    String iDeliveryFieldId = "";
    String vValue = "";
    String vFieldName = "";
    String tSaddress = "";
    String tDaddress = "";
    String ePaymentByReceiver = "";
    String iTripDeliveryLocationId = "";

    public boolean isShowDetails() {
        return showDetails;
    }

    public void setShowDetails(boolean showDetails) {
        this.showDetails = showDetails;
    }

    double tStartLat = 0.0;
    boolean showDetails = true;


    public String getiTripDeliveryLocationId() {
        return iTripDeliveryLocationId;
    }

    public void setiTripDeliveryLocationId(String iTripDeliveryLocationId) {
        this.iTripDeliveryLocationId = iTripDeliveryLocationId;
    }

    public String getiDeliveryFieldId() {
        return iDeliveryFieldId;
    }

    public void setiDeliveryFieldId(String iDeliveryFieldId) {
        this.iDeliveryFieldId = iDeliveryFieldId;
    }

    public String getvValue() {
        return vValue;
    }

    public void setvValue(String vValue) {
        this.vValue = vValue;
    }

    public String getvFieldName() {
        return vFieldName;
    }

    public void setvFieldName(String vFieldName) {
        this.vFieldName = vFieldName;
    }

    public String gettSaddress() {
        return tSaddress;
    }

    public void settSaddress(String tSaddress) {
        this.tSaddress = tSaddress;
    }

    public String gettDaddress() {
        return tDaddress;
    }

    public void settDaddress(String tDaddress) {
        this.tDaddress = tDaddress;
    }

    public String getePaymentByReceiver() {
        return ePaymentByReceiver;
    }

    public void setePaymentByReceiver(String ePaymentByReceiver) {
        this.ePaymentByReceiver = ePaymentByReceiver;
    }

    public double gettStartLat() {
        return tStartLat;
    }

    public void settStartLat(double tStartLat) {
        this.tStartLat = tStartLat;
    }

    public double gettStartLong() {
        return tStartLong;
    }

    public void settStartLong(double tStartLong) {
        this.tStartLong = tStartLong;
    }

    public double gettDestLat() {
        return tDestLat;
    }

    public void settDestLat(double tDestLat) {
        this.tDestLat = tDestLat;
    }

    public double gettDestLong() {
        return tDestLong;
    }

    public void settDestLong(double tDestLong) {
        this.tDestLong = tDestLong;
    }

    double tStartLong = 0.0;
    double tDestLat = 0.0;
    double tDestLong = 0.0;
}
