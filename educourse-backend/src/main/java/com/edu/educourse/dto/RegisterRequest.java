package com.edu.educourse.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * RegisterRequest — DTO for new user registration.
 *
 * Fields:
 *   fullName    → display name
 *   email       → unique login identifier
 *   username    → display name alias (same as fullName, kept for compat)
 *   password    → min 6 chars, BCrypt-encoded before storage
 *   confirmPassword → validated client-side; not stored
 */
public class RegisterRequest {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    /** Optional — not validated server-side (client handles confirm match) */
    private String confirmPassword;

    /** Optional role hint — only ROLE_STUDENT accepted from public registration */
    private String role;

    // ── Constructors ──────────────────────────────────────────────

    public RegisterRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────

    public String getFullName()                     { return fullName; }
    public void setFullName(String fn)              { this.fullName = fn; }

    public String getEmail()                        { return email; }
    public void setEmail(String email)              { this.email = email; }

    /** Username defaults to fullName if not separately provided */
    public String getUsername() {
        return (fullName != null) ? fullName : email;
    }

    public String getPassword()                     { return password; }
    public void setPassword(String p)               { this.password = p; }

    public String getConfirmPassword()              { return confirmPassword; }
    public void setConfirmPassword(String cp)       { this.confirmPassword = cp; }

    public String getRole()                         { return role; }
    public void setRole(String role)                { this.role = role; }
}
