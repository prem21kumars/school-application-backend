package com.quizapp.controller;

import com.quizapp.dto.LoginRequest;
import com.quizapp.dto.LoginResponse;
import com.quizapp.entity.UserEntity;
import com.quizapp.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        if (response.isSuccess()) {
            String token = response.getToken();
            ResponseCookie httpOnlyCookie = authService.getJwtUtil().createHttpOnlyCookie(token);
            ResponseCookie clientCookie = authService.getJwtUtil().createClientCookie(token);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, httpOnlyCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, clientCookie.toString())
                    .body(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "No authentication token provided in cookies or Authorization header");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        Optional<UserEntity> userOpt = authService.getUserFromToken(token);
        if (userOpt.isEmpty()) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Invalid or expired JWT session");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }

        UserEntity user = userOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user", user);
        result.put("token", token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        ResponseCookie cleanHttpOnly = authService.getJwtUtil().createCleanHttpOnlyCookie();
        ResponseCookie cleanClient = authService.getJwtUtil().createCleanClientCookie();

        Map<String, Object> body = new HashMap<>();
        body.put("success", true);
        body.put("message", "Successfully logged out. Authentication cookies cleared.");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cleanHttpOnly.toString())
                .header(HttpHeaders.SET_COOKIE, cleanClient.toString())
                .body(body);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody com.quizapp.dto.ResetPasswordRequest request) {
        Map<String, Object> result = authService.resetPassword(request);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/reset-all-passwords")
    public ResponseEntity<?> resetAllPasswords() {
        Map<String, Object> result = authService.resetAllPasswordsToDefault();
        return ResponseEntity.ok(result);
    }

    private String resolveToken(HttpServletRequest request) {
        // 1. Try Authorization header
        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }

        // 2. Try HTTP-only or client cookies
        if (request.getCookies() != null) {
            for (Cookie c : request.getCookies()) {
                if ("jwt_token".equals(c.getName()) || "client_jwt".equals(c.getName()) || "token".equals(c.getName())) {
                    if (c.getValue() != null && !c.getValue().isBlank()) {
                        return c.getValue();
                    }
                }
            }
        }

        return null;
    }

    @PostMapping("/send-otp")
    public ResponseEntity<?> sendOtp(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        Map<String, Object> result = authService.sendOtp(identifier);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@RequestBody Map<String, String> request) {
        String identifier = request.get("identifier");
        String otp = request.get("otp");
        Map<String, Object> result = authService.verifyOtp(identifier, otp);
        if (Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }

}
