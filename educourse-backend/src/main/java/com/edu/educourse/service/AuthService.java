package com.edu.educourse.service;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.edu.educourse.dto.AuthResponse;
import com.edu.educourse.dto.LoginRequest;
import com.edu.educourse.dto.RegisterRequest;
import com.edu.educourse.entity.Role;
import com.edu.educourse.entity.RoleType;
import com.edu.educourse.entity.User;
import com.edu.educourse.exception.UserAlreadyExistsException;
import com.edu.educourse.repository.RoleRepository;
import com.edu.educourse.repository.UserRepository;

/**
 * ============================================================
 *  AuthService — Business Logic for Registration and Login
 * ============================================================
 *
 *  This @Service class contains the core authentication logic.
 *  Controllers call this service; this service calls repositories.
 *  This layered approach keeps controllers thin and logic testable.
 *
 *  TWO MAIN OPERATIONS:
 *  ─────────────────────────────────────────────────────────────
 *  1. register(RegisterRequest)
 *     Creates a new user account. Always assigns ROLE_STUDENT.
 *     ROLE_ADMIN is never assignable via this method.
 *     ROLE_STUDENT_ADMIN is assigned later by admin via AdminController.
 *
 *  2. login(LoginRequest)
 *     Authenticates by EMAIL + PASSWORD.
 *     Returns a signed JWT token on success.
 *     Throws BadCredentialsException on failure (handled globally).
 *
 *  WHO ASSIGNS ROLES?
 *  ─────────────────────────────────────────────────────────────
 *  ROLE_STUDENT       → register() method (public, automatic)
 *  ROLE_STUDENT_ADMIN → AdminController.makeStudentAdmin() (admin only)
 *  ROLE_ADMIN         → DataInitializer reading admin-users.properties (startup only)
 *
 *  WHY EMAIL-BASED LOGIN?
 *  ─────────────────────────────────────────────────────────────
 *  Email is a unique, immutable identifier that users remember.
 *  Spring Security's AuthenticationManager expects a "username" string —
 *  we simply use email as that string everywhere in the system.
 * ============================================================
 */
@Service
public class AuthService {

    /**
     * Used to check if email already exists before creating a new user.
     * Also used to save the new user after building the User object.
     */
    @Autowired private UserRepository      userRepository;

    /**
     * Used to look up role objects (e.g., ROLE_STUDENT) by their enum type.
     * The Role object is a JPA entity with an ID, needed to build the roles Set.
     */
    @Autowired private RoleRepository      roleRepository;

    /**
     * BCryptPasswordEncoder — hashes the plain-text password before storing.
     * BCrypt is a one-way hash: you cannot reverse it to get the original password.
     * When the user logs in, Spring re-hashes the input and compares hashes.
     */
    @Autowired private PasswordEncoder     passwordEncoder;

    /**
     * JwtService — generates the signed JWT token after successful login.
     * The token is returned to the client for use in all future requests.
     */
    @Autowired private JwtService          jwtService;

