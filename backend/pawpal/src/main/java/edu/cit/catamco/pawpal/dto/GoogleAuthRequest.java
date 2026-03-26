package edu.cit.catamco.pawpal.dto;

import jakarta.validation.constraints.NotBlank;

public class GoogleAuthRequest {

    @NotBlank(message = "Token is required")
    private String token;

    private String role; // only needed for register

    public String getToken() { return token; }
    public String getRole() { return role; }

    public void setToken(String token) { this.token = token; }
    public void setRole(String role) { this.role = role; }
}