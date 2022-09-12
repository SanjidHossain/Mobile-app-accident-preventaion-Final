package com.example.finalprojectdu.model;

import java.io.Serializable;

public class Report implements Serializable {
    String time, date, roadCondition, name, phone, uid, image;
    double lat, lan;

    public Report() {
    }

    public Report(String time, String date, String roadCondition, String name, String phone, String uid, double lat, double lan, String image) {
        this.time = time;
        this.date = date;
        this.roadCondition = roadCondition;
        this.name = name;
        this.phone = phone;
        this.uid = uid;
        this.lan = lan;
        this.lat = lat;
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLan() {
        return lan;
    }

    public void setLan(double lan) {
        this.lan = lan;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getRoadCondition() {
        return roadCondition;
    }

    public void setRoadCondition(String roadCondition) {
        this.roadCondition = roadCondition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
}
