package com.eventbooking.dto;

public class LoginRequestDTO {
    private String emailOrMobile;
    private String password;

    // Getter
    public String getEmailOrMobile() {
        return emailOrMobile;
    }

    // Setter
    public void setEmailOrMobile(String emailOrMobile) {
        this.emailOrMobile = emailOrMobile;
    }

    // Getter
    public String getPassword() {
        return password;
    }

    // Setter
    public void setPassword(String password) {
        this.password = password;
    }
}
