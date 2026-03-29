package com.edu.educourse.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.edu.educourse.entity.User;

/**
 * UserRepository — Data access for User entity.
 *
 * findByEmail()    → used for login (email is the login identifier)
 * findByUsername() → kept for backward compatibility / display lookups
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /** Find user by email (primary login identifier) */
    Optional<User> findByEmail(String email);

    /** Find user by username (display / legacy lookups) */
    Optional<User> findByUsername(String username);

    /** Check if an email is already registered */
    boolean existsByEmail(String email);

    /** Check if a username is already taken */
    boolean existsByUsername(String username);
}
