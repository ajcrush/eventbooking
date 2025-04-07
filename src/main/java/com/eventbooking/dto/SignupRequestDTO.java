package com.eventbooking.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class SignupRequestDTO {

    // Setters
    // Getters
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

}
