package com.edu.educourse.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.edu.educourse.entity.Course;

/**
 * ============================================================
 *  CourseRepository — Data Access Layer for Course Entity
 * ============================================================
 *
 *  Extends JpaRepository<Course, Long>:
 *    Course → the entity this repository manages
 *    Long   → the type of the primary key (Course.id)
 *
 *  JpaRepository gives us these methods for FREE (no implementation needed):
 *    save(course)        → INSERT or UPDATE
 *    findById(id)        → SELECT * FROM courses WHERE id = ?
 *    findAll()           → SELECT * FROM courses
 *    deleteById(id)      → DELETE FROM courses WHERE id = ?
 *    existsById(id)      → SELECT COUNT(*) > 0 WHERE id = ?
 *    count()             → SELECT COUNT(*) FROM courses
 *    ... and many more
 *
 *  HOW SPRING DATA JPA WORKS:
 *  ─────────────────────────────────────────────────────────────
 *  1. You define an interface extending JpaRepository.
 *  2. At startup, Spring creates a proxy class that implements this interface.
 *  3. For @Query methods: Spring uses the JPQL string you provided.
 *  4. For named methods (like findByCreatedByEmail): Spring parses the
 *     method name and generates the JPQL automatically.
 *     "findByCreatedByEmail" → WHERE c.createdByEmail = :email
 *
 *  JPQL vs SQL:
 *  ─────────────────────────────────────────────────────────────
 *  JPQL (Java Persistence Query Language) works with ENTITY class names and
 *  FIELD names, not table/column names.
 *  - SQL:  SELECT * FROM courses WHERE category = ?
 *  - JPQL: SELECT c FROM Course c WHERE c.category = :category
 *  Spring translates JPQL to the actual SQL for your database.
 * ============================================================
 */
