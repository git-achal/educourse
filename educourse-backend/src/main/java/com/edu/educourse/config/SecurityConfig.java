package com.edu.educourse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.edu.educourse.security.JwtAuthFilter;

/**
 * ============================================================
 *  SecurityConfig - Central Spring Security Configuration
 * ============================================================
 *
 *  @Configuration     - this class provides Spring beans
 *  @EnableWebSecurity - activates Spring Security for HTTP
 *  @EnableMethodSecurity - enables @PreAuthorize on methods
 *
 *  Beans defined here:
 *    1. SecurityFilterChain - URL access rules + JWT filter
 *    2. PasswordEncoder     - BCrypt for passwords
 *    3. AuthenticationManager - used by AuthService for login
 * ============================================================
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /**
     * JWT filter injected via constructor.
     * Reads and validates the JWT token on every request.
     */
    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    /**
     * Main HTTP security configuration.
     *
     * URL rules are evaluated TOP TO BOTTOM - first match wins.
     *
     * Public (no token needed):
     *   /api/auth/**   - register and login
     *   /h2-console/** - H2 browser UI
     *   /              - home health check
     *   /test          - API sanity check
     *
     * Admin only:
     *   /api/admin/**  - user management endpoints
     *
     * Any authenticated user:
     *   /api/**        - courses, favorites, purchases, user info
     *
     * Stateless session: no server-side sessions, JWT carries all state.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            // 1. Disable CSRF - not needed for stateless JWT REST APIs
            .csrf(csrf -> csrf.disable())

            // 2. Allow H2 console iframes
            .headers(headers -> headers.frameOptions(frame -> frame.disable()))

            // 3. No HTTP sessions - every request must carry its own JWT
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // 4. URL-based access rules
            .authorizeHttpRequests(auth -> auth

                // Public endpoints - no JWT required
                .requestMatchers(
                    "/api/auth/**",        // login, register
                    "/api/courses",        // all courses (homepage)
                    "/api/courses/**",     // search, filter, by-category, categories
                    "/h2-console/**",      // H2 browser console
                    "/",
                    "/test",
                    "/swagger-ui/**",      // Swagger UI static assets
                    "/swagger-ui.html",    // Swagger UI entry page
                    "/v3/api-docs/**",     // OpenAPI JSON spec
                    "/v3/api-docs"         // OpenAPI JSON spec root
                ).permitAll()

                // Admin-only REST API endpoints
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // All other API calls require a valid JWT (any role)
                // Finer control is done via @PreAuthorize in each controller
                .requestMatchers("/api/**").authenticated()

                // Legacy test routes
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/user/**").hasAnyRole("ADMIN", "STUDENT")

                // Everything else requires authentication
                .anyRequest().authenticated()
            )

            // 5. Run JWT filter before Spring's default login filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * BCrypt password encoder.
     * Strength 10 = 1024 hashing rounds (industry default).
     * Used by AuthService.register() and internally by AuthenticationManager.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Exposes Spring's AuthenticationManager as a bean.
     * AuthService injects this to call authenticate() during login.
     * Spring auto-detects our CustomUserDetailsService and PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
