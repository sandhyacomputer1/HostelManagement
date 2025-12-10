package com.sandhyasofttech.hostelmanagement.Models;

public class StudentModel {

    private String id;
    private String name;
    private String phone;
    private String room;
    private String joiningDate;
    private String photoUrl;
    private boolean active;

    private String address;
    private int annualFee;
    private int paidFee;
    private int remainingFee;
    private String parentName;
    private String parentPhone;

    // NEW FIELDS MUST MATCH FIREBASE KEYS
    private String studentClass;
    private String panNumber;
    private String aadhaarNumber;
    private String panPhotoUrl;
    private String aadhaarPhotoUrl;

    public StudentModel() {}

    // GETTERS
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPhone() { return phone; }
    public String getRoom() { return room; }
    public String getJoiningDate() { return joiningDate; }
    public String getPhotoUrl() { return photoUrl; }
    public boolean isActive() { return active; }

    public String getAddress() { return address; }
    public int getAnnualFee() { return annualFee; }
    public int getPaidFee() { return paidFee; }
    public int getRemainingFee() { return remainingFee; }
    public String getParentName() { return parentName; }
    public String getParentPhone() { return parentPhone; }

    public String getStudentClass() { return studentClass; }
    public String getPanNumber() { return panNumber; }
    public String getAadhaarNumber() { return aadhaarNumber; }
    public String getPanPhotoUrl() { return panPhotoUrl; }
    public String getAadhaarPhotoUrl() { return aadhaarPhotoUrl; }

    // SETTERS
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setRoom(String room) { this.room = room; }
    public void setJoiningDate(String joiningDate) { this.joiningDate = joiningDate; }
    public void setPhotoUrl(String photoUrl) { this.photoUrl = photoUrl; }
    public void setActive(boolean active) { this.active = active; }

    public void setAddress(String address) { this.address = address; }
    public void setAnnualFee(int annualFee) { this.annualFee = annualFee; }
    public void setPaidFee(int paidFee) { this.paidFee = paidFee; }
    public void setRemainingFee(int remainingFee) { this.remainingFee = remainingFee; }
    public void setParentName(String parentName) { this.parentName = parentName; }
    public void setParentPhone(String parentPhone) { this.parentPhone = parentPhone; }

    public void setStudentClass(String studentClass) { this.studentClass = studentClass; }
    public void setPanNumber(String panNumber) { this.panNumber = panNumber; }
    public void setAadhaarNumber(String aadhaarNumber) { this.aadhaarNumber = aadhaarNumber; }
    public void setPanPhotoUrl(String panPhotoUrl) { this.panPhotoUrl = panPhotoUrl; }
    public void setAadhaarPhotoUrl(String aadhaarPhotoUrl) { this.aadhaarPhotoUrl = aadhaarPhotoUrl; }
}
