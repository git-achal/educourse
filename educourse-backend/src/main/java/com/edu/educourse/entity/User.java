package com.edu.educourse.entity;

import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;

/**
 * ============================================================
 *  User — JPA Entity mapped to the "users" table
 * ============================================================
 *
 *  Fields:
 *    id        → auto-generated primary key
 *    fullName  → display name (e.g. "John Doe")
 *    email     → unique login identifier (used instead of username for login)
 *    username  → kept for backward compat / display
 *    password  → BCrypt hash
 *    roles     → many-to-many with Role via user_roles table
 *
 *  Email is the primary login identifier.
 *  Username is still stored for display purposes.
 * ============================================================
 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Display name — e.g. "Rajesh Kumar" */
    @Column(nullable = false)
    private String fullName;

    /**
     * Email address — unique, used as login identifier.
     * Spring Security's loadUserByUsername() will receive this value.
     */
    @Column(unique = true, nullable = false)
    private String email;

    /**
     * Username — kept for display / navbar greeting.
     * Does not need to be unique (email is the unique key).
     */
    @Column(nullable = false)
    private String username;

    /** BCrypt-encoded password — never store plain text */
    @Column(nullable = false)
    private String password;

    /**
     * Roles assigned to this user.
     * EAGER fetch so Spring Security can read authorities on every request
     * without a separate DB query.
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    // ── Constructors ──────────────────────────────────────────────

    public User() {}

    public User(String fullName, String email, String username, String password, Set<Role> roles) {
        this.fullName = fullName;
        this.email    = email;
        this.username = username;
        this.password = password;
        this.roles    = roles;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }

    public String getFullName()             { return fullName; }
    public void setFullName(String fn)      { this.fullName = fn; }

    public String getEmail()                { return email; }
    public void setEmail(String email)      { this.email = email; }

    public String getUsername()             { return username; }
    public void setUsername(String u)       { this.username = u; }

    public String getPassword()             { return password; }
    public void setPassword(String p)       { this.password = p; }

    public Set<Role> getRoles()             { return roles; }
    public void setRoles(Set<Role> roles)   { this.roles = roles; }
}
