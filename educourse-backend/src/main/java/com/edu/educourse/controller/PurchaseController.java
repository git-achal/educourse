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
import com.edu.educourse.entity.Purchase;
import com.edu.educourse.entity.User;
import com.edu.educourse.repository.CourseRepository;
import com.edu.educourse.repository.PurchaseRepository;
import com.edu.educourse.repository.UserRepository;

/**
 * ============================================================
 *  PurchaseController — Course Enrollment / Purchase
 * ============================================================
 *
 *  Handles a logged-in student enrolling in (purchasing) courses.
 *  All endpoints require a valid JWT (any role).
 *
 *  ENDPOINTS:
 *  ─────────────────────────────────────────────────────────────
 *  GET  /api/purchases       → list all enrolled courses for the logged-in user
 *  POST /api/purchases/{id}  → enroll in / purchase a specific course
 *
 *  NOTE: There is no DELETE endpoint for purchases.
 *  Once enrolled, a user remains enrolled (simulating a real purchase).
 *  Admins would need direct DB access to reverse a purchase.
 *
 *  DATABASE STRUCTURE (purchases table):
 *  ─────────────────────────────────────────────────────────────
 *  ┌────┬─────────┬───────────┐
 *  │ id │ user_id │ course_id │
 *  ├────┼─────────┼───────────┤
 *  │  1 │       3 │         5 │  ← user #3 purchased course #5
 *  │  2 │       3 │        18 │  ← user #3 also purchased course #18
 *  └────┴─────────┴───────────┘
 *  UniqueConstraint on (user_id, course_id) prevents buying the same course twice.
 * ============================================================
 */
@RestController
@RequestMapping("/api/purchases")
public class PurchaseController {

    /** Repository for querying and saving purchase records */
    @Autowired private PurchaseRepository purchaseRepository;

    /** Repository for looking up Course entities by ID */
    @Autowired private CourseRepository   courseRepository;

    /** Repository for looking up User entities by email */
    @Autowired private UserRepository     userRepository;

    // ─────────────────────────────────────────────────────────────────────────
    // GET MY COURSES (ENROLLED)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns all courses purchased/enrolled in by the logged-in user.
     *
     * FLOW:
     *   1. Get email from JWT via @AuthenticationPrincipal
     *   2. Load User entity from database
     *   3. Find all Purchase records for this user
     *      SQL: SELECT * FROM purchases WHERE user_id = ?
     *   4. Extract Course from each Purchase and convert to DTO
     *   5. Return the list
     *
     * Frontend use: displayed on purchased.html as "My Learning" page.
     *
     * @param userDetails logged-in user's credentials (from JWT via SecurityContext)
     * @return list of CourseDto for all enrolled courses
     */
    @GetMapping
    public ResponseEntity<List<CourseDto>> getPurchased(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername()); // getUsername() returns email
        return ResponseEntity.ok(
            purchaseRepository.findByUser(user)         // all Purchase rows for this user
                .stream()
                .map(p -> toDto(p.getCourse()))          // extract and convert each Course
                .collect(Collectors.toList())
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PURCHASE / ENROLL
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Enrolls the logged-in user in a course (simulates purchase).
     *
     * FLOW:
     *   1. Get email from JWT, load User entity
     *   2. Load the Course entity by {id}
     *   3. Check if already purchased:
     *      SQL: SELECT * FROM purchases WHERE user_id = ? AND course_id = ?
     *      → If found: return 200 OK with "already enrolled" message (no error)
     *   4. If not already purchased:
     *      Create Purchase(user, course), save to database
     *      SQL: INSERT INTO purchases (user_id, course_id) VALUES (?, ?)
     *   5. Return success message with the course title
     *
     * DESIGN NOTE:
     *   This is a simplified purchase flow — no payment processing.
     *   In a real system, you'd integrate a payment gateway (Razorpay, Stripe)
     *   and only create the Purchase record after payment confirmation.
     *
     * @param id          the course ID to enroll in
     * @param userDetails the logged-in user's details from JWT
     * @return 200 OK with a message
     */
    @PostMapping("/{id}")
    public ResponseEntity<String> purchaseCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getUser(userDetails.getUsername());
        Course course = getCourse(id);

        // Prevent double-enrollment — check before inserting
        if (purchaseRepository.findByUserAndCourse(user, course).isPresent())
            return ResponseEntity.ok("You have already enrolled in this course.");

        // Record the enrollment
        purchaseRepository.save(new Purchase(user, course));
        return ResponseEntity.ok("Successfully enrolled in: " + course.getTitle());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads the User JPA entity by email.
     * Email is used because CustomUserDetails.getUsername() returns email.
     *
     * @param email the authenticated user's email
     * @return User entity
     */
    private User getUser(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found: " + email));
    }

    /**
     * Loads the Course JPA entity by primary key.
     *
     * @param id the course's database ID
     * @return Course entity
     */
    private Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found: " + id));
    }

    /**
     * Converts a Course entity to a CourseDto.
     * CourseDto is the safe API-facing representation of a course
     * (contains the same fields but is decoupled from the JPA entity).
     *
     * @param c the Course entity
     * @return CourseDto for JSON serialization
     */
    private CourseDto toDto(Course c) {
        return new CourseDto(c.getId(), c.getTitle(), c.getInstructor(), c.getCategory(),
                c.getPrice(), c.getDuration(), c.getLevel(), c.getDescription(), c.getCreatedByEmail());
    }
}
