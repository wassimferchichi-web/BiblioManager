package com.bibliomanager.models;

import java.time.LocalDate;

public class Member {

    private int id;
    private String fullName;
    private String email;
    private String phone;
    private LocalDate memberSince;

    public Member(int id, String fullName, String email, String phone, LocalDate memberSince) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.memberSince = memberSince;
    }

    public int getId()                  { return id; }
    public String getFullName()         { return fullName; }
    public String getEmail()            { return email; }
    public String getPhone()            { return phone; }
    public LocalDate getMemberSince()   { return memberSince; }

    public void setId(int id)                        { this.id = id; }
    public void setFullName(String fullName)         { this.fullName = fullName; }
    public void setEmail(String email)               { this.email = email; }
    public void setPhone(String phone)               { this.phone = phone; }
    public void setMemberSince(LocalDate memberSince){ this.memberSince = memberSince; }
}