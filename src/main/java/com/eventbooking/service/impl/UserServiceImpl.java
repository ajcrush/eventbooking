package com.eventbooking.service.impl;

import com.eventbooking.dto.SignupRequestDTO;
import com.eventbooking.model.Role;
import com.eventbooking.model.Role.RoleName;
import com.eventbooking.model.User;
import com.eventbooking.repository.RoleRepository;
import com.eventbooking.repository.UserRepository;
import com.eventbooking.service.OTPService;
import com.eventbooking.service.UserService;
import jakarta.transaction.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.Principal;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;
    private final RoleRepository roleRepo;
    private final OTPService otpService;
    private final PasswordEncoder passwordEncoder;

    // In-memory temp storage for users pending OTP verification
    private final Map<String, SignupRequestDTO> tempUsers = new HashMap<>();

    public UserServiceImpl(UserRepository userRepo,
                           RoleRepository roleRepo,
                           OTPService otpService,
                           PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.roleRepo = roleRepo;
        this.otpService = otpService;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void registerUser(SignupRequestDTO dto) {
        if (userRepo.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        if (userRepo.existsByMobileNumber(dto.getMobileNumber())) {
            throw new RuntimeException("Mobile number already registered");
        }

        tempUsers.put(dto.getEmail(), dto);
        otpService.generateAndSendOTP(dto.getEmail());
    }

    @Override
    @Transactional
    public User verifyOtp(String emailOrMobile, String otp) {
        if (!otpService.verifyOTP(emailOrMobile, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        SignupRequestDTO dto = tempUsers.remove(emailOrMobile);
        if (dto == null) {
            throw new RuntimeException("No signup request found for this identifier");
        }

        Role role = roleRepo.findByName(RoleName.valueOf(dto.getRole()))
                .orElseThrow(() -> new RuntimeException("Role not found"));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setMobileNumber(dto.getMobileNumber());
        user.setVerified(true);
        user.setRoles(Collections.singleton(role));

        return userRepo.save(user);
    }

    @Override
    public User authenticateUser(String emailOrMobile, String password) {
        User user = userRepo.findByEmail(emailOrMobile)
                .orElseGet(() -> userRepo.findByMobileNumber(emailOrMobile)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    @Override
    @Transactional
    public User updateUser(Map<String, String> updates) {
        String emailOrMobile = updates.get("emailOrMobile");
        User user = userRepo.findByEmail(emailOrMobile)
                .orElseGet(() -> userRepo.findByMobileNumber(emailOrMobile)
                        .orElseThrow(() -> new RuntimeException("User not found")));

        String newName = updates.get("name");
        String newMobile = updates.get("mobileNumber");
        String newEmail = updates.get("email");

        if (newName != null) user.setName(newName);
        if (newMobile != null) user.setMobileNumber(newMobile);

        if (newEmail != null && !newEmail.equals(user.getEmail())) {
            tempUsers.put(newEmail, new SignupRequestDTO(
                    user.getName(), newEmail, "", user.getMobileNumber(), "USER"));
            otpService.generateAndSendOTP(newEmail);
            return null; // Signal OTP pending
        }

        return userRepo.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(String email) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.getRoles().clear();
        userRepo.save(user);
        userRepo.delete(user);
    }

    @Override
    public User verifyEmailChangeOtp(String email, String otp) {
        if (!otpService.verifyOTP(email, otp)) {
            throw new RuntimeException("Invalid or expired OTP");
        }

        SignupRequestDTO dto = tempUsers.remove(email);
        if (dto == null) {
            throw new RuntimeException("No pending email update request");
        }

        User user = userRepo.findByMobileNumber(dto.getMobileNumber())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setEmail(dto.getEmail());
        return userRepo.save(user);
    }

    @Override
    public User getUserFromPrincipal(Principal principal) {
        String email = principal.getName();
        System.out.println("email : " + email);
        return userRepo.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
}
