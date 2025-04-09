package com.eventbooking.controller;

import com.eventbooking.dto.LoginRequestDTO;
import com.eventbooking.dto.OTPVerifyDTO;
import com.eventbooking.dto.SignupRequestDTO;
import com.eventbooking.model.User;
import com.eventbooking.service.JWTService;
import com.eventbooking.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JWTService jwtService;

    public AuthController(UserService userService, JWTService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/signup")
    public ResponseEntity<Map<String, Object>> signup(@Valid @RequestBody SignupRequestDTO request) {
        try {
            // Disallow signup for role ADMIN
            if ("ADMIN".equalsIgnoreCase(request.getRole())) {
                return ResponseEntity.status(403).body(errorResponse("Admin signup is not allowed."));
            }

            userService.registerUser(request);
            return ResponseEntity.ok(successResponse("OTP sent to email or mobile. Please verify.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, Object>> verifyOtp(@Valid @RequestBody OTPVerifyDTO request, HttpServletResponse response) {
        User user = userService.verifyOtp(request.getEmailOrMobile(), request.getOtp());

        String token = jwtService.generateToken(user);
        Cookie cookie = new Cookie("jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60); // 7 days
        response.addCookie(cookie);

        return ResponseEntity.ok(successResponse("User verified and logged in.", user));
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequestDTO loginDTO, HttpServletResponse response) {
        try {
            User user = userService.authenticateUser(loginDTO.getEmailOrMobile(), loginDTO.getPassword());

            if (!user.isVerified()) {
                return ResponseEntity.badRequest().body(errorResponse("User is not verified. Please verify OTP first."));
            }

            String token = jwtService.generateToken(user);
            Cookie cookie = new Cookie("jwt", token);
            cookie.setHttpOnly(true);
            cookie.setPath("/");
            cookie.setMaxAge(7 * 24 * 60 * 60);
            response.addCookie(cookie);

            return ResponseEntity.ok(successResponse("User logged in successfully.", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PutMapping("/update")
    public ResponseEntity<Map<String, Object>> updateUser(@RequestBody Map<String, String> updates) {
        try {
            User updatedUser = userService.updateUser(updates);
            if (updatedUser == null) {
                return ResponseEntity.ok(successResponse("OTP sent to new email. Please verify email change.", null));
            }
            return ResponseEntity.ok(successResponse("User updated successfully.", updatedUser));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PostMapping("/verify-email-change")
    public ResponseEntity<Map<String, Object>> verifyEmailChange(@RequestBody OTPVerifyDTO dto) {
        try {
            User user = userService.verifyEmailChangeOtp(dto.getEmailOrMobile(), dto.getOtp());
            return ResponseEntity.ok(successResponse("Email updated successfully.", user));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Map<String, Object>> deleteUser(@RequestParam String email) {
        try {
            userService.deleteUser(email);
            return ResponseEntity.ok(successResponse("User deleted successfully.", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0); // delete the cookie
        response.addCookie(cookie);

        return ResponseEntity.ok(successResponse("User logged out successfully.", null));
    }

    @GetMapping("/user/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(Principal principal) {
        User user = userService.getUserFromPrincipal(principal);
        Map<String, Object> response = new HashMap<>();
        response.put("user", user);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, Object>> changePassword(@RequestBody Map<String, String> body) {
        try {
            String emailOrMobile = body.get("emailOrMobile");
            String currentPassword = body.get("currentPassword");
            String newPassword = body.get("newPassword");

            userService.changePassword(emailOrMobile, currentPassword, newPassword);
            return ResponseEntity.ok(successResponse("Password changed successfully", null));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(errorResponse(e.getMessage()));
        }
    }

    private Map<String, Object> successResponse(String message, User user) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "success");
        map.put("message", message);

        if (user != null) {
            Map<String, Object> userInfo = new HashMap<>();
            userInfo.put("name", user.getName());
            userInfo.put("email", user.getEmail());
            userInfo.put("verified", user.isVerified());
            map.put("user", userInfo);
        }

        return map;
    }

    private Map<String, Object> errorResponse(String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "error");
        map.put("message", message);
        return map;
    }
}
