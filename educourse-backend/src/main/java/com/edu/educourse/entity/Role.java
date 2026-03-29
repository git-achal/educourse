package com.edu.educourse.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * ============================================================
 *  Role — JPA Entity mapped to the "roles" table
 * ============================================================
 *
 *  Represents a role that can be assigned to users.
 *  This application has two roles: ROLE_ADMIN and ROLE_STUDENT.
 *
 *  Database table structure:
 *  ┌────┬───────────────┐
 *  │ id │ name          │
 *  ├────┼───────────────┤
 *  │  1 │ ROLE_ADMIN    │
 *  │  2 │ ROLE_STUDENT  │
 *  └────┴───────────────┘
 *
 *  Relationship with User:
 *  -----------------------
 *  Role is the INVERSE side of the Many-to-Many relationship.
 *  User is the OWNING side (it has the @JoinTable annotation).
 *  The join table "user_roles" links users and roles together.
 *
 *  Why EnumType.STRING instead of EnumType.ORDINAL?
 *  -------------------------------------------------
 *  ORDINAL stores 0, 1, 2... If you add a new enum value in the
 *  middle of the list, all existing data becomes wrong.
 *  STRING stores "ROLE_ADMIN", "ROLE_STUDENT" — safe to add new values.
 * ============================================================
 */
@Entity
@Table(name = "roles")
public class Role {

    /*
     * @Id         → marks this field as the primary key
     * @GeneratedValue(IDENTITY) → database auto-increments the ID
     *   H2/PostgreSQL will assign: 1, 2, 3, ...
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * @Enumerated(EnumType.STRING):
     *   Stores the enum's name() as a String in the database.
     *   e.g., RoleType.ROLE_ADMIN is stored as "ROLE_ADMIN"
     *
     * @Column(nullable = false, unique = true):
     *   - nullable = false  → DB constraint: this column cannot be NULL
     *   - unique = true     → DB constraint: no two rows can have the same name
     *     This prevents inserting duplicate roles like two "ROLE_ADMIN" entries.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private RoleType name;

    // ── Constructors ─────────────────────────────────────────────

    /**
     * Default no-args constructor.
     * Required by JPA — Hibernate needs to instantiate the entity
     * using reflection when loading data from the database.
     */
    public Role() {
    }

    /**
     * Constructor for creating a Role with a specific RoleType.
     * Used when creating roles programmatically (not via data.sql).
     *
     * @param name the role type (ROLE_ADMIN or ROLE_STUDENT)
     */
    public Role(RoleType name) {
        this.name = name;
    }

    // ── Getters & Setters ─────────────────────────────────────────

    /**
     * Returns the database-generated primary key.
     *
     * @return the role's ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the role's ID.
     * Normally not called manually — managed by JPA/Hibernate.
     *
     * @param id the role ID
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Returns the role type enum value.
     * Use .name() on the result to get the String: "ROLE_ADMIN"
     *
     * @return the RoleType enum value
     */
    public RoleType getName() {
        return name;
    }

    /**
     * Sets the role type.
     *
     * @param name the RoleType enum value to assign
     */
    public void setName(RoleType name) {
        this.name = name;
    }
}
