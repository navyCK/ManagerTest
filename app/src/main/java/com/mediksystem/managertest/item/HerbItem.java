package com.mediksystem.managertest.item;

public class HerbItem {

    // 이미지 URL
    String image;
    String barcode;
    String name;
    String company;
    // 원산지
    String county_of_origin;
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

    public void setCounty_of_origin(String county_of_origin) {
        this.county_of_origin = county_of_origin;
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

    public String getCounty_of_origin() {
        return county_of_origin;
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

