package com.eventbooking.service.impl;

import com.eventbooking.service.OTPService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


@Service
public class OTPServiceImpl implements OTPService {

    private final Map<String, OTPDetails> otpStorage = new HashMap<>();

    @Autowired
    private JavaMailSender mailSender;

    @Override
    public void generateAndSendOTP(String identifier) {
        String otp = String.format("%06d", new Random().nextInt(999999));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(5);

        otpStorage.put(identifier, new OTPDetails(otp, expiry));

        // Send email
        sendOtpEmail(identifier, otp);  // identifier = user email in this context
    }

    private void sendOtpEmail(String to, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code");
        message.setText("Your OTP is: " + otp + "\nIt is valid for 5 minutes.");
        message.setFrom("your_email@gmail.com"); // Optional

        mailSender.send(message);
    }

    @Override
    public boolean verifyOTP(String identifier, String otp) {
        OTPDetails details = otpStorage.get(identifier);
        if (details == null || details.expiry.isBefore(LocalDateTime.now()))
            return false;

        boolean isValid = details.otp.equals(otp);
        if (isValid) otpStorage.remove(identifier); // Invalidate OTP
        return isValid;
    }

    private static class OTPDetails {
        String otp;
        LocalDateTime expiry;

        OTPDetails(String otp, LocalDateTime expiry) {
            this.otp = otp;
            this.expiry = expiry;
        }
    }
}