    /**
     * Spring Security's central authentication component.
     * authenticate() handles:
     *   1. Loading the user via CustomUserDetailsService.loadUserByUsername(email)
     *   2. Comparing the provided password against the stored BCrypt hash
     *   3. Throwing BadCredentialsException if either check fails
     *
     * Configured in SecurityConfig as a @Bean so Spring can inject it here.
     */
    @Autowired private AuthenticationManager authenticationManager;

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers a new student user account.
     *
     * FULL STEP-BY-STEP FLOW:
     * ─────────────────────────────────────────────────────────────
     * Step 1: Email uniqueness check
     *   - userRepository.existsByEmail() queries: SELECT COUNT(*) FROM users WHERE email = ?
     *   - If found → throw UserAlreadyExistsException → GlobalExceptionHandler → 409 Conflict
     *   - This prevents duplicate accounts with the same email
     *
     * Step 2: Fetch ROLE_STUDENT from the database
     *   - We need the actual Role JPA entity (not just the enum)
     *   - Because User.roles is a @ManyToMany Set<Role> — it needs Role objects
     *   - roleRepository.findByName(ROLE_STUDENT) → SELECT * FROM roles WHERE name = 'ROLE_STUDENT'
     *   - .orElseThrow() fails fast if the role wasn't seeded by data.sql
     *
     * Step 3: Build the User entity
     *   - fullName: display name (e.g., "Rajesh Kumar")
     *   - email: stored lowercase and trimmed for consistency
     *   - username: mirrors fullName (kept for navbar display and backward compat)
     *   - password: BCrypt hash of the plain-text password
     *     e.g., "mypassword" → "$2a$10$xyz..." (60-char hash, can never be reversed)
     *   - roles: Set.of(studentRole) — single role, ROLE_STUDENT
     *
     * Step 4: Save to database
     *   - userRepository.save(user) → INSERT INTO users (...) VALUES (...)
     *   - JPA also inserts into user_roles: INSERT INTO user_roles (user_id, role_id) VALUES (?, 2)
     *   - The user's ID is auto-generated by the database (IDENTITY strategy)
     *
     * What happens AFTER register?
     *   - The client shows a success message and redirects to login.html
     *   - The user must then POST to /api/auth/login to get a JWT token.
     *
     * @param request DTO containing fullName, email, password (validated by @Valid in controller)
     * @throws UserAlreadyExistsException if the email is already registered (→ 409 Conflict)
     */
    public void register(RegisterRequest request) {

        // Step 1: Reject duplicate email — one account per email address
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                "An account with email '" + request.getEmail() + "' already exists.");
        }

        // Step 2: Look up ROLE_STUDENT from the roles table
        // This role was seeded in data.sql: INSERT INTO roles VALUES (2, 'ROLE_STUDENT')
        // Public registration ALWAYS gets ROLE_STUDENT, never ROLE_ADMIN
        Role studentRole = roleRepository.findByName(RoleType.ROLE_STUDENT)
                .orElseThrow(() -> new RuntimeException("ROLE_STUDENT not found in DB — check data.sql"));

        // Step 3 & 4: Build and persist the new user
        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail().toLowerCase().trim()); // normalize email
        user.setUsername(request.getFullName());                // username = display name
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt hash
        user.setRoles(Set.of(studentRole));                     // only ROLE_STUDENT

        // Saves to users table AND inserts into user_roles join table
        userRepository.save(user);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Authenticates a user by email and password, returns a JWT token.
     *
     * FULL STEP-BY-STEP FLOW:
     * ─────────────────────────────────────────────────────────────
     * Step 1: AuthenticationManager.authenticate()
     *   - We create a UsernamePasswordAuthenticationToken with email as the "username"
     *   - AuthenticationManager delegates to CustomUserDetailsService.loadUserByUsername(email)
     *     which calls: SELECT * FROM users WHERE email = ? (with all roles eagerly loaded)
     *   - Spring Security then compares the provided password against the stored BCrypt hash
     *     using: passwordEncoder.matches(rawPassword, storedHash)
     *   - If NO user found: throws UsernameNotFoundException → 404
     *   - If PASSWORD wrong: throws BadCredentialsException → 401
     *   - If BOTH correct: authentication proceeds silently (no return value needed)
     *
     * Step 2: Generate JWT token
     *   - jwtService.generateToken(email) → JwtUtil.generateToken(email)
     *   - Creates a signed token with:
     *       subject (sub)  = user's email
     *       issuedAt (iat) = now
     *       expiration (exp) = now + 24 hours
     *   - The token is signed with HMAC-SHA256 using our secret key from application.yaml
     *
     * Step 3: Return AuthResponse
     *   - { "token": "eyJhbGci...", "tokenType": "Bearer" }
     *   - The frontend stores this in localStorage
     *   - All future requests include: Authorization: Bearer eyJhbGci...
     *
     * SECURITY NOTE:
     * ─────────────────────────────────────────────────────────────
     *   We intentionally do NOT say "email not found" vs "wrong password"
     *   in the error response. Both give a generic 401 Unauthorized.
     *   This prevents username enumeration attacks where attackers probe
     *   which emails are registered before trying to crack passwords.
     *
     * @param request DTO containing email and password
     * @return AuthResponse with the signed JWT token and token type
     * @throws BadCredentialsException if authentication fails (handled by GlobalExceptionHandler)
     */
    public AuthResponse login(LoginRequest request) {

        // Step 1: Let Spring Security do the full authentication:
        //   load user by email → compare BCrypt hash → throw if wrong
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getEmail().toLowerCase().trim(), // normalize email (same as stored)
                request.getPassword()                    // raw password (Spring compares with BCrypt hash)
            )
        );

        // Step 2: Authentication succeeded — generate the JWT
        // The email is used as the JWT subject claim
        String token = jwtService.generateToken(
            request.getEmail().toLowerCase().trim()
        );

        // Step 3: Wrap token in AuthResponse DTO and return it
        // This becomes: {"token": "eyJhbGci...", "tokenType": "Bearer"}
        return new AuthResponse(token);
    }
}
