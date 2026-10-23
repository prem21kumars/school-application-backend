package com.quizapp.dto;

import com.quizapp.entity.UserEntity;

public class LoginResponse {
    private boolean success;
    private String message;
    private UserEntity user;
    private String token;

    public LoginResponse() {}

    public LoginResponse(boolean success, String message, UserEntity user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }

    public LoginResponse(boolean success, String message, UserEntity user, String token) {
        this.success = success;
        this.message = message;
        this.user = user;
        this.token = token;
    }

    public static LoginResponse ok(UserEntity user) {
        return new LoginResponse(true, "Login successful", user);
    }

    public static LoginResponse ok(UserEntity user, String token) {
        return new LoginResponse(true, "Login successful", user, token);
    }

    public static LoginResponse fail(String message) {
        return new LoginResponse(false, message, null, null);
    }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public UserEntity getUser() { return user; }
    public void setUser(UserEntity user) { this.user = user; }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
}