@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    /**
     * Finds all courses in a specific category (case-insensitive).
     *
     * JPQL EXPLAINED:
     *   SELECT c FROM Course c  → select all Course entities
     *   WHERE LOWER(c.category) = LOWER(:category) → case-insensitive match
     *   ORDER BY c.title        → alphabetical sort
     *
     * LOWER() ensures "Programming" == "programming" == "PROGRAMMING"
     * :category is a named parameter bound by @Param("category")
     *
     * Generated SQL example:
     *   SELECT * FROM courses WHERE LOWER(category) = LOWER('programming') ORDER BY title
     *
     * Used by: CourseController.getCoursesByCategory()
     * Frontend: category dropdown on index.html
     *
     * @param category the category name to filter by
     * @return list of matching Course entities, sorted by title
     */
    @Query("SELECT c FROM Course c WHERE LOWER(c.category) = LOWER(:category) ORDER BY c.title")
    List<Course> findByCategory(@Param("category") String category);

    /**
     * Full-text keyword search across title, instructor, category, and description.
     *
     * JPQL EXPLAINED:
     *   LOWER(CONCAT('%', :term, '%'))
     *   → if term = "java", this becomes: '%java%'
     *   → LOWER('%java%') = '%java%'
     *
     *   LOWER(c.title) LIKE LOWER(CONCAT('%', :term, '%'))
     *   → checks if the LOWERCASED title CONTAINS the LOWERCASED term anywhere
     *   → e.g., "Java for Beginners" → "java for beginners" LIKE '%java%' → TRUE
     *
     *   We search across 4 fields with OR so a term like "spring" matches:
     *     - Courses titled "Spring Boot Masterclass"
     *     - Courses by instructor "Spring Framework Expert"
     *     - Courses in category "Spring" (if it existed)
     *     - Courses describing "uses the Spring framework"
     *
     * Generated SQL example for term = "java":
     *   SELECT * FROM courses WHERE
     *   LOWER(title) LIKE '%java%' OR LOWER(instructor) LIKE '%java%' OR
     *   LOWER(category) LIKE '%java%' OR LOWER(description) LIKE '%java%'
     *   ORDER BY title
     *
     * Used by: CourseController.searchCourses()
     * Frontend: search box on index.html
     *
     * @param term the search keyword (e.g., "java", "spring", "data")
     * @return list of matching Course entities sorted by title
     */
    @Query("SELECT c FROM Course c WHERE " +
           "LOWER(c.title)       LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.instructor)  LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.category)    LIKE LOWER(CONCAT('%', :term, '%')) OR " +
           "LOWER(c.description) LIKE LOWER(CONCAT('%', :term, '%')) " +
           "ORDER BY c.title")
    List<Course> searchCourses(@Param("term") String term);

    /**
     * Returns a sorted list of all unique category names.
     *
     * JPQL EXPLAINED:
     *   SELECT DISTINCT c.category → no duplicate category names
     *   FROM Course c               → from all courses
     *   ORDER BY c.category         → alphabetical order
     *
     * Used to populate the category dropdown on the frontend.
     * When a new course is added in a new category, it automatically
     * appears in the dropdown because this query is dynamic.
     *
     * Generated SQL:
     *   SELECT DISTINCT category FROM courses ORDER BY category
     *
     * Used by: CourseController.getCategories()
     * Frontend: category <select> dropdown on index.html
     *
     * @return sorted list of unique category name strings
     */
    @Query("SELECT DISTINCT c.category FROM Course c ORDER BY c.category")
    List<String> findAllCategories();

    /**
     * Combined search + category filter — the main course browsing query.
     *
     * This single query handles ALL browsing scenarios:
     *   - No search, no filter   → :term='', :category='' → returns ALL courses
     *   - Search only            → :term='java', :category='' → full-text search
     *   - Category filter only   → :term='', :category='Programming' → filter only
     *   - Both                   → :term='java', :category='Programming' → both applied
     *
     * JPQL EXPLAINED:
     *   (:term = '' OR LOWER(c.title) LIKE ...)
     *   → If term is empty, this entire condition is TRUE (skip text search)
     *   → If term is non-empty, at least one field must match
     *
     *   AND (:category = '' OR LOWER(c.category) = LOWER(:category))
     *   → If category is empty, this is TRUE (all categories pass)
     *   → If category is non-empty, must match exactly (case-insensitive)
     *
     * This is an efficient approach — one query handles 4 scenarios
     * instead of branching with 4 different repository methods.
     *
     * Used by: CourseController.filterCourses()
     * Frontend: both search and category filter on index.html (combined debounced call)
     *
     * @param term     search keyword, or "" for no search filter
     * @param category category name, or "" for all categories
     * @return filtered and sorted list of Course entities
     */
    @Query("SELECT c FROM Course c WHERE " +
           "(:term     = '' OR LOWER(c.title)      LIKE LOWER(CONCAT('%', :term, '%')) " +
           "              OR LOWER(c.instructor)   LIKE LOWER(CONCAT('%', :term, '%')) " +
           "              OR LOWER(c.category)     LIKE LOWER(CONCAT('%', :term, '%')) " +
           "              OR LOWER(c.description)  LIKE LOWER(CONCAT('%', :term, '%'))) " +
           "AND (:category = '' OR LOWER(c.category) = LOWER(:category)) " +
           "ORDER BY c.title")
    List<Course> filterCourses(@Param("term") String term, @Param("category") String category);

    /**
     * Finds all courses created by a specific user (by their email).
     *
     * This is a Spring Data JPA DERIVED QUERY — Spring reads the method name
     * "findByCreatedByEmail" and automatically generates:
     *   SELECT * FROM courses WHERE created_by_email = ?
     *
     * No @Query annotation needed — the method name IS the query.
     *
     * Naming convention for derived queries:
     *   findBy + FieldName → WHERE field_name = ?
     *   findByCreatedByEmail → WHERE created_by_email = ?
     *   (Spring converts camelCase to snake_case for the column name)
     *
     * Used by: CourseController.getMyCourses() for STUDENT_ADMIN
     * Frontend: Manage Courses page shows only courses the user created
     *
     * @param email the creator's email address
     * @return list of courses created by this user
     */
    List<Course> findByCreatedByEmail(String email);
}
