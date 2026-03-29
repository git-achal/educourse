package com.edu.educourse.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * LoginRequest — DTO for login.
 * Email is the login identifier (replaces username).
 */
public class LoginRequest {

    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    public LoginRequest() {}

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    // Spring Security uses getUsername() internally — delegate to email
    public String getUsername()             { return email; }

    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }
}
