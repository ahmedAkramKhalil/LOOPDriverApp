package com.model.deliverAll;

import java.io.Serializable;

/**
 * Created by Esite on 21-05-2018.
 */

public class orderItemDetailDataModel implements Serializable {
    String iOrderDetailId;
    String itemName;
    String subItemName;
    String itemQuantity;
    String itemPrice;
    String eAvailable;

    public String getvImage() {
        return vImage;
    }

    public void setvImage(String vImage) {
        this.vImage = vImage;
    }

    String vImage;

    public String getTotalDiscountPrice() {
        return TotalDiscountPrice;
    }

    public void setTotalDiscountPrice(String totalDiscountPrice) {
        TotalDiscountPrice = totalDiscountPrice;
    }

    String TotalDiscountPrice;


    public String geteAvailable() {
        return eAvailable;
    }

    public void seteAvailable(String eAvailable) {
        this.eAvailable = eAvailable;
    }


    public String getSubItemName() {
        return subItemName;
    }

    public void setSubItemName(String subItemName) {
        this.subItemName = subItemName;
    }

    public String getiOrderDetailId() {

        return iOrderDetailId;
    }

    public void setiOrderDetailId(String iOrderDetailId) {
        this.iOrderDetailId = iOrderDetailId;
    }

    public String getItemTotalPrice() {
        return itemTotalPrice;
    }

    public void setItemTotalPrice(String itemTotalPrice) {
        this.itemTotalPrice = itemTotalPrice;
    }

    String itemTotalPrice;
    String itemAvability;

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public String getItemQuantity() {
        return itemQuantity;
    }

    public void setItemQuantity(String itemQuantity) {
        this.itemQuantity = itemQuantity;
    }

    public String getItemPrice() {
        return itemPrice;
    }

    public void setItemPrice(String itemPrice) {
        this.itemPrice = itemPrice;
    }

    public String getItemAvability() {
        return itemAvability;
    }

    public void setItemAvability(String itemAvability) {
        this.itemAvability = itemAvability;
    }
}
