package com.eventbooking.service;

import com.eventbooking.dto.SignupRequestDTO;
import com.eventbooking.model.User;

import java.security.Principal;
import java.util.Map;

public interface UserService {
    void registerUser(SignupRequestDTO signupRequestDTO);
    User verifyOtp(String emailOrMobile, String otp);
    User updateUser(Map<String, String> updates);
    void deleteUser(String emailOrMobile);
    User verifyEmailChangeOtp(String email, String otp);
    User authenticateUser(String emailOrMobile, String password);
    User getUserFromPrincipal(Principal principal);
}
