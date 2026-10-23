package com.quizapp.dto;

public class LoginRequest {
    private String role; // principal, teacher, student, parent
    private String identifier; // email or enrolmentNumber
    private String password;

    public LoginRequest() {}

    public LoginRequest(String role, String identifier, String password) {
        this.role = role;
        this.identifier = identifier;
        this.password = password;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getIdentifier() { return identifier; }
    public void setIdentifier(String identifier) { this.identifier = identifier; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
