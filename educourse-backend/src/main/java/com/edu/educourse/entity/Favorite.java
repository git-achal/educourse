package com.edu.educourse.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * ============================================================
 *  Favorite — JPA Entity mapping a User to a favorited Course
 * ============================================================
 *
 *  Table: favorites
 *
 *  Each row = one user has favorited one course.
 *  UniqueConstraint prevents a user favoriting the same course twice.
 *
 *  Relationship:
 *    Many Favorites → One User  (many users can have the same course favorited)
 *    Many Favorites → One Course
 * ============================================================
 */
@Entity
@Table(
    name = "favorites",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
public class Favorite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * The user who favorited the course.
     * @ManyToOne: many favorites can belong to one user.
     * FetchType.LAZY: load user only when accessed (efficient).
     */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* The course that was favorited */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // ── Constructors ──────────────────────────────────────────

    public Favorite() {}

    public Favorite(User user, Course course) {
        this.user   = user;
        this.course = course;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                 { return id; }
    public void setId(Long id)          { this.id = id; }

    public User getUser()               { return user; }
    public void setUser(User user)      { this.user = user; }

    public Course getCourse()                   { return course; }
    public void setCourse(Course course)        { this.course = course; }
}
