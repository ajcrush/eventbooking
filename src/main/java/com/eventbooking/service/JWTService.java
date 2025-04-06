package com.eventbooking.service;

import com.eventbooking.model.User;

public interface JWTService {
    String generateToken(User user);
    String extractUsername(String token);
    boolean isTokenValid(String token, User user);
}
