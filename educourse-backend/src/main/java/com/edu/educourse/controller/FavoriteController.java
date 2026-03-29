package com.edu.educourse.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.edu.educourse.dto.CourseDto;
import com.edu.educourse.entity.Course;
import com.edu.educourse.entity.Favorite;
import com.edu.educourse.entity.User;
import com.edu.educourse.repository.CourseRepository;
import com.edu.educourse.repository.FavoriteRepository;
import com.edu.educourse.repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * ============================================================
 *  FavoriteController — Save and Manage Favorite Courses
 * ============================================================
 *
 *  Handles a logged-in student's saved/favorite courses.
 *  All endpoints require a valid JWT (any role).
 *
 *  HOW @AuthenticationPrincipal WORKS:
 *  ─────────────────────────────────────────────────────────────
 *  @AuthenticationPrincipal UserDetails userDetails
 *  → Spring injects the UserDetails object that was set into
 *    the SecurityContextHolder by JwtAuthFilter for this request.
 *  → userDetails.getUsername() returns the EMAIL (because
 *    CustomUserDetails.getUsername() returns email, not display name)
 *  → We use this email to look up the User entity in the database.
 *
 *  ENDPOINTS:
 *  ─────────────────────────────────────────────────────────────
 *  GET    /api/favorites        → get all favorites for logged-in user
 *  POST   /api/favorites/{id}   → add a course to favorites
 *  DELETE /api/favorites/{id}   → remove a course from favorites
 *
 *  DATABASE STRUCTURE (favorites table):
 *  ─────────────────────────────────────────────────────────────
 *  ┌────┬─────────┬───────────┐
 *  │ id │ user_id │ course_id │
 *  ├────┼─────────┼───────────┤
 *  │  1 │       3 │        12 │  ← user #3 favorited course #12
 *  │  2 │       3 │        25 │  ← user #3 also favorited course #25
 *  │  3 │       5 │        12 │  ← user #5 also favorited course #12
 *  └────┴─────────┴───────────┘
 *  UniqueConstraint on (user_id, course_id) prevents duplicates.
 * ============================================================
 */
@RestController
@RequestMapping("/api/favorites")
public class FavoriteController {

    /**
     * Repository for querying and modifying favorite records.
     * findByUser(), findByUserAndCourse(), deleteByUserAndCourse() are all used here.
     */
    @Autowired private FavoriteRepository favoriteRepository;

    /**
     * Repository for loading the full Course entity when adding a favorite.
     * We need the Course object to create a Favorite(user, course) record.
     */
    @Autowired private CourseRepository   courseRepository;

