package com.edu.educourse.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * ============================================================
 *  TestController — Endpoints to Verify Security Configuration
 * ============================================================
 *
 *  This controller has three endpoints, each with a different
 *  level of access restriction, to test that Spring Security
 *  is working correctly for all roles.
 *
 *  Endpoint Summary:
 *  ------------------
 *  GET /test         → PUBLIC  — no JWT needed
 *  GET /user         → AUTHENTICATED — ROLE_STUDENT or ROLE_ADMIN
 *  GET /admin        → ADMIN ONLY — ROLE_ADMIN
 *
 *  Two levels of security are demonstrated:
 *  ------------------------------------------
 *  1. URL-level (SecurityConfig):
 *     .requestMatchers("/admin/**").hasRole("ADMIN")
 *     .requestMatchers("/user/**").hasAnyRole("ADMIN", "STUDENT")
 *     Applied to all matching URLs at the filter level.
 *
 *  2. Method-level (@PreAuthorize):
 *     @PreAuthorize("hasRole('ADMIN')") on the adminApi() method.
 *     Applied directly to the method — a second layer of protection.
 *     Even if someone removes the URL rule, this still protects the method.
 *     Requires @EnableMethodSecurity in SecurityConfig.
 * ============================================================
 */
@RestController
public class TestController {

    /**
     * Public test endpoint — accessible without any JWT token.
     *
     * GET /test
     *
     * Configured in SecurityConfig:
     *   .requestMatchers("/test").permitAll() → no authentication required
     *
     * Use this to verify the API is running before attempting login.
     *
     * @return a plain string confirming the API works
     */
    @GetMapping("/test")
    public String testApi() {
        return "API is working successfully! No authentication required for this endpoint.";
    }

    /**
     * User endpoint — accessible by ROLE_STUDENT and ROLE_ADMIN.
     *
     * GET /user
     *
     * Configured in SecurityConfig:
     *   .requestMatchers("/user/**").hasAnyRole("ADMIN", "STUDENT")
     *
     * This method reads the authenticated user's information from
     * the SecurityContextHolder, which was set by JwtAuthFilter.
     *
     * SecurityContextHolder:
     *   A thread-local holder that stores the Authentication object
     *   for the current request thread.
     *   JwtAuthFilter populates it after validating the JWT token.
     *   It's cleared automatically after the request completes.
     *
     * @return greeting message with the username and their roles
     */
    @GetMapping("/user")
    public String userApi() {

        // Get the current Authentication object from SecurityContextHolder
        // This was set by JwtAuthFilter after validating the JWT token
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // authentication.getName() returns the username (the JWT subject claim)
        // authentication.getAuthorities() returns the user's roles as GrantedAuthority objects
        //   e.g., [ROLE_STUDENT] or [ROLE_ADMIN]
        return "Hello " + authentication.getName()
                + "! You are authenticated. Your roles: " + authentication.getAuthorities();
    }

    /**
     * Admin-only endpoint — accessible ONLY by ROLE_ADMIN.
     *
     * GET /admin
     *
     * Protected by TWO layers of security:
     *
     * Layer 1 — URL rule in SecurityConfig:
     *   .requestMatchers("/admin/**").hasRole("ADMIN")
     *   This blocks non-admin requests at the filter level,
     *   before the request even reaches this controller.
     *
     * Layer 2 — @PreAuthorize annotation:
     *   @PreAuthorize("hasRole('ADMIN')")
     *   This is method-level security.
     *   Spring AOP intercepts the method call and checks the role.
     *   If the user doesn't have ROLE_ADMIN, an AccessDeniedException
     *   is thrown → 403 Forbidden response.
     *
     * Why two layers?
     *   Defense in depth: if someone accidentally removes the URL rule,
     *   the @PreAuthorize still protects this specific endpoint.
     *
     * @return greeting message with the admin's username
     */
    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminApi() {

        // Read the authenticated admin user from SecurityContextHolder
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        return "Hello Admin " + authentication.getName()
                + "! You have full admin access. Roles: " + authentication.getAuthorities();
    }
}
