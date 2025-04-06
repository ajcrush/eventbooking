package com.eventbooking.dto;

public class SignupRequestDTO {

    private String name;
    private String email;
    private String password;
    private String mobileNumber;
    private String role; // Assuming this is a string like "USER" or "ADMIN"

    public SignupRequestDTO() {
    }

    public SignupRequestDTO(String name, String email, String password, String mobileNumber, String role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.mobileNumber = mobileNumber;
        this.role = role;
    }

    // Getters
    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public String getRole() {
        return role;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
