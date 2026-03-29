package com.edu.educourse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.educourse.entity.Role;
import com.edu.educourse.entity.RoleType;

/**
 * ============================================================
 *  RoleRepository — Data Access Layer for Role entity
 * ============================================================
 *
 *  Extends JpaRepository to get built-in CRUD operations for
 *  the Role entity (save, findById, findAll, delete, etc.)
 *
 *  JpaRepository<Role, Long>:
 *    - Role → the entity this repository manages
 *    - Long → the type of the primary key (Role.id is Long)
 *
 *  Custom Query Method:
 *  --------------------
 *  findByName() is a derived query — Spring reads the method name
 *  and generates the SQL:
 *    SELECT * FROM roles WHERE name = ?
 *
 *  Because Role.name is a @Enumerated(EnumType.STRING) field,
 *  the query compares against the String representation of the enum.
 *  e.g., findByName(RoleType.ROLE_ADMIN) → WHERE name = 'ROLE_ADMIN'
 *
 *  How it's used in the project:
 *  ------------------------------
 *  - AuthService.register() → fetches the role to assign to a new user
 *    e.g., roleRepository.findByName(RoleType.ROLE_STUDENT)
 * ============================================================
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Find a role by its RoleType enum value.
     *
     * Spring Data JPA generates:
     *   SELECT * FROM roles WHERE name = :name
     *
     * Since EnumType.STRING is used, the comparison is against
     * the string name of the enum, e.g., "ROLE_ADMIN".
     *
     * Returns Optional<Role> to safely handle the case where
     * the role doesn't exist in the database yet.
     *
     * Usage:
     *   roleRepository.findByName(RoleType.ROLE_ADMIN)
     *       .orElseThrow(() -> new RuntimeException("Role not found"))
     *
     * @param name the RoleType enum value to search for
     * @return Optional containing the Role if found, or empty if not
     */
    Optional<Role> findByName(RoleType name);
}
