package com.example.merefriendskiyaden;

public class AdminDetails {
    String name;
    String emailId;
    String password;
    String mobNo;

    public AdminDetails(String name, String emailId, String password, String mobNo) {
        this.name = name;
        this.emailId = emailId;
        this.password = password;
        this.mobNo = mobNo;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getMobNo() {
        return mobNo;
    }

    public void setMobNo(String mobNo) {
        this.mobNo = mobNo;
    }
}
