package com.mediksystem.managertest.item;

import java.util.ArrayList;

public class HerbItem {
    int type;
    // 이미지 URL
    String image;
    String barcode;
    String name;
    String company;
    // 원산지
    String country_of_origin;
    String memo;
    // 보관기한
    int storage_period;
    // 사용기한
    int expiration;
    // 보관위치
    String storage_location;
    // 재고보관총중량
    int total_weight_of_inventory_storage;
    // 구매가격
    double purchase_price;
    // 판매가격
    double sales_price;

    public void setType(int type) {
        this.type = type;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public void setCountry_of_origin(String country_of_origin) {
        this.country_of_origin = country_of_origin;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public void setStorage_period(int storage_period) {
        this.storage_period = storage_period;
    }

    public void setExpiration(int expiration) {
        this.expiration = expiration;
    }

    public void setStorage_location(String storage_location) {
        this.storage_location = storage_location;
    }

    public void setTotal_weight_of_inventory_storage(int total_weight_of_inventory_storage) {
        this.total_weight_of_inventory_storage = total_weight_of_inventory_storage;
    }

    public void setPurchase_price(double purchase_price) {
        this.purchase_price = purchase_price;
    }

    public void setSales_price(double sales_price) {
        this.sales_price = sales_price;
    }


    public int getType() {
        return type;
    }

    public String getImage() {
        return image;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public String getCountry_of_origin() {
        return country_of_origin;
    }

    public String getMemo() {
        return memo;
    }

    public int getStorage_period() {
        return storage_period;
    }

    public int getExpiration() {
        return expiration;
    }

    public String getStorage_location() {
        return storage_location;
    }

    public double getPurchase_price() {
        return purchase_price;
    }

    public double getSales_price() {
        return sales_price;
    }

    public int getTotal_weight_of_inventory_storage() {
        return total_weight_of_inventory_storage;
    }

}

