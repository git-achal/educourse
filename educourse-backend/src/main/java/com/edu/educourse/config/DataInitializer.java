package com.edu.educourse.config;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.edu.educourse.entity.Role;
import com.edu.educourse.entity.RoleType;
import com.edu.educourse.entity.User;
import com.edu.educourse.repository.RoleRepository;
import com.edu.educourse.repository.UserRepository;

/**
 * ============================================================
 *  DataInitializer — Bootstraps admin users at startup
 * ============================================================
 *
 *  Reads admin-users.properties at runtime.
 *  For each email in admin.emails:
 *    - If user doesn't exist → creates them with default password
 *    - If user exists but lacks ROLE_ADMIN → promotes them
 *    - If user exists and already has ROLE_ADMIN → no change (idempotent)
 *
 *  This is the ONLY way ROLE_ADMIN is ever assigned.
 *  No API endpoint can create or grant ROLE_ADMIN.
 *
 *  Login credentials for auto-created admins:
 *    email:    as listed in admin.emails
 *    password: admin.default-password (change before production!)
 * ============================================================
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired private UserRepository  userRepository;
    @Autowired private RoleRepository  roleRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    /**
     * Comma-separated admin emails from admin-users.properties.
     * Example: "elkorf@educourse.com, boss@company.com"
     */
    @Value("${admin.emails}")
    private String adminEmails;

    /**
     * Default password for auto-created admin accounts.
     * Users should change this after first login.
     */
    @Value("${admin.default-password}")
    private String defaultPassword;

    @Override
    public void run(ApplicationArguments args) {
        Role adminRole = roleRepository.findByName(RoleType.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException(
                        "ROLE_ADMIN not found in DB — check data.sql"));

        // Parse comma-separated email list, trim whitespace
        List<String> emails = Arrays.stream(adminEmails.split(","))
                .map(String::trim)
                .filter(e -> !e.isBlank())
                .toList();

        for (String email : emails) {
            processAdminEmail(email.toLowerCase(), adminRole);
        }
    }

    private void processAdminEmail(String email, Role adminRole) {
        userRepository.findByEmail(email).ifPresentOrElse(
            // User exists — ensure they have ROLE_ADMIN
            existingUser -> {
                boolean alreadyAdmin = existingUser.getRoles().stream()
                        .anyMatch(r -> r.getName() == RoleType.ROLE_ADMIN);
                if (!alreadyAdmin) {
                    Set<Role> roles = new HashSet<>(existingUser.getRoles());
                    roles.add(adminRole);
                    existingUser.setRoles(roles);
                    userRepository.save(existingUser);
                    System.out.printf("[DataInitializer] Promoted '%s' to ROLE_ADMIN.%n", email);
                }
            },
            // User does not exist — create them
            () -> {
                // Extract display name from email (part before @)
                String displayName = email.contains("@")
                        ? email.substring(0, email.indexOf('@'))
                        : email;
                displayName = displayName.substring(0, 1).toUpperCase() + displayName.substring(1);

                User admin = new User();
                admin.setFullName(displayName);
                admin.setEmail(email);
                admin.setUsername(displayName);
                admin.setPassword(passwordEncoder.encode(defaultPassword));
                Set<Role> roles = new HashSet<>();
                roles.add(adminRole);
                admin.setRoles(roles);

                userRepository.save(admin);
                System.out.printf(
                    "[DataInitializer] Created admin '%s' (email: %s, password: %s).%n",
                    displayName, email, defaultPassword
                );
            }
        );
    }
}
