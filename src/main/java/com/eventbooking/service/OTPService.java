package com.eventbooking.service;

public interface OTPService {
    void generateAndSendOTP(String identifier); // email or mobile
    boolean verifyOTP(String identifier, String otp);
}
