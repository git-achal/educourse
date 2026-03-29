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
 *  Purchase — JPA Entity recording a User buying a Course
 * ============================================================
 *
 *  Table: purchases
 *
 *  Each row = one user has purchased/enrolled in one course.
 *  UniqueConstraint prevents buying the same course twice.
 * ============================================================
 */
@Entity
@Table(
    name = "purchases",
    uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "course_id"})
)
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* The user who made the purchase */
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* The course that was purchased */
    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    // ── Constructors ──────────────────────────────────────────

    public Purchase() {}

    public Purchase(User user, Course course) {
        this.user   = user;
        this.course = course;
    }

    // ── Getters & Setters ─────────────────────────────────────

    public Long getId()                 { return id; }
    public void setId(Long id)          { this.id = id; }

    public User getUser()               { return user; }
    public void setUser(User user)      { this.user = user; }

    public Course getCourse()               { return course; }
    public void setCourse(Course course)    { this.course = course; }
}
