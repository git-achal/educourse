package com.edu.educourse.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.edu.educourse.dto.CourseDto;
import com.edu.educourse.entity.Course;
import com.edu.educourse.entity.RoleType;
import com.edu.educourse.entity.User;
import com.edu.educourse.repository.CourseRepository;
import com.edu.educourse.repository.UserRepository;

import jakarta.validation.Valid;

/**
 * ============================================================
 *  CourseController — RESTful API for courses
 * ============================================================
 *
 *  Public GET endpoints (no JWT required):
 *    GET /api/courses                             → all courses
 *    GET /api/courses/categories                  → distinct categories
 *    GET /api/courses/search?q={term}             → keyword search
 *    GET /api/courses/by-category/{cat}           → by category
 *    GET /api/courses/filter?q={t}&category={c}   → combined filter
 *
 *  Authenticated POST/DELETE:
 *    POST   /api/courses          → ADMIN or STUDENT_ADMIN can add courses
 *    DELETE /api/courses/{id}     → ADMIN can delete any; STUDENT_ADMIN only their own
 *
 *  GET /api/courses/my            → STUDENT_ADMIN: see only their own courses
 * ============================================================
 */
@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired private CourseRepository courseRepository;
    @Autowired private UserRepository   userRepository;

    // ── Public READ endpoints ─────────────────────────────────────

    /** GET /api/courses — all courses, public */
    @GetMapping
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return ResponseEntity.ok(
            courseRepository.findAll().stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    /** GET /api/courses/categories — distinct category list, public */
    @GetMapping("/categories")
    public ResponseEntity<List<String>> getCategories() {
        return ResponseEntity.ok(courseRepository.findAllCategories());
    }

    /** GET /api/courses/search?q=java — keyword search, public */
    @GetMapping("/search")
    public ResponseEntity<List<CourseDto>> searchCourses(
            @RequestParam(name = "q", defaultValue = "") String q) {
        List<Course> results = q.isBlank()
                ? courseRepository.findAll()
                : courseRepository.searchCourses(q.trim());
        return ResponseEntity.ok(results.stream().map(this::toDto).collect(Collectors.toList()));
    }

    /** GET /api/courses/by-category/{category} — filter by category, public */
    @GetMapping("/by-category/{category}")
    public ResponseEntity<List<CourseDto>> getCoursesByCategory(@PathVariable String category) {
        return ResponseEntity.ok(
            courseRepository.findByCategory(category).stream()
                .map(this::toDto).collect(Collectors.toList())
        );
    }

    /** GET /api/courses/filter?q=&category= — combined filter, public */
    @GetMapping("/filter")
    public ResponseEntity<List<CourseDto>> filterCourses(
            @RequestParam(name = "q",        defaultValue = "") String q,
            @RequestParam(name = "category", defaultValue = "") String category) {
        return ResponseEntity.ok(
            courseRepository.filterCourses(q.trim(), category.trim())
                .stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    // ── STUDENT_ADMIN: view own courses ──────────────────────────

    /**
     * GET /api/courses/my
     * Returns only the courses created by the currently logged-in STUDENT_ADMIN.
     * ROLE_ADMIN and ROLE_STUDENT_ADMIN can call this.
     */
    @GetMapping("/my")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT_ADMIN')")
    public ResponseEntity<List<CourseDto>> getMyCourses(
            @AuthenticationPrincipal UserDetails userDetails) {
        String email = userDetails.getUsername();  // email is the principal
        return ResponseEntity.ok(
            courseRepository.findByCreatedByEmail(email)
                .stream().map(this::toDto).collect(Collectors.toList())
        );
    }

    // ── Add course ────────────────────────────────────────────────

    /**
     * POST /api/courses
     * ADMIN can add any course.
     * STUDENT_ADMIN can also add courses (createdByEmail is set to their email).
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT_ADMIN')")
    public ResponseEntity<CourseDto> addCourse(
            @Valid @RequestBody CourseDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        String email = userDetails.getUsername();
        Course course = new Course(
            dto.getTitle(), dto.getInstructor(), dto.getCategory(),
            dto.getPrice(), dto.getDuration(), dto.getLevel(),
            dto.getDescription(), email
        );
        return ResponseEntity.ok(toDto(courseRepository.save(course)));
    }

    // ── Update course ─────────────────────────────────────────────

    /**
     * PUT /api/courses/{id}
     *
     * ROLE_ADMIN        → can update any course
     * ROLE_STUDENT_ADMIN → can update ONLY courses they created
     *                      (403 Forbidden otherwise)
     *
     * createdByEmail is never changed — ownership stays with the original creator.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT_ADMIN')")
    public ResponseEntity<?> updateCourse(
            @PathVariable Long id,
            @Valid @RequestBody CourseDto dto,
            @AuthenticationPrincipal UserDetails userDetails) {

        Course course = courseRepository.findById(id).orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        String email  = userDetails.getUsername();
        User   caller = userRepository.findByEmail(email).orElse(null);
        boolean isAdmin = caller != null && caller.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleType.ROLE_ADMIN);

        if (!isAdmin) {
            if (course.getCreatedByEmail() == null ||
                !course.getCreatedByEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only update courses you created.");
            }
        }

        // Apply changes — createdByEmail is intentionally preserved
        course.setTitle(dto.getTitle());
        course.setInstructor(dto.getInstructor());
        course.setCategory(dto.getCategory());
        course.setPrice(dto.getPrice());
        course.setDuration(dto.getDuration());
        course.setLevel(dto.getLevel());
        course.setDescription(dto.getDescription());

        return ResponseEntity.ok(toDto(courseRepository.save(course)));
    }

    // ── Delete course ─────────────────────────────────────────────

    /**
     * DELETE /api/courses/{id}
     *
     * ROLE_ADMIN    → can delete any course
     * ROLE_STUDENT_ADMIN → can delete ONLY courses they created
     *                      (403 Forbidden if they try to delete someone else's)
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','STUDENT_ADMIN')")
    public ResponseEntity<String> deleteCourse(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {

        Course course = courseRepository.findById(id)
                .orElse(null);
        if (course == null) return ResponseEntity.notFound().build();

        String email = userDetails.getUsername();

        // Check if the caller is ADMIN
        User caller = userRepository.findByEmail(email).orElse(null);
        boolean isAdmin = caller != null && caller.getRoles().stream()
                .anyMatch(r -> r.getName() == RoleType.ROLE_ADMIN);

        if (!isAdmin) {
            // STUDENT_ADMIN: can only delete their own courses
            if (course.getCreatedByEmail() == null ||
                !course.getCreatedByEmail().equals(email)) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete courses you created.");
            }
        }

        courseRepository.deleteById(id);
        return ResponseEntity.ok("Course deleted successfully.");
    }

    // ── Helper ────────────────────────────────────────────────────

    private CourseDto toDto(Course c) {
        return new CourseDto(
            c.getId(), c.getTitle(), c.getInstructor(), c.getCategory(),
            c.getPrice(), c.getDuration(), c.getLevel(),
            c.getDescription(), c.getCreatedByEmail()
        );
    }
}
