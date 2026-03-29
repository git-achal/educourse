package com.edu.educourse.entity;

/**
 * ============================================================
 *  RoleType — All application roles
 * ============================================================
 *
 *  ROLE_ADMIN         → Full access. Can manage all courses,
 *                        all users, promote/demote/delete users.
 *                        Assigned via admin-users.properties only.
 *
 *  ROLE_STUDENT_ADMIN → Limited instructor role. Can add new
 *                        courses and delete only their own courses.
 *                        Cannot manage users.
 *
 *  ROLE_STUDENT       → Default role. Can browse, favorite,
 *                        and enroll in courses.
 *
 *  Spring Security convention: names must start with "ROLE_"
 *  so hasRole("ADMIN") maps to authority "ROLE_ADMIN".
 * ============================================================
 */
public enum RoleType {
    ROLE_ADMIN,
    ROLE_STUDENT_ADMIN,
    ROLE_STUDENT
}
