package com.edu.educourse.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.edu.educourse.dto.UserDto;
import com.edu.educourse.entity.Role;
import com.edu.educourse.entity.RoleType;
import com.edu.educourse.entity.User;
import com.edu.educourse.repository.RoleRepository;
import com.edu.educourse.repository.UserRepository;

/**
 * ============================================================
 *  AdminController — Full user management (ROLE_ADMIN only)
 * ============================================================
 *
 *  Users hold MULTIPLE roles simultaneously via the many-to-many
 *  user_roles join table. Example:
 *    STUDENT  →  [ROLE_STUDENT]
 *    STUDENT_ADMIN → [ROLE_STUDENT, ROLE_STUDENT_ADMIN]
 *    ADMIN    →  [ROLE_ADMIN]
 *
 *  makeStudentAdmin  → ADDS ROLE_STUDENT_ADMIN (keeps ROLE_STUDENT)
 *  removeStudentAdmin → REMOVES ROLE_STUDENT_ADMIN (keeps ROLE_STUDENT)
 *
 *  Endpoints:
 *    GET    /api/admin/users                        → list all users
 *    GET    /api/admin/roles                        → list all available roles
 *    POST   /api/admin/make-student-admin/{email}   → add STUDENT_ADMIN role
 *    DELETE /api/admin/remove-student-admin/{email} → remove STUDENT_ADMIN role
 *    DELETE /api/admin/users/{email}                → delete any non-admin user
 * ============================================================
 */
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired private UserRepository userRepository;
    @Autowired private RoleRepository roleRepository;

    // ── List all users ────────────────────────────────────────────

    /** GET /api/admin/users — returns all users with their current role set */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        return ResponseEntity.ok(
            userRepository.findAll().stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    // ── List all roles ────────────────────────────────────────────

    /**
     * GET /api/admin/roles
     * Returns all role names available in the system.
     * Used by the admin frontend to populate the Roles table.
     */
    @GetMapping("/roles")
    public ResponseEntity<List<String>> getAllRoles() {
        return ResponseEntity.ok(
            roleRepository.findAll().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList())
        );
    }

    // ── Grant STUDENT_ADMIN ───────────────────────────────────────

    /**
     * POST /api/admin/make-student-admin/{email}
     *
     * ADDS ROLE_STUDENT_ADMIN to the user's existing roles.
     * Does NOT remove ROLE_STUDENT — the user keeps both roles.
     *
     * Result: user_roles table has two rows for this user:
     *   (user_id, role_id=ROLE_STUDENT)
     *   (user_id, role_id=ROLE_STUDENT_ADMIN)
     *
     * This means Spring Security sees both authorities:
     *   hasRole("STUDENT") → true
     *   hasRole("STUDENT_ADMIN") → true
     */
    @PostMapping("/make-student-admin/{email}")
    public ResponseEntity<String> makeStudentAdmin(@PathVariable String email) {
        User user = findUserByEmail(email);

        if (hasRole(user, RoleType.ROLE_ADMIN))
            return ResponseEntity.badRequest().body("Cannot change roles of an ADMIN user.");

        if (hasRole(user, RoleType.ROLE_STUDENT_ADMIN))
            return ResponseEntity.ok(email + " already has ROLE_STUDENT_ADMIN.");

        // ADD the role — preserve all existing roles
        Role saRole = getRole(RoleType.ROLE_STUDENT_ADMIN);
        Set<Role> updatedRoles = new HashSet<>(user.getRoles());
        updatedRoles.add(saRole);
        user.setRoles(updatedRoles);
        userRepository.save(user);

        return ResponseEntity.ok(email + " has been granted ROLE_STUDENT_ADMIN.");
    }

    // ── Revoke STUDENT_ADMIN ──────────────────────────────────────

    /**
     * DELETE /api/admin/remove-student-admin/{email}
     *
     * REMOVES ROLE_STUDENT_ADMIN from the user's roles.
     * ROLE_STUDENT is preserved — the user goes back to student-only.
     *
     * If user somehow lost ROLE_STUDENT (edge case), it is restored.
     */
    @DeleteMapping("/remove-student-admin/{email}")
    public ResponseEntity<String> removeStudentAdmin(@PathVariable String email) {
        User user = findUserByEmail(email);

        if (hasRole(user, RoleType.ROLE_ADMIN))
            return ResponseEntity.badRequest().body("Cannot change roles of an ADMIN user.");

        if (!hasRole(user, RoleType.ROLE_STUDENT_ADMIN))
            return ResponseEntity.ok(email + " does not have ROLE_STUDENT_ADMIN.");

        // REMOVE student_admin, ensure ROLE_STUDENT is present
        Role studentRole = getRole(RoleType.ROLE_STUDENT);
        Set<Role> updatedRoles = user.getRoles().stream()
                .filter(r -> r.getName() != RoleType.ROLE_STUDENT_ADMIN)
                .collect(Collectors.toCollection(HashSet::new));
        updatedRoles.add(studentRole);   // guarantee ROLE_STUDENT is kept
        user.setRoles(updatedRoles);
        userRepository.save(user);

        return ResponseEntity.ok(email + " has been demoted — ROLE_STUDENT_ADMIN removed.");
    }

    // ── Delete user ───────────────────────────────────────────────

    /** DELETE /api/admin/users/{email} — permanently removes a non-admin user */
    @DeleteMapping("/users/{email}")
    public ResponseEntity<String> deleteUser(@PathVariable String email) {
        User user = findUserByEmail(email);
        if (hasRole(user, RoleType.ROLE_ADMIN))
            return ResponseEntity.badRequest().body("Cannot delete an ADMIN user.");
        userRepository.delete(user);
        return ResponseEntity.ok("User " + email + " deleted successfully.");
    }

    // ── Helpers ───────────────────────────────────────────────────

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    private Role getRole(RoleType type) {
        return roleRepository.findByName(type)
                .orElseThrow(() -> new RuntimeException(type + " not found in DB"));
    }

    private boolean hasRole(User user, RoleType type) {
        return user.getRoles().stream().anyMatch(r -> r.getName() == type);
    }

    private UserDto toDto(User u) {
        List<String> roleNames = u.getRoles().stream()
                .map(r -> r.getName().name())
                .collect(Collectors.toList());
        return new UserDto(u.getId(), u.getFullName(), u.getEmail(), u.getUsername(), roleNames);
    }
}
