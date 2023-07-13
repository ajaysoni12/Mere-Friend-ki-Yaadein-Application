package com.example.merefriendskiyaden;

public class GroupMember {
    private String name;
    private String email;
    private String mobileNo;
    private String id;

    public GroupMember(String name, String email, String mobileNo, String id) {
        this.name = name;
        this.email = email;
        this.mobileNo = mobileNo;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
