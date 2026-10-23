package com.quizapp.dto;

public class ResetPasswordRequest {
    private String identifier; // email or enrolmentNumber
    private String newPassword;
    private String role; // optional role to disambiguate

    public ResetPasswordRequest() {}

    public ResetPasswordRequest(String identifier, String newPassword) {
        this.identifier = identifier;
        this.newPassword = newPassword;
    }

    public ResetPasswordRequest(String identifier, String newPassword, String role) {
        this.identifier = identifier;
        this.newPassword = newPassword;
        this.role = role;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
