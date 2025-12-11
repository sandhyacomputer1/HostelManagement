package com.sandhyasofttech.hostelmanagement.Models;

public class StudentModel {

    private String id;
    private String name;
    private String phone;
    private String parentName;
    private String parentPhone;
    private String address;
    private String room;
    private String studentClass;
    private String joiningDate;

    private int annualFee;
    private int paidFee;
    private int remainingFee;

    private boolean active;

    private String photoUrl;
    private String aadhaarPhotoUrl;
    private String panPhotoUrl;

    // Empty constructor required for Firebase
    public StudentModel() {
    }

    // Getters & Setters  (all)

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getParentPhone() {
        return parentPhone;
    }

    public void setParentPhone(String parentPhone) {
        this.parentPhone = parentPhone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRoom() {
        return room;
    }

    public void setRoom(String room) {
        this.room = room;
    }

    public String getStudentClass() {
        return studentClass;
    }

    public void setStudentClass(String studentClass) {
        this.studentClass = studentClass;
    }

    public String getJoiningDate() {
        return joiningDate;
    }

    public void setJoiningDate(String joiningDate) {
        this.joiningDate = joiningDate;
    }

    public int getAnnualFee() {
        return annualFee;
    }

    public void setAnnualFee(int annualFee) {
        this.annualFee = annualFee;
    }

    public int getPaidFee() {
        return paidFee;
    }

    public void setPaidFee(int paidFee) {
        this.paidFee = paidFee;
    }

    public int getRemainingFee() {
        return remainingFee;
    }

    public void setRemainingFee(int remainingFee) {
        this.remainingFee = remainingFee;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getAadhaarPhotoUrl() {
        return aadhaarPhotoUrl;
    }

    public void setAadhaarPhotoUrl(String aadhaarPhotoUrl) {
        this.aadhaarPhotoUrl = aadhaarPhotoUrl;
    }

    public String getPanPhotoUrl() {
        return panPhotoUrl;
    }

    public void setPanPhotoUrl(String panPhotoUrl) {
        this.panPhotoUrl = panPhotoUrl;
    }
}
