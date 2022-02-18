package com.mediksystem.managertest.item;

public class HerbPackageItem {
    int size;
    int quantity;
    String barcode;

    public void setSize(int size) {
        this.size = size;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public int getSize() {
        return size;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getBarcode() {
        return barcode;
    }
}
