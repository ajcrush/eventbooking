package com.eventbooking.dto;

public class OTPVerifyDTO {

    private String emailOrMobile;
    private String otp;

    public OTPVerifyDTO() {
    }

    public OTPVerifyDTO(String emailOrMobile, String otp) {
        this.emailOrMobile = emailOrMobile;
        this.otp = otp;
    }

    public String getEmailOrMobile() {
        return emailOrMobile;
    }

    public void setEmailOrMobile(String emailOrMobile) {
        this.emailOrMobile = emailOrMobile;
    }

    public String getOtp() {
        return otp;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }
}
