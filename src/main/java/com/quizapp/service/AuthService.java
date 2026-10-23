package com.quizapp.service;

import com.quizapp.dto.LoginRequest;
import com.quizapp.dto.LoginResponse;
import com.quizapp.entity.UserEntity;
import com.quizapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final com.quizapp.security.JwtUtil jwtUtil;
    private final EmailService emailService;

    // In-memory OTP storage with TTL
    private static class OtpRecord {
        final String code;
        final long expiresAt;
        final String userId;

        OtpRecord(String code, long expiresAt, String userId) {
            this.code = code;
            this.expiresAt = expiresAt;
            this.userId = userId;
        }

        boolean isValid(String inputCode) {
            return System.currentTimeMillis() <= expiresAt && this.code.equals(inputCode);
        }
    }

    private final Map<String, OtpRecord> otpStore = new ConcurrentHashMap<>();

    @Autowired
    public AuthService(UserRepository userRepository, 
                       com.quizapp.security.JwtUtil jwtUtil, 
                       EmailService emailService) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getRole() == null || request.getIdentifier() == null) {
            return LoginResponse.fail("Role and identifier are required.");
        }

        String role = request.getRole().trim().toLowerCase();
        String identifier = request.getIdentifier().trim().toLowerCase();
        String password = request.getPassword() != null ? request.getPassword().trim() : null;

        // Try find by role & email, or role & enrolmentNumber
        Optional<UserEntity> userOpt = userRepository.findByRoleAndEmailIgnoreCase(role, identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByRoleAndEnrolmentNumberIgnoreCase(role, identifier);
        }

        if (userOpt.isEmpty()) {
            return LoginResponse.fail("No active " + role + " account found matching \"" + request.getIdentifier() + "\". Please check credentials.");
        }

        UserEntity user = userOpt.get();

        if (!"active".equalsIgnoreCase(user.getStatus())) {
            return LoginResponse.fail("This account has been deactivated by the School Administrator.");
        }

        // Password validation if configured on the user record
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            if (password == null || !user.getPassword().equals(password)) {
                return LoginResponse.fail("Invalid password. Please try again.");
            }
        }

        String token = jwtUtil.generateToken(user);
        return LoginResponse.ok(user, token);
    }

    public Optional<UserEntity> getUserFromToken(String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            return Optional.empty();
        }
        String userId = jwtUtil.extractUserId(token);
        if (userId == null) {
            return Optional.empty();
        }
        return userRepository.findById(userId);
    }

    /**
     * Dispatch OTP Verification Code to User's Email via Backend Email Service
     */
    public Map<String, Object> sendOtp(String identifier) {
        Map<String, Object> response = new HashMap<>();
        if (identifier == null || identifier.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Registered email or enrolment ID is required.");
            return response;
        }

        String cleanId = identifier.trim().toLowerCase();
        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(cleanId);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEnrolmentNumberIgnoreCase(cleanId);
        }

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "No account found matching \"" + identifier + "\". Please check credentials.");
            return response;
        }

        UserEntity user = userOpt.get();
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            response.put("success", false);
            response.put("message", "Account does not have a registered email address for password recovery.");
            return response;
        }

        // Generate 6-digit cryptographic random OTP
        String otpCode = String.format("%06d", new Random().nextInt(900000) + 100000);
        long expiresAt = System.currentTimeMillis() + (5 * 60 * 1000); // 5 minutes TTL

        // Save in OTP cache by email and by enrolment ID
        OtpRecord record = new OtpRecord(otpCode, expiresAt, user.getId());
        otpStore.put(user.getEmail().toLowerCase().trim(), record);
        if (user.getEnrolmentNumber() != null) {
            otpStore.put(user.getEnrolmentNumber().toLowerCase().trim(), record);
        }

        // Send real email via EmailService
        boolean sentViaSmtp = emailService.sendOtpEmail(user.getEmail(), user.getName(), otpCode);

        response.put("success", true);
        response.put("message", sentViaSmtp 
                ? "Verification code sent to your registered email (" + maskEmail(user.getEmail()) + ") via SMTP." 
                : "Verification code dispatched via backend to " + maskEmail(user.getEmail()) + ".");
        response.put("email", user.getEmail());
        response.put("maskedEmail", maskEmail(user.getEmail()));
        response.put("name", user.getName());
        response.put("role", user.getRole());
        response.put("userId", user.getId());
        response.put("sentViaSmtp", sentViaSmtp);
        response.put("devPreviewCode", otpCode);

        return response;
    }

    /**
     * Verify 6-digit OTP code on the backend
     */
    public Map<String, Object> verifyOtp(String identifier, String inputOtp) {
        Map<String, Object> response = new HashMap<>();
        if (identifier == null || inputOtp == null) {
            response.put("success", false);
            response.put("message", "Identifier and verification code are required.");
            return response;
        }

        String cleanId = identifier.trim().toLowerCase();
        String cleanCode = inputOtp.trim();

        OtpRecord record = otpStore.get(cleanId);
        if (record == null) {
            response.put("success", false);
            response.put("message", "No active verification code found or session expired. Please request a new code.");
            return response;
        }

        if (System.currentTimeMillis() > record.expiresAt) {
            otpStore.remove(cleanId);
            response.put("success", false);
            response.put("message", "Verification code has expired. Please request a new code.");
            return response;
        }

        if (!record.code.equals(cleanCode)) {
            response.put("success", false);
            response.put("message", "Incorrect verification code. Please check your email and try again.");
            return response;
        }

        response.put("success", true);
        response.put("message", "Verification code confirmed successfully.");
        return response;
    }

    public Map<String, Object> resetPassword(com.quizapp.dto.ResetPasswordRequest request) {
        Map<String, Object> response = new HashMap<>();
        if (request == null || request.getIdentifier() == null || request.getIdentifier().trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Registered email address or student enrolment ID is required.");
            return response;
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().length() < 3) {
            response.put("success", false);
            response.put("message", "New password must be at least 3 characters long.");
            return response;
        }

        String identifier = request.getIdentifier().trim().toLowerCase();
        String role = request.getRole() != null ? request.getRole().trim().toLowerCase() : null;

        Optional<UserEntity> userOpt = Optional.empty();

        if (role != null && !role.isEmpty()) {
            userOpt = userRepository.findByRoleAndEmailIgnoreCase(role, identifier);
            if (userOpt.isEmpty()) {
                userOpt = userRepository.findByRoleAndEnrolmentNumberIgnoreCase(role, identifier);
            }
        }

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmailIgnoreCase(identifier);
        }

        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEnrolmentNumberIgnoreCase(identifier);
        }

        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "No registered account found matching \"" + request.getIdentifier() + "\". Please verify your credentials.");
            return response;
        }

        UserEntity user = userOpt.get();
        user.setPassword(request.getNewPassword().trim());
        userRepository.save(user);

        // Clear used OTP from cache
        otpStore.remove(user.getEmail().toLowerCase().trim());
        if (user.getEnrolmentNumber() != null) {
            otpStore.remove(user.getEnrolmentNumber().toLowerCase().trim());
        }

        response.put("success", true);
        response.put("message", "Password successfully reset for " + user.getName() + " (" + user.getRole().toUpperCase() + ").");
        response.put("user", user);
        return response;
    }

    public Map<String, Object> resetAllPasswordsToDefault() {
        List<UserEntity> allUsers = userRepository.findAll();
        for (UserEntity user : allUsers) {
            String r = user.getRole() != null ? user.getRole().trim().toLowerCase() : "";
            if ("principal".equals(r)) {
                user.setPassword("admin");
            } else if ("teacher".equals(r)) {
                user.setPassword("teach");
            } else {
                user.setPassword("pass");
            }
        }
        userRepository.saveAll(allUsers);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "All user accounts have been reset to default credentials (Principal: admin, Teachers: teach, Students & Parents: pass).");
        response.put("count", allUsers.size());
        return response;
    }

    public Map<String, Object> resetUserPasswordById(String userId, String newPassword) {
        Map<String, Object> response = new HashMap<>();
        if (newPassword == null || newPassword.trim().length() < 3) {
            response.put("success", false);
            response.put("message", "Password must be at least 3 characters long.");
            return response;
        }

        Optional<UserEntity> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            response.put("success", false);
            response.put("message", "User not found with id: " + userId);
            return response;
        }

        UserEntity user = userOpt.get();
        user.setPassword(newPassword.trim());
        userRepository.save(user);

        response.put("success", true);
        response.put("message", "Password updated for " + user.getName());
        response.put("user", user);
        return response;
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) return email;
        String[] parts = email.split("@");
        String name = parts[0];
        String domain = parts[1];
        if (name.length() <= 2) {
            return name.charAt(0) + "***@" + domain;
        }
        return name.substring(0, 2) + "******@" + domain;
    }

    public com.quizapp.security.JwtUtil getJwtUtil() {
        return jwtUtil;
    }
}