    /**
     * Repository for looking up the User entity by email.
     * @AuthenticationPrincipal gives us the email; we need the User entity.
     */
    @Autowired private UserRepository     userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // GET ALL FAVORITES
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all courses favorited by the currently logged-in user.
     *
     * FLOW:
     *   1. Extract email from JWT (via @AuthenticationPrincipal)
     *   2. Load the User entity from the database by email
     *   3. Query favoriteRepository for all Favorite records where user = this user
     *      SQL: SELECT * FROM favorites WHERE user_id = ?
     *   4. Each Favorite has a getCourse() — extract those Course objects
     *   5. Convert each Course to CourseDto (safe DTO, no internal fields exposed)
     *   6. Return the list
     *
     * Frontend use: displayed on favorites.html page
     *
     * @param userDetails injected by Spring from the SecurityContext (set by JwtAuthFilter)
     * @return list of CourseDto for all favorited courses, or empty list
     */
    @GetMapping
    public ResponseEntity<List<CourseDto>> getFavorites(
            @AuthenticationPrincipal UserDetails userDetails) {
        // userDetails.getUsername() returns email (not display name)
        User user = getUser(userDetails.getUsername());
        return ResponseEntity.ok(
            favoriteRepository.findByUser(user)      // get all Favorite records for this user
                .stream()
                .map(f -> toDto(f.getCourse()))       // extract the Course from each Favorite
                .collect(Collectors.toList())
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADD FAVORITE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Adds a course to the logged-in user's favorites.
     *
     * FLOW:
     *   1. Load the User entity by email from JWT
     *   2. Load the Course entity by the {id} path variable
     *   3. Check if this exact (user, course) combination already exists
     *      SQL: SELECT * FROM favorites WHERE user_id = ? AND course_id = ?
     *      → If already exists: return "Already in favorites." (idempotent, no error)
     *   4. If not exists: create a new Favorite(user, course) and save it
     *      SQL: INSERT INTO favorites (user_id, course_id) VALUES (?, ?)
     *   5. Return success message
     *
     * IDEMPOTENCY:
     *   If the user clicks "Save" twice, the second call finds the record
     *   already exists and returns 200 OK without creating a duplicate.
     *   The database also has a UniqueConstraint as a safety net.
     *
     * @param id          the course ID from the URL path (e.g., /api/favorites/42)
     * @param userDetails the logged-in user's details from JWT
     * @return 200 OK with a message
     */
    @PostMapping("/{id}")
    public ResponseEntity<String> addFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        Course course = getCourse(id);

        // Prevent duplicate favorites — check before inserting
        if (favoriteRepository.findByUserAndCourse(user, course).isPresent())
            return ResponseEntity.ok("Already in favorites.");

        // Create and save the new favorite record
        favoriteRepository.save(new Favorite(user, course));
        return ResponseEntity.ok("Added to favorites.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // REMOVE FAVORITE
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Removes a course from the logged-in user's favorites.
     *
     * FLOW:
     *   1. Load the User entity by email from JWT
     *   2. Load the Course entity by the {id} path variable
     *   3. Delete the Favorite record matching (user, course)
     *      SQL: DELETE FROM favorites WHERE user_id = ? AND course_id = ?
     *   4. Return success message
     *
     * @Transactional:
     *   Required for custom delete methods in Spring Data JPA.
     *   Wraps the deleteByUserAndCourse() call in a database transaction.
     *   Without this annotation, Spring Data would throw an exception
     *   because deleteBy* methods require an active transaction.
     *
     * @param id          the course ID to remove from favorites
     * @param userDetails the logged-in user's details from JWT
     * @return 200 OK with a message
     */
    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<String> removeFavorite(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        Course course = getCourse(id);

        // Delete the specific (user, course) record from the favorites table
        favoriteRepository.deleteByUserAndCourse(user, course);
        return ResponseEntity.ok("Removed from favorites.");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads a User entity from the database by email.
     *
     * Why not use the User directly from UserDetails?
     *   UserDetails only gives us the Spring Security principal (email + password + roles).
     *   We need the actual JPA User entity to use as a foreign key in Favorite records.
     *
     * @param email the user's email (from @AuthenticationPrincipal)
     * @return the User JPA entity
     * @throws RuntimeException if no user with this email exists (should not happen in practice)
     */
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    /**
     * Loads a Course entity from the database by ID.
     *
     * @param id the course's primary key
     * @return the Course JPA entity
     * @throws RuntimeException if no course with this ID exists
     */
    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }

    /**
     * Converts a Course entity to a CourseDto for the API response.
     *
     * Why use a DTO instead of returning the entity directly?
     *   1. DTOs control exactly what fields are exposed in the API response
     *   2. Entities may have lazy-loaded collections that cause serialization issues
     *   3. Cleaner API contract — decoupled from internal database structure
     *
     * @param c the Course entity from the database
     * @return CourseDto with all relevant fields
     */
    private CourseDto toDto(Course c) {
        return new CourseDto(c.getId(), c.getTitle(), c.getInstructor(), c.getCategory(),
                c.getPrice(), c.getDuration(), c.getLevel(), c.getDescription(), c.getCreatedByEmail());
    }
}
