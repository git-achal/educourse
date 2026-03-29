package com.edu.educourse.service;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.edu.educourse.entity.User;

/**
 * CustomUserDetails — bridges our User entity and Spring Security.
 *
 * IMPORTANT: getUsername() returns the EMAIL, not the display username.
 * Email is our login identifier, so it must be the Spring Security principal.
 * JwtUtil stores email in the JWT subject claim.
 * JwtAuthFilter calls loadUserByUsername(email) with the extracted subject.
 */
public class CustomUserDetails implements UserDetails {

    private static final long serialVersionUID = 1L;

    /** Email — used as the Spring Security principal (login identifier) */
    private final String email;

    /** BCrypt-encoded password hash */
    private final String password;

    /** Roles converted to Spring's GrantedAuthority format */
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.email    = user.getEmail();   // email is the principal
        this.password = user.getPassword();
        this.authorities = user.getRoles()
                .stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());
    }

    /**
     * Returns email — this is what Spring Security uses as the "username" principal.
     * JWT subject = email. loadUserByUsername(email). @AuthenticationPrincipal gives email.
     */
    @Override public String getUsername()                 { return email; }
    @Override public String getPassword()                 { return password; }
    @Override public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }
    @Override public boolean isAccountNonExpired()        { return true; }
    @Override public boolean isAccountNonLocked()         { return true; }
    @Override public boolean isCredentialsNonExpired()    { return true; }
    @Override public boolean isEnabled()                  { return true; }
}
