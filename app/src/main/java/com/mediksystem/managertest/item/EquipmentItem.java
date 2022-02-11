package com.mediksystem.managertest.item;

public class EquipmentItem {
    private String userId;
    private String id;
    private String title;
    private String body;


    public EquipmentItem() {
        this.userId = userId;
        this.id = id;
        this.title = title;
        this.body = body;
    }

    public void setUserId(String userId) {
        this.userId = userId ;
    }
    public void setId(String id) {
        this.id = id ;
    }
    public void setTitle(String title) {
        this.title = title ;
    }
    public void setBody(String body) {
        this.body = body ;
    }

    public String getUserId() {
        return userId ;
    }
    public String getId() {
        return id ;
    }
    public String getTitle() {
        return title ;
    }
    public String getBody() {
        return body ;
    }
}
