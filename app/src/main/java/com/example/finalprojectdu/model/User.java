package com.example.finalprojectdu.model;

import java.io.Serializable;

public class User implements Serializable {
    String fullName, imageUpload, phone, uid;

    public User() {
    }

    public User(String fullName, String imageUpload, String phone, String uid) {
        this.fullName = fullName;
        this.imageUpload = imageUpload;
        this.phone = phone;
        this.uid = uid;
    }


}
